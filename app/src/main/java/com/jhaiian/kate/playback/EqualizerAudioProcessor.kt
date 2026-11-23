package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.EqualizerMath
import com.jhaiian.kate.data.EqualizerParams
import com.jhaiian.kate.data.EqualizerSettings
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class EqualizerAudioProcessor : BaseAudioProcessor() {
    private companion object {
        const val NYQUIST_LIMIT = 0.45
        const val SHORT_SCALE = 32_768.0
        const val SHORT_MAX = 32_767.0
    }

    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var applied: EqualizerParams? = null
    private var processing = false
    private var preamp = 1.0
    private val bands = EqualizerSettings.BAND_COUNT
    private val coefficients = Array(bands) { DoubleArray(5) }
    private val bandActive = BooleanArray(bands)
    private var state = Array(bands) { DoubleArray(0) }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val params = EqualizerSettings.params
        if (params !== applied) rebuild(params)
        val output = replaceOutputBuffer(size)
        if (!processing) {
            output.put(inputBuffer)
            output.flip()
            return
        }
        if (encoding == C.ENCODING_PCM_16BIT) {
            val frameBytes = channelCount * 2
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channelCount) {
                    val sample = inputBuffer.getShort() / SHORT_SCALE
                    val processed = process(sample, channel).coerceIn(-1.0, 1.0)
                    output.putShort((processed * SHORT_MAX).roundToInt().toShort())
                }
            }
        } else {
            val frameBytes = channelCount * 4
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channelCount) {
                    val sample = inputBuffer.getFloat().toDouble()
                    output.putFloat(process(sample, channel).toFloat())
                }
            }
        }
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        val format = inputAudioFormat
        if (format.channelCount <= 0 || format.sampleRate <= 0) {
            sampleRate = 0
            channelCount = 0
            encoding = C.ENCODING_INVALID
            state = Array(bands) { DoubleArray(0) }
            applied = null
            return
        }
        val changed = format.sampleRate != sampleRate || format.channelCount != channelCount || format.encoding != encoding
        sampleRate = format.sampleRate
        channelCount = format.channelCount
        encoding = format.encoding
        if (changed || state.firstOrNull()?.size != channelCount * 2) {
            state = Array(bands) { DoubleArray(channelCount * 2) }
            applied = null
        } else {
            clearState()
        }
    }

    override fun onReset() {
        sampleRate = 0
        channelCount = 0
        encoding = C.ENCODING_INVALID
        applied = null
        processing = false
        state = Array(bands) { DoubleArray(0) }
    }

    private fun process(sample: Double, channel: Int): Double {
        var value = sample * preamp
        val offset = channel * 2
        for (band in 0 until bands) {
            if (!bandActive[band]) continue
            val c = coefficients[band]
            val s = state[band]
            val out = c[0] * value + s[offset]
            s[offset] = c[1] * value - c[3] * out + s[offset + 1]
            s[offset + 1] = c[2] * value - c[4] * out
            value = out
        }
        return value
    }

    private fun rebuild(params: EqualizerParams) {
        applied = params
        val rate = sampleRate.toDouble()
        var anyBand = false
        for (band in 0 until bands) {
            val gain = params.gains.getOrElse(band) { 0f }
            val frequency = EqualizerSettings.BandFrequencies[band].toDouble()
            val active = params.enabled && gain != 0f && rate > 0.0 && frequency < rate * NYQUIST_LIMIT
            if (active) {
                EqualizerMath.peaking(frequency, gain.toDouble(), rate).copyInto(coefficients[band])
                anyBand = true
            } else if (bandActive[band]) {
                state[band].fill(0.0)
            }
            bandActive[band] = active
        }
        preamp = if (params.enabled) 10.0.pow(params.preampDb / 20.0) else 1.0
        processing = params.enabled && (anyBand || params.preampDb != 0f)
    }

    private fun clearState() {
        state.forEach { it.fill(0.0) }
    }
}
