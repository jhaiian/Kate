package com.jhaiian.kate.playback

import android.media.AudioDeviceInfo
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessingPipeline
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider.OutputConfig
import com.google.common.collect.ImmutableList
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(UnstableApi::class)
internal class OutputEffects(sampleRate: Int, channels: Int, replayGain: ReplayGainAudioProcessor) {
    private val inputFormat = AudioProcessor.AudioFormat(sampleRate, channels, C.ENCODING_PCM_FLOAT)
    private val sonic = SonicAudioProcessor()
    private val pipeline = AudioProcessingPipeline(
        ImmutableList.of(
            replayGain,
            EqualizerAudioProcessor(),
            ShelfBoostAudioProcessor(treble = false),
            ShelfBoostAudioProcessor(treble = true),
            ParametricEqProcessor(),
            LoudnessProcessor(),
            ChannelMixAudioProcessor(),
            CrossfeedProcessor(),
            FloatSilenceSkipper(),
            CompressorProcessor(),
            sonic,
            LimiterAudioProcessor(floatOutput = true)
        )
    )

    init {
        pipeline.configure(inputFormat)
        pipeline.flush(AudioProcessor.StreamMetadata.DEFAULT)
    }

    fun setPlaybackParameters(params: PlaybackParameters) {
        sonic.setSpeed(params.speed)
        sonic.setPitch(params.pitch)
        pipeline.configure(inputFormat)
        pipeline.flush(AudioProcessor.StreamMetadata.DEFAULT)
    }

    fun process(input: ByteBuffer, consumer: (ByteBuffer) -> Unit) {
        if (!pipeline.isOperational) {
            consumer(input)
            return
        }
        var guard = 0
        while (input.hasRemaining() && guard < 64) {
            val before = input.remaining()
            pipeline.queueInput(input)
            drain(consumer)
            guard = if (input.remaining() == before) guard + 1 else 0
        }
        drain(consumer)
    }

    fun flush() {
        pipeline.flush(AudioProcessor.StreamMetadata.DEFAULT)
    }

    fun reset() {
        pipeline.reset()
    }

    private fun drain(consumer: (ByteBuffer) -> Unit) {
        var output = pipeline.getOutput()
        while (output.hasRemaining()) {
            consumer(output)
            output = pipeline.getOutput()
        }
    }
}

