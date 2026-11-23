package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(UnstableApi::class)
internal class ResamplerAudioProcessor(val deviceRate: Int) : BaseAudioProcessor() {
    private companion object {
        const val HALF = 16
        const val TAPS = HALF * 2
        const val PHASES = 256
        const val BETA = 8.6
        const val CUTOFF_MARGIN = 0.97
        const val SHORT_SCALE = 32_768f
    }

    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var step = 1.0
    private var table = FloatArray(0)
    private var history = FloatArray(0)
    private var historyFrames = 0
    private var position = 0.0
    private val weights = FloatArray(TAPS)

    @Volatile
    var holdRate = 0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val params = AudioSettings.pipelineParams
        if (params.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val targetRate = when {
            params.resample && deviceRate > 0 -> deviceRate
            holdRate > 0 -> holdRate
            else -> return AudioProcessor.AudioFormat.NOT_SET
        }
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        if (inputAudioFormat.sampleRate == targetRate) return AudioProcessor.AudioFormat.NOT_SET
        return AudioProcessor.AudioFormat(targetRate, inputAudioFormat.channelCount, C.ENCODING_PCM_FLOAT)
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        val inputRate = inputAudioFormat.sampleRate
        val targetRate = outputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding
        if (inputRate <= 0 || channelCount <= 0 || targetRate <= 0) return
        step = inputRate.toDouble() / targetRate
        table = buildTable(min(1.0, targetRate.toDouble() / inputRate) * CUTOFF_MARGIN)
        history = FloatArray(4_096 * channelCount)
        historyFrames = HALF
        position = HALF.toDouble()
    }

    override fun onReset() {
        channelCount = 0
        encoding = C.ENCODING_INVALID
        table = FloatArray(0)
        history = FloatArray(0)
        historyFrames = 0
        position = 0.0
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val sampleBytes = if (encoding == C.ENCODING_PCM_16BIT) 2 else 4
        val frameBytes = channelCount * sampleBytes
        if (frameBytes <= 0 || table.isEmpty()) {
            inputBuffer.position(inputBuffer.limit())
            return
        }
        val frames = size / frameBytes
        ensureCapacity(historyFrames + frames)
        var write = historyFrames * channelCount
        repeat(frames * channelCount) {
            history[write++] = if (sampleBytes == 2) inputBuffer.getShort() / SHORT_SCALE else inputBuffer.getFloat()
        }
        inputBuffer.position(inputBuffer.limit())
        historyFrames += frames

        val available = historyFrames - HALF - position
        val maxOut = if (available <= 0.0) 0 else (available / step).toInt() + 2
        val output = replaceOutputBuffer(maxOut * channelCount * 4)
        while (floor(position).toInt() + HALF < historyFrames) {
            val base = floor(position).toInt()
            computeWeights(position - base)
            val start = (base - HALF + 1) * channelCount
            for (channel in 0 until channelCount) {
                var sum = 0f
                var index = start + channel
                for (tap in 0 until TAPS) {
                    sum += history[index] * weights[tap]
                    index += channelCount
                }
                output.putFloat(sum)
            }
            position += step
        }
        output.flip()

        val drop = floor(position).toInt() - HALF + 1
        if (drop > 0) {
            System.arraycopy(history, drop * channelCount, history, 0, (historyFrames - drop) * channelCount)
            historyFrames -= drop
            position -= drop
        }
    }

    private fun ensureCapacity(frames: Int) {
        val needed = frames * channelCount
        if (history.size >= needed) return
        history = history.copyOf(maxOf(needed, history.size * 2))
    }

    private fun computeWeights(fraction: Double) {
        for (tap in 0 until TAPS) {
            val x = (tap - HALF + 1) - fraction
            val scaled = (x + HALF) * PHASES
            val index = scaled.toInt().coerceIn(0, table.size - 2)
            val blend = (scaled - index).toFloat()
            weights[tap] = table[index] + (table[index + 1] - table[index]) * blend
        }
    }

    private fun buildTable(cutoff: Double): FloatArray {
        val size = TAPS * PHASES + 2
        val denominator = besselI0(BETA)
        return FloatArray(size) { n ->
            val x = n.toDouble() / PHASES - HALF
            val ratio = x / HALF
            if (ratio <= -1.0 || ratio >= 1.0) {
                0f
            } else {
                val window = besselI0(BETA * sqrt(1.0 - ratio * ratio)) / denominator
                val arg = PI * cutoff * x
                val sinc = if (x == 0.0) 1.0 else sin(arg) / arg
                (cutoff * sinc * window).toFloat()
            }
        }
    }

    private fun besselI0(value: Double): Double {
        var sum = 1.0
        var term = 1.0
        val half = value / 2.0
        var k = 1
        while (k < 50) {
            term *= (half / k) * (half / k)
            sum += term
            if (term < sum * 1e-12) break
            k++
        }
        return sum
    }
}
