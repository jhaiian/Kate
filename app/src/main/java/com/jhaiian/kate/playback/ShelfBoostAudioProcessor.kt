package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.EffectsParams
import com.jhaiian.kate.data.DspParams
import com.jhaiian.kate.data.EqualizerMath
import com.jhaiian.kate.data.EqualizerParams
import com.jhaiian.kate.data.EqualizerSettings
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class ShelfBoostAudioProcessor(private val treble: Boolean) : BaseAudioProcessor() {
    private companion object {
        const val SHORT_SCALE = 32_768.0
        const val SHORT_MAX = 32_767.0
    }

    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var appliedTone: EffectsParams? = null
    private var appliedHz = 0
    private var applied: DspParams? = null
    private var appliedEq: EqualizerParams? = null
    private var appliedDb = 0f
    private var active = false
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0
    private var state = DoubleArray(0)

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding
        state = DoubleArray(channelCount.coerceAtLeast(0) * 2)
        applied = null
        appliedEq = null
        appliedDb = Float.NaN
    }

    override fun onReset() {
        sampleRate = 0
        channelCount = 0
        encoding = C.ENCODING_INVALID
        state = DoubleArray(0)
        applied = null
        appliedEq = null
        appliedTone = null
        active = false
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val params = AudioSettings.dspParams
        val eq = EqualizerSettings.params
        val tone = AudioEffects.params
        if (params !== applied || eq !== appliedEq || tone !== appliedTone) {
            applied = params
            appliedEq = eq
            val db = when {
                !eq.enabled -> 0f
                treble -> params.trebleDb
                else -> params.bassDb
            }
            val hz = if (treble) tone.trebleHz else tone.bassHz
            if (db != appliedDb || hz != appliedHz) {
                appliedHz = hz
                rebuild(db)
            }
            appliedTone = tone
        }
        val output = replaceOutputBuffer(size)
        if (!active) {
            output.put(inputBuffer)
            output.flip()
            return
        }
        if (encoding == C.ENCODING_PCM_16BIT) {
            val frameBytes = channelCount * 2
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channelCount) {
                    val processed = process(inputBuffer.getShort() / SHORT_SCALE, channel).coerceIn(-1.0, 1.0)
                    output.putShort((processed * SHORT_MAX).roundToInt().toShort())
                }
            }
        } else {
            val frameBytes = channelCount * 4
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channelCount) {
                    output.putFloat(process(inputBuffer.getFloat().toDouble(), channel).toFloat())
                }
            }
        }
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }

    private fun process(sample: Double, channel: Int): Double {
        val offset = channel * 2
        val out = b0 * sample + state[offset]
        state[offset] = b1 * sample - a1 * out + state[offset + 1]
        state[offset + 1] = b2 * sample - a2 * out
        return out
    }

    private fun rebuild(db: Float) {
        appliedDb = db
        if (db <= 0f || sampleRate <= 0) {
            if (active) state.fill(0.0)
            active = false
            return
        }
        val coefficients = EqualizerMath.shelf(db.toDouble(), sampleRate.toDouble(), treble)
        b0 = coefficients[0]
        b1 = coefficients[1]
        b2 = coefficients[2]
        a1 = coefficients[3]
        a2 = coefficients[4]
        active = true
    }
}
