package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Metadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink
import androidx.media3.extractor.metadata.id3.InternalFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.ReplayGainParams
import java.nio.ByteBuffer
import kotlin.math.pow

internal data class ReplayGainTags(
    val trackGainDb: Float?,
    val trackPeak: Float?,
    val albumGainDb: Float?,
    val albumPeak: Float?
) {
    companion object {
        val NONE = ReplayGainTags(null, null, null, null)
        private const val R128_REFERENCE_OFFSET_DB = 5f
        private const val R128_SCALE = 256f
        private val Number = Regex("""[-+]?\d+(?:[.,]\d+)?""")

        @OptIn(UnstableApi::class)
        fun from(metadata: Metadata?): ReplayGainTags {
            if (metadata == null) return NONE
            var trackGain: Float? = null
            var trackPeak: Float? = null
            var albumGain: Float? = null
            var albumPeak: Float? = null
            var r128Track: Float? = null
            var r128Album: Float? = null
            fun read(key: String?, value: String?) {
                if (key == null || value == null) return
                when (key.trim().uppercase()) {
                    "REPLAYGAIN_TRACK_GAIN" -> trackGain = number(value)
                    "REPLAYGAIN_TRACK_PEAK" -> trackPeak = number(value)
                    "REPLAYGAIN_ALBUM_GAIN" -> albumGain = number(value)
                    "REPLAYGAIN_ALBUM_PEAK" -> albumPeak = number(value)
                    "R128_TRACK_GAIN" -> r128Track = number(value)?.let { it / R128_SCALE + R128_REFERENCE_OFFSET_DB }
                    "R128_ALBUM_GAIN" -> r128Album = number(value)?.let { it / R128_SCALE + R128_REFERENCE_OFFSET_DB }
                }
            }
            for (index in 0 until metadata.length()) {
                when (val entry = metadata.get(index)) {
                    is VorbisComment -> read(entry.key, entry.value)
                    is TextInformationFrame -> read(entry.description, entry.values.firstOrNull())
                    is InternalFrame -> read(entry.description, entry.text)
                }
            }
            return ReplayGainTags(
                trackGainDb = trackGain ?: r128Track,
                trackPeak = trackPeak?.takeIf { it > 0f },
                albumGainDb = albumGain ?: r128Album,
                albumPeak = albumPeak?.takeIf { it > 0f }
            )
        }

        private fun number(value: String): Float? =
            Number.find(value)?.value?.replace(',', '.')?.toFloatOrNull()
    }
}

@OptIn(UnstableApi::class)
internal class ReplayGainAudioProcessor(private val live: Boolean = false) : BaseAudioProcessor() {
    private companion object {
        const val RAMP_SECONDS = 0.05
        const val SHORT_SCALE = 32_768.0
    }

    @Volatile
    private var pendingTags = ReplayGainTags.NONE
    private var tags = ReplayGainTags.NONE
    private var appliedParams: ReplayGainParams? = null
    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var current = 1.0
    private var target = 1.0
    private var step = 0.0
    private var rampFrames = 0

    fun setPendingTags(value: ReplayGainTags) {
        pendingTags = value
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return AudioProcessor.AudioFormat(inputAudioFormat.sampleRate, inputAudioFormat.channelCount, C.ENCODING_PCM_FLOAT)
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding
        tags = pendingTags
        val params = AudioSettings.replayGainParams
        appliedParams = params
        target = gainFor(params, tags)
        current = target
        rampFrames = 0
    }

    override fun onReset() {
        sampleRate = 0
        channelCount = 0
        encoding = C.ENCODING_INVALID
        tags = ReplayGainTags.NONE
        appliedParams = null
        current = 1.0
        target = 1.0
        rampFrames = 0
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val params = AudioSettings.replayGainParams
        val latestTags = pendingTags
        if (live && latestTags !== tags) {
            tags = latestTags
            appliedParams = params
            retarget(gainFor(params, latestTags))
        }
        if (params !== appliedParams) {
            appliedParams = params
            retarget(gainFor(params, tags))
        }
        if (encoding == C.ENCODING_PCM_16BIT) {
            val frameBytes = channelCount * 2
            val frames = size / frameBytes
            val output = replaceOutputBuffer(frames * channelCount * 4)
            repeat(frames) {
                val gain = nextGain()
                for (channel in 0 until channelCount) {
                    output.putFloat((inputBuffer.getShort() / SHORT_SCALE * gain).toFloat())
                }
            }
            inputBuffer.position(inputBuffer.limit())
            output.flip()
            return
        }
        val output = replaceOutputBuffer(size)
        if (rampFrames == 0 && current == 1.0) {
            output.put(inputBuffer)
            output.flip()
            return
        }
        val frameBytes = channelCount * 4
        while (inputBuffer.remaining() >= frameBytes) {
            val gain = nextGain()
            for (channel in 0 until channelCount) {
                output.putFloat((inputBuffer.getFloat() * gain).toFloat())
            }
        }
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }

