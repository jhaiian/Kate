package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioTrack
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider
import androidx.media3.exoplayer.audio.AudioOutputProvider.FormatConfig
import androidx.media3.exoplayer.audio.AudioOutputProvider.FormatSupport
import androidx.media3.exoplayer.audio.AudioOutputProvider.OutputConfig
import androidx.media3.exoplayer.audio.AudioTrackAudioOutputProvider
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import com.jhaiian.kate.data.AudioSettings

@OptIn(UnstableApi::class)
internal class KateAudioOutputProvider(
    context: Context,
    private val outputReplayGain: ReplayGainAudioProcessor,
    private val tapGate: TapGate
) : AudioOutputProvider {
    private companion object {
        const val RING_SECONDS_DIVISOR = 1
        const val MAX_CHANNELS = 8
    }

    private val appContext = context.applicationContext
    private val fallback = AudioTrackAudioOutputProvider.Builder(appContext)
        .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(appContext))
        .build()
    private var idle: NativeStream? = null

    override fun getFormatSupport(formatConfig: FormatConfig): FormatSupport = fallback.getFormatSupport(formatConfig)

    override fun getOutputConfig(formatConfig: FormatConfig): OutputConfig {
        val format = formatConfig.format
        if (formatConfig.enableTunneling || MimeTypes.AUDIO_RAW != format.sampleMimeType ||
            !nativeEncoding(format.pcmEncoding) || format.channelCount !in 1..MAX_CHANNELS || format.sampleRate <= 0 ||
            !NativeAudio.available
        ) {
            return fallback.getOutputConfig(formatConfig)
        }
        val channelMask = Util.getAudioTrackChannelConfig(format.channelCount)
        val frameBytes = Util.getPcmFrameSize(format.pcmEncoding, format.channelCount)
        val minimum = AudioTrack.getMinBufferSize(format.sampleRate, channelMask, format.pcmEncoding)
        val preferred = if (formatConfig.preferredBufferSize > 0) formatConfig.preferredBufferSize else 0
        val bufferSize = maxOf(
            preferred,
            if (minimum > 0) minimum * 4 else format.sampleRate * frameBytes / RING_SECONDS_DIVISOR
        )
        return OutputConfig.Builder()
            .setEncoding(format.pcmEncoding)
            .setSampleRate(format.sampleRate)
            .setChannelMask(channelMask)
            .setBufferSize(bufferSize)
            .setAudioAttributes(formatConfig.audioAttributes)
            .setAudioSessionId(formatConfig.audioSessionId)
            .setVirtualDeviceId(formatConfig.virtualDeviceId)
            .setUsePlaybackParameters(formatConfig.enablePlaybackParameters && format.pcmEncoding == C.ENCODING_PCM_FLOAT)
            .build()
    }

    override fun getAudioOutput(config: OutputConfig): AudioOutput {
        if (!config.isOffload && !config.isTunneling && nativeEncoding(config.encoding) && NativeAudio.available) {
            val channels = Integer.bitCount(config.channelMask)
            val inputFloat = config.encoding == C.ENCODING_PCM_FLOAT
            val deviceFormat = chooseDeviceFormat(config.sampleRate, channels, inputFloat)
            val stream = obtain(config.sampleRate, channels, deviceFormat)
            if (stream != null) {
                val effects = if (inputFloat) {
                    runCatching { OutputEffects(config.sampleRate, channels, outputReplayGain) }.getOrNull()
                } else {
                    null
                }
                return OboeAudioOutput(config, stream, effects, tapGate, ::recycle)
            }
        }
        return fallback.getAudioOutput(config)
    }

    override fun addListener(listener: AudioOutputProvider.Listener) {
        fallback.addListener(listener)
    }

    override fun removeListener(listener: AudioOutputProvider.Listener) {
        fallback.removeListener(listener)
    }

    override fun setClock(clock: Clock) {
        fallback.setClock(clock)
    }

    override fun hasPendingReleases(): Boolean = fallback.hasPendingReleases()

    override fun release() {
        idle?.close()
        idle = null
        fallback.release()
    }

    private fun nativeEncoding(encoding: Int): Boolean =
        encoding == C.ENCODING_PCM_16BIT || encoding == C.ENCODING_PCM_FLOAT

    private fun chooseDeviceFormat(sampleRate: Int, channels: Int, inputFloat: Boolean): Int {
        val fallbackFormat = if (inputFloat) NativeAudio.FORMAT_FLOAT else NativeAudio.FORMAT_I16
        if (!AudioSettings.pipelineParams.bitPerfect) return fallbackFormat
        return BitPerfect.chooseNativeFormat(appContext, sampleRate, channels, inputFloat) ?: fallbackFormat
    }

    private fun obtain(sampleRate: Int, channels: Int, format: Int): NativeStream? {
        val cached = idle
        idle = null
        if (cached != null) {
            if (cached.matches(sampleRate, channels, format) && !NativeAudio.isDisconnected(cached.handle)) return cached
            cached.close()
        }
        val capacityFrames = maxOf(sampleRate / RING_SECONDS_DIVISOR, 4_096)
        val handle = runCatching { NativeAudio.open(sampleRate, channels, format, capacityFrames) }.getOrDefault(0L)
        if (handle == 0L) return null
        return NativeStream(handle, sampleRate, channels, format)
    }

    private fun recycle(stream: NativeStream) {
        if (NativeAudio.isDisconnected(stream.handle)) {
            stream.close()
            return
        }
        NativeAudio.reset(stream.handle)
        idle?.close()
        idle = stream
    }
}