@OptIn(UnstableApi::class)
internal class OboeAudioOutput(
    private val config: OutputConfig,
    private val stream: NativeStream,
    private val effects: OutputEffects?,
    private val tapGate: TapGate,
    private val onRelease: (NativeStream) -> Unit
) : AudioOutput {
    private val listeners = CopyOnWriteArraySet<AudioOutput.Listener>()
    private val channels = stream.channels
    private val inputFloat = config.encoding == C.ENCODING_PCM_FLOAT
    private val inputSampleBytes = if (inputFloat) 4 else 2
    private val inputFrameBytes = channels * inputSampleBytes
    private val deviceSampleBytes = NativeAudio.bytesPerSample(stream.format)
    private val passthrough = effects == null &&
        ((!inputFloat && stream.format == NativeAudio.FORMAT_I16) || (inputFloat && stream.format == NativeAudio.FORMAT_FLOAT))
    private var pending: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var submittedBytes = 0L
    private var deviceFrames = 0L
    private val checkpoints = ArrayDeque<LongArray>()
    private var baseDevice = 0L
    private var baseMedia = 0L
    private var playbackParameters = PlaybackParameters.DEFAULT
    private var playing = false
    private var advancingReported = false
    private var lastUnderruns = 0
    private var released = false

    override fun play() {
        playing = true
        advancingReported = false
        NativeAudio.start(stream.handle)
    }

    override fun pause() {
        playing = false
        NativeAudio.pause(stream.handle)
    }

    override fun write(buffer: ByteBuffer, encodedAccessUnitCount: Int, presentationTimeUs: Long): Boolean {
        if (NativeAudio.isDisconnected(stream.handle)) return false
        NativeAudio.ensureRunning(stream.handle)
        checkUnderrun()
        if (passthrough) {
            val before = buffer.remaining()
            stream.write(buffer)
            submittedBytes += before - buffer.remaining()
            if (tapGate.open && VisualizerTap.active && config.sampleRate > 0) {
                val queued = (submittedBytes / inputFrameBytes - NativeAudio.presentedFrames(stream.handle)).coerceAtLeast(0L)
                VisualizerTap.reportNativeLatency(queued.toDouble() / config.sampleRate)
            }
            return !buffer.hasRemaining()
        }
        if (pending.hasRemaining()) {
            stream.write(pending)
            if (pending.hasRemaining()) return false
        }
        if (!buffer.hasRemaining()) return true
        if (effects != null && tapGate.open && VisualizerTap.active) {
            VisualizerTap.capture(buffer, config.encoding, channels, config.sampleRate)
        }
        submittedBytes += buffer.remaining()
        pending.clear()
        pending.limit(0)
        if (effects != null) {
            effects.process(buffer) { chunk -> append(chunk) }
        } else {
            append(buffer)
        }
        deviceFrames += pending.remaining() / (channels * deviceSampleBytes)
        checkpoints.addLast(longArrayOf(deviceFrames, submittedBytes / inputFrameBytes))
        stream.write(pending)
        if (tapGate.open && VisualizerTap.active && config.sampleRate > 0) {
            val queued = (deviceFrames - NativeAudio.presentedFrames(stream.handle)).coerceAtLeast(0L)
            VisualizerTap.reportNativeLatency(queued.toDouble() / config.sampleRate)
        }
        return !pending.hasRemaining()
    }

    private fun mediaFramesAt(presented: Long): Long {
        if (passthrough) return presented
        while (checkpoints.size > 1 && checkpoints.first()[0] <= presented) {
            val done = checkpoints.removeFirst()
            baseDevice = done[0]
            baseMedia = done[1]
        }
        val next = checkpoints.firstOrNull() ?: return baseMedia
        if (presented >= next[0]) return next[1]
        val span = next[0] - baseDevice
        if (span <= 0L) return baseMedia
        val fraction = (presented - baseDevice).coerceAtLeast(0L).toDouble() / span
        return baseMedia + ((next[1] - baseMedia) * fraction).toLong()
    }

    override fun flush() {
        NativeAudio.reset(stream.handle)
        pending.clear()
        pending.limit(0)
        submittedBytes = 0L
        deviceFrames = 0L
        checkpoints.clear()
        baseDevice = 0L
        baseMedia = 0L
        advancingReported = false
        effects?.flush()
        if (playing) NativeAudio.start(stream.handle)
    }

    override fun stop() {
        checkUnderrun()
    }

    override fun release() {
        if (released) return
        released = true
        playing = false
        effects?.reset()
        onRelease(stream)
        listeners.forEach { it.onReleased() }
    }

    override fun setVolume(volume: Float) {
        NativeAudio.setVolume(stream.handle, volume)
    }

    override fun isOffloadedPlayback(): Boolean = false

    override fun getAudioSessionId(): Int = config.audioSessionId

    override fun getSampleRate(): Int = config.sampleRate

    override fun getBufferSizeInFrames(): Long = NativeAudio.bufferFrames(stream.handle).toLong()

    override fun getPositionUs(): Long {
        NativeAudio.ensureRunning(stream.handle)
        val submittedFrames = submittedBytes / inputFrameBytes
        val frames = minOf(mediaFramesAt(NativeAudio.presentedFrames(stream.handle)), submittedFrames)
        val positionUs = frames * 1_000_000L / config.sampleRate
        if (playing && !advancingReported && frames > 0) {
            advancingReported = true
            val startMs = System.currentTimeMillis() - positionUs / 1_000L
            listeners.forEach { it.onPositionAdvancing(startMs) }
        }
        return positionUs
    }

    override fun getPlaybackParameters(): PlaybackParameters = playbackParameters

    override fun isStalled(): Boolean = NativeAudio.isDisconnected(stream.handle)

    override fun addListener(listener: AudioOutput.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AudioOutput.Listener) {
        listeners.remove(listener)
    }

    override fun setPlaybackParameters(playbackParams: PlaybackParameters) {
        val target = effects ?: return
        playbackParameters = PlaybackParameters(
            playbackParams.speed.coerceIn(0.1f, 8f),
            playbackParams.pitch.coerceIn(0.1f, 8f)
        )
        target.setPlaybackParameters(playbackParameters)
    }

    override fun setOffloadDelayPadding(delayInFrames: Int, paddingInFrames: Int) = Unit

    override fun setOffloadEndOfStream() = Unit

    override fun attachAuxEffect(effectId: Int) = Unit

    override fun setAuxEffectSendLevel(level: Float) = Unit

    override fun setPreferredDevice(preferredDevice: AudioDeviceInfo?) = Unit

    private fun checkUnderrun() {
        val count = NativeAudio.underrunCount(stream.handle)
        if (count != lastUnderruns) {
            lastUnderruns = count
            if (playing) listeners.forEach { it.onUnderrun() }
        }
    }

    private fun append(chunk: ByteBuffer) {
        val sourceFloat = inputFloat || effects != null
        val sourceSampleBytes = if (sourceFloat) 4 else 2
        val samples = chunk.remaining() / sourceSampleBytes
        ensureSpace(samples * deviceSampleBytes)
        val target = pending
        target.position(target.limit())
        target.limit(target.capacity())
        repeat(samples) {
            if (sourceFloat) putFloatSample(target, chunk.getFloat()) else putShortSample(target, chunk.getShort().toInt())
        }
        chunk.position(chunk.limit())
        target.flip()
    }

    private fun ensureSpace(extra: Int) {
        val used = pending.limit()
        if (pending.capacity() - used >= extra) return
        val grown = ByteBuffer.allocateDirect(maxOf(used + extra, pending.capacity() * 2)).order(ByteOrder.nativeOrder())
        pending.position(0)
        grown.put(pending)
        grown.flip()
        pending = grown
    }

    private fun putFloatSample(target: ByteBuffer, value: Float) {
        when (stream.format) {
            NativeAudio.FORMAT_FLOAT -> target.putFloat(value)
            NativeAudio.FORMAT_I24 -> putInt24(target, (value * 8_388_608.0).roundToLong().coerceIn(-8_388_608L, 8_388_607L).toInt())
            NativeAudio.FORMAT_I32 -> target.putInt((value * 2_147_483_648.0).roundToLong().coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt())
            else -> target.putShort((value * 32_768f).roundToInt().coerceIn(-32_768, 32_767).toShort())
        }
    }

    private fun putShortSample(target: ByteBuffer, value: Int) {
        when (stream.format) {
            NativeAudio.FORMAT_FLOAT -> target.putFloat(value / 32_768f)
            NativeAudio.FORMAT_I24 -> putInt24(target, value shl 8)
            NativeAudio.FORMAT_I32 -> target.putInt(value shl 16)
            else -> target.putShort(value.toShort())
        }
    }

    private fun putInt24(target: ByteBuffer, value: Int) {
        target.put((value and 0xFF).toByte())
        target.put(((value shr 8) and 0xFF).toByte())
        target.put(((value shr 16) and 0xFF).toByte())
    }
}