    private fun nextGain(): Double {
        if (rampFrames > 0) {
            current += step
            rampFrames -= 1
            if (rampFrames == 0) current = target
        }
        return current
    }

    private fun retarget(value: Double) {
        target = value
        val frames = (sampleRate * RAMP_SECONDS).toInt()
        if (frames <= 0 || value == current) {
            current = value
            rampFrames = 0
            return
        }
        step = (value - current) / frames
        rampFrames = frames
    }

    private fun gainFor(params: ReplayGainParams, tags: ReplayGainTags): Double {
        if (params.mode == AudioSettings.REPLAY_GAIN_OFF) return 1.0
        val preferAlbum = params.mode == AudioSettings.REPLAY_GAIN_ALBUM
        val gainDb = if (preferAlbum) tags.albumGainDb ?: tags.trackGainDb else tags.trackGainDb ?: tags.albumGainDb
        val peak = if (preferAlbum) tags.albumPeak ?: tags.trackPeak else tags.trackPeak ?: tags.albumPeak
        val totalDb = if (gainDb != null) gainDb + params.preampDb else params.fallbackDb
        var linear = 10.0.pow(totalDb / 20.0)
        if (params.preventClipping && gainDb != null && peak != null) {
            linear = minOf(linear, 1.0 / peak)
        }
        return linear
    }
}

@OptIn(UnstableApi::class)
internal class ReplayGainAudioSink(
    sink: AudioSink,
    private val processors: List<ReplayGainAudioProcessor>,
    private val context: Context,
    private val floatOutput: Boolean,
    private val nativeOutput: Boolean,
    private val resampler: ResamplerAudioProcessor
) : ForwardingAudioSink(sink) {
    private var lastOutputRate = 0

    override fun configure(audioSinkConfig: AudioSink.AudioSinkConfig) {
        val format = audioSinkConfig.format
        val tags = ReplayGainTags.from(format.metadata)
        processors.forEach { it.setPendingTags(tags) }
        val raw = MimeTypes.AUDIO_RAW == format.sampleMimeType && format.sampleRate > 0
        val floatPath = floatOutput && Util.isEncodingHighResolutionPcm(format.pcmEncoding)
        val params = AudioSettings.pipelineParams
        val continuing = raw && !floatPath && lastOutputRate > 0 && hasPendingData()
        resampler.holdRate = if (continuing && format.sampleRate != lastOutputRate && !params.bitPerfect) lastOutputRate else 0
        lastOutputRate = when {
            !raw || floatPath || params.bitPerfect -> format.sampleRate
            params.resample && resampler.deviceRate > 0 -> resampler.deviceRate
            resampler.holdRate > 0 -> resampler.holdRate
            else -> format.sampleRate
        }
        if (!nativeOutput && MimeTypes.AUDIO_RAW == format.sampleMimeType && format.sampleRate > 0 && format.channelCount > 0) {
            val encoding = if (floatOutput && Util.isEncodingHighResolutionPcm(format.pcmEncoding)) {
                C.ENCODING_PCM_FLOAT
            } else {
                C.ENCODING_PCM_16BIT
            }
            BitPerfect.prepare(context, format.sampleRate, format.channelCount, encoding)
        }
        super.configure(audioSinkConfig)
    }
}

@OptIn(UnstableApi::class)
internal class KateRenderersFactory(
    context: Context,
    private val tapGate: TapGate
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParams: Boolean
    ): AudioSink {
        val params = AudioSettings.pipelineParams
        val native = params.native && NativeAudio.available
        val hiRes = params.hiRes
        val floatOutput = enableFloatOutput || hiRes || (native && params.bitPerfect)
        val replayGain = ReplayGainAudioProcessor()
        val outputReplayGain = ReplayGainAudioProcessor(live = true)
        val resampler = ResamplerAudioProcessor(deviceSampleRate(context))
        val builder = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(floatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParams || (hiRes && !native) || (native && floatOutput))
        if (native) builder.setAudioOutputProvider(KateAudioOutputProvider(context, outputReplayGain, tapGate))
        val sink = builder
            .setAudioProcessors(
                arrayOf(
                    replayGain,
                    EqualizerAudioProcessor(),
                    ShelfBoostAudioProcessor(treble = false),
                    ShelfBoostAudioProcessor(treble = true),
                    ParametricEqProcessor(),
                    LoudnessProcessor(),
                    ChannelMixAudioProcessor(),
                    CrossfeedProcessor(),
                    CompressorProcessor(),
                    resampler,
                    LimiterAudioProcessor(),
                    VisualizerTapProcessor(tapGate)
                )
            )
            .build()
        return ReplayGainAudioSink(
            sink,
            if (native) listOf(replayGain, outputReplayGain) else listOf(replayGain),
            context.applicationContext,
            floatOutput,
            native,
            resampler
        )
    }

    private fun deviceSampleRate(context: Context): Int {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return 0
        return audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 0
    }
}
