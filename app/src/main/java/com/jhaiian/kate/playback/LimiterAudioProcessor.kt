package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class LimiterAudioProcessor(private val floatOutput: Boolean = false) : BaseAudioProcessor() {
    private companion object {
        const val THRESHOLD = 0.944
        const val RELEASE_SECONDS = 0.15
        const val SEEK_FADE_SECONDS = 0.05
        const val SHORT_SCALE = 32_768.0
        const val SHORT_MIN = -32_768
        const val SHORT_MAX = 32_767
    }

    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var gain = 1.0
    private var release = 0.0
    private var fadeTotal = 0
    private var fadeLeft = 0
    private var frame = DoubleArray(0)

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        val outputEncoding = if (floatOutput) C.ENCODING_PCM_FLOAT else C.ENCODING_PCM_16BIT
        return AudioProcessor.AudioFormat(inputAudioFormat.sampleRate, inputAudioFormat.channelCount, outputEncoding)
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        val rate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding
        frame = DoubleArray(channelCount.coerceAtLeast(0))
        gain = 1.0
        release = if (rate > 0) exp(-1.0 / (rate * RELEASE_SECONDS)) else 0.0
        fadeTotal = if (AudioSettings.dspParams.smoothSeeking && rate > 0) (rate * SEEK_FADE_SECONDS).toInt() else 0
        fadeLeft = fadeTotal
    }

    override fun onReset() {
        channelCount = 0
        encoding = C.ENCODING_INVALID
        gain = 1.0
        fadeTotal = 0
        fadeLeft = 0
        frame = DoubleArray(0)
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val sampleBytes = if (encoding == C.ENCODING_PCM_16BIT) 2 else 4
        val frameBytes = channelCount * sampleBytes
        if (frameBytes <= 0) {
            inputBuffer.position(inputBuffer.limit())
            return
        }
        val frames = size / frameBytes
        val output = replaceOutputBuffer(frames * channelCount * if (floatOutput) 4 else 2)
        val limiting = AudioSettings.dspParams.limiter
        val isShort = encoding == C.ENCODING_PCM_16BIT
        repeat(frames) {
            var peak = 0.0
            for (channel in 0 until channelCount) {
                val sample = if (isShort) inputBuffer.getShort() / SHORT_SCALE else inputBuffer.getFloat().toDouble()
                frame[channel] = sample
                val level = abs(sample)
                if (level > peak) peak = level
            }
            if (limiting) {
                val needed = if (peak > THRESHOLD) THRESHOLD / peak else 1.0
                gain = if (needed < gain) needed else needed - (needed - gain) * release
            } else {
                gain = 1.0
            }
            var scale = gain
            if (fadeLeft > 0) {
                scale *= 1.0 - fadeLeft.toDouble() / fadeTotal
                fadeLeft -= 1
            }
            if (floatOutput) {
                for (channel in 0 until channelCount) {
                    output.putFloat((frame[channel] * scale).coerceIn(-1.0, 1.0).toFloat())
                }
            } else {
                for (channel in 0 until channelCount) {
                    val value = (frame[channel] * scale * SHORT_SCALE).roundToInt().coerceIn(SHORT_MIN, SHORT_MAX)
                    output.putShort(value.toShort())
                }
            }
        }
        inputBuffer.position(inputBuffer.limit())
        output.flip()
    }
}
