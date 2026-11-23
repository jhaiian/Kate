package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.EffectsParams
import com.jhaiian.kate.data.EqualizerMath
import com.jhaiian.kate.data.ParametricFilter
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object BiquadMath {
    private const val NYQUIST_FRACTION = 0.45

    fun coefficients(filter: ParametricFilter, sampleRate: Double): DoubleArray? {
        val frequency = filter.frequency.toDouble()
        if (frequency <= 0.0 || frequency >= sampleRate * NYQUIST_FRACTION) return null
        val q = filter.q.toDouble().coerceAtLeast(0.05)
        val gain = filter.gainDb.toDouble()
        return when (filter.type) {
            AudioEffects.FILTER_LOW_SHELF -> shelf(frequency, gain, q, sampleRate, high = false)
            AudioEffects.FILTER_HIGH_SHELF -> shelf(frequency, gain, q, sampleRate, high = true)
            AudioEffects.FILTER_LOW_PASS -> pass(frequency, q, sampleRate, high = false)
            AudioEffects.FILTER_HIGH_PASS -> pass(frequency, q, sampleRate, high = true)
            AudioEffects.FILTER_NOTCH -> notch(frequency, q, sampleRate)
            else -> EqualizerMath.peaking(frequency, gain, sampleRate, q)
        }
    }

    fun shelf(frequency: Double, gainDb: Double, q: Double, sampleRate: Double, high: Boolean): DoubleArray {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * frequency / sampleRate
        val cosW = cos(w0)
        val alpha = sin(w0) / (2.0 * q)
        val root = 2.0 * sqrt(a) * alpha
        return if (high) {
            val a0 = (a + 1.0) - (a - 1.0) * cosW + root
            doubleArrayOf(
                a * ((a + 1.0) + (a - 1.0) * cosW + root) / a0,
                -2.0 * a * ((a - 1.0) + (a + 1.0) * cosW) / a0,
                a * ((a + 1.0) + (a - 1.0) * cosW - root) / a0,
                2.0 * ((a - 1.0) - (a + 1.0) * cosW) / a0,
                ((a + 1.0) - (a - 1.0) * cosW - root) / a0
            )
        } else {
            val a0 = (a + 1.0) + (a - 1.0) * cosW + root
            doubleArrayOf(
                a * ((a + 1.0) - (a - 1.0) * cosW + root) / a0,
                2.0 * a * ((a - 1.0) - (a + 1.0) * cosW) / a0,
                a * ((a + 1.0) - (a - 1.0) * cosW - root) / a0,
                -2.0 * ((a - 1.0) + (a + 1.0) * cosW) / a0,
                ((a + 1.0) + (a - 1.0) * cosW - root) / a0
            )
        }
    }

    private fun pass(frequency: Double, q: Double, sampleRate: Double, high: Boolean): DoubleArray {
        val w0 = 2.0 * PI * frequency / sampleRate
        val cosW = cos(w0)
        val alpha = sin(w0) / (2.0 * q)
        val a0 = 1.0 + alpha
        return if (high) {
            doubleArrayOf((1.0 + cosW) / 2.0 / a0, -(1.0 + cosW) / a0, (1.0 + cosW) / 2.0 / a0, -2.0 * cosW / a0, (1.0 - alpha) / a0)
        } else {
            doubleArrayOf((1.0 - cosW) / 2.0 / a0, (1.0 - cosW) / a0, (1.0 - cosW) / 2.0 / a0, -2.0 * cosW / a0, (1.0 - alpha) / a0)
        }
    }

    private fun notch(frequency: Double, q: Double, sampleRate: Double): DoubleArray {
        val w0 = 2.0 * PI * frequency / sampleRate
        val cosW = cos(w0)
        val alpha = sin(w0) / (2.0 * q)
        val a0 = 1.0 + alpha
        return doubleArrayOf(1.0 / a0, -2.0 * cosW / a0, 1.0 / a0, -2.0 * cosW / a0, (1.0 - alpha) / a0)
    }

    fun responseDb(filters: List<ParametricFilter>, preampDb: Float, frequency: Double, sampleRate: Double = 48_000.0): Double {
        var total = preampDb.toDouble()
        filters.forEach { filter ->
            if (!filter.enabled) return@forEach
            val c = coefficients(filter, sampleRate) ?: return@forEach
            total += EqualizerMath.magnitudeDb(c, frequency, sampleRate)
        }
        return total
    }
}

internal class BiquadBank {
    private var coefficients = Array(0) { DoubleArray(5) }
    private var state = Array(0) { DoubleArray(0) }

    val size: Int get() = coefficients.size

    fun configure(filters: List<DoubleArray>, channels: Int) {
        val resize = filters.size != coefficients.size || state.firstOrNull()?.size != channels * 2
        coefficients = filters.map { it.copyOf() }.toTypedArray()
        if (resize) state = Array(filters.size) { DoubleArray(channels * 2) }
    }

    fun clear() {
        state.forEach { it.fill(0.0) }
    }

    fun process(sample: Double, channel: Int): Double {
        var value = sample
        val offset = channel * 2
        for (index in coefficients.indices) {
            val c = coefficients[index]
            val s = state[index]
            val out = c[0] * value + s[offset]
            s[offset] = c[1] * value - c[3] * out + s[offset + 1]
            s[offset + 1] = c[2] * value - c[4] * out
            value = out
        }
        return value
    }
}

@OptIn(UnstableApi::class)
internal abstract class FrameAudioProcessor : BaseAudioProcessor() {
    private companion object {
        const val SHORT_SCALE = 32_768.0
        const val SHORT_MAX = 32_767.0
    }

    protected var sampleRate = 0
        private set
    protected var channels = 0
        private set
    private var encoding = C.ENCODING_INVALID
    private var frame = DoubleArray(0)

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (AudioSettings.pipelineParams.bitPerfect) return AudioProcessor.AudioFormat.NOT_SET
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT || inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        val format = inputAudioFormat
        sampleRate = format.sampleRate.coerceAtLeast(0)
        channels = format.channelCount.coerceAtLeast(0)
        encoding = format.encoding
        frame = DoubleArray(channels)
        streamChanged()
    }

    override fun onReset() {
        sampleRate = 0
        channels = 0
        encoding = C.ENCODING_INVALID
        frame = DoubleArray(0)
        streamChanged()
    }

    protected abstract fun refresh(): Boolean

    protected abstract fun processFrame(frame: DoubleArray)

    protected open fun streamChanged() {}

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val output = replaceOutputBuffer(size)
        if (sampleRate <= 0 || channels <= 0 || !refresh()) {
            output.put(inputBuffer)
            output.flip()
            return
        }
        if (encoding == C.ENCODING_PCM_16BIT) {
            val frameBytes = channels * 2
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channels) frame[channel] = inputBuffer.getShort() / SHORT_SCALE
                processFrame(frame)
                for (channel in 0 until channels) {
                    output.putShort((frame[channel].coerceIn(-1.0, 1.0) * SHORT_MAX).roundToInt().toShort())
                }
            }
        } else {
            val frameBytes = channels * 4
            while (inputBuffer.remaining() >= frameBytes) {
                for (channel in 0 until channels) frame[channel] = inputBuffer.getFloat().toDouble()
                processFrame(frame)
                for (channel in 0 until channels) output.putFloat(frame[channel].toFloat())
            }
        }
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }
}

internal class ParametricEqProcessor : FrameAudioProcessor() {
    private val bank = BiquadBank()
    private var applied: EffectsParams? = null
    private var appliedRate = 0
    private var active = false
    private var preamp = 1.0

    override fun streamChanged() {
        applied = null
        bank.clear()
    }

    override fun refresh(): Boolean {
        val params = AudioEffects.params
        if (params !== applied || appliedRate != sampleRate) {
            applied = params
            appliedRate = sampleRate
            val rate = sampleRate.toDouble()
            val coefficients = if (params.parametricEnabled) {
                params.filters.filter { it.enabled }.mapNotNull { BiquadMath.coefficients(it, rate) }
            } else {
                emptyList()
            }
            bank.configure(coefficients, channels)
            preamp = 10.0.pow(params.parametricPreampDb / 20.0)
            val wasActive = active
            active = params.parametricEnabled && (coefficients.isNotEmpty() || params.parametricPreampDb != 0f)
            if (active && !wasActive) bank.clear()
        }
        return active
    }

    override fun processFrame(frame: DoubleArray) {
        for (channel in frame.indices) frame[channel] = bank.process(frame[channel] * preamp, channel)
    }
}

internal class LoudnessProcessor : FrameAudioProcessor() {
    private companion object {
        const val BASS_HZ = 100.0
        const val TREBLE_HZ = 10_000.0
        const val MAX_BASS_DB = 10.0
        const val MAX_TREBLE_DB = 6.0
        const val STEP = 0.05f
    }

    private val bank = BiquadBank()
    private var appliedStrength = -1
    private var appliedVolume = -1f
    private var appliedRate = 0
    private var active = false

    override fun streamChanged() {
        appliedStrength = -1
        bank.clear()
    }

    override fun refresh(): Boolean {
        val strength = AudioEffects.params.loudness
        val volume = (AudioEffects.volumeFraction / STEP).roundToInt() * STEP
        if (strength != appliedStrength || volume != appliedVolume || appliedRate != sampleRate) {
            appliedStrength = strength
            appliedVolume = volume
            appliedRate = sampleRate
            val amount = strength / 100.0 * (1.0 - volume)
            val rate = sampleRate.toDouble()
            val wasActive = active
            active = amount > 0.01 && TREBLE_HZ < rate * 0.45
            if (active) {
                bank.configure(
                    listOf(
                        BiquadMath.shelf(BASS_HZ, MAX_BASS_DB * amount, 0.707, rate, high = false),
                        BiquadMath.shelf(TREBLE_HZ, MAX_TREBLE_DB * amount, 0.707, rate, high = true)
                    ),
                    channels
                )
                if (!wasActive) bank.clear()
            }
        }
        return active
    }

    override fun processFrame(frame: DoubleArray) {
        for (channel in frame.indices) frame[channel] = bank.process(frame[channel], channel)
    }
}

internal class CrossfeedProcessor : FrameAudioProcessor() {
    private var appliedLevel = -1
    private var appliedRate = 0
    private var coefficient = 0.0
    private var feed = 0.0
    private var norm = 1.0
    private var lowLeft = 0.0
    private var lowRight = 0.0

    override fun streamChanged() {
        appliedLevel = -1
        lowLeft = 0.0
        lowRight = 0.0
    }

    override fun refresh(): Boolean {
        if (channels != 2) return false
        val level = AudioEffects.params.crossfeed
        if (level != appliedLevel || appliedRate != sampleRate) {
            appliedLevel = level
            appliedRate = sampleRate
            val (cutoff, feedDb) = when (level) {
                1 -> 650.0 to -9.5
                2 -> 700.0 to -6.0
                else -> 700.0 to -4.5
            }
            coefficient = 1.0 - exp(-2.0 * PI * cutoff / sampleRate)
            feed = 10.0.pow(feedDb / 20.0)
            norm = 1.0 / (1.0 + feed * 0.6)
        }
        return level > 0
    }

    override fun processFrame(frame: DoubleArray) {
        val left = frame[0]
        val right = frame[1]
        lowLeft += coefficient * (left - lowLeft)
        lowRight += coefficient * (right - lowRight)
        frame[0] = (left + lowRight * feed) * norm
        frame[1] = (right + lowLeft * feed) * norm
    }
}

internal class CompressorProcessor : FrameAudioProcessor() {
    private var appliedLevel = -1
    private var appliedRate = 0
    private var threshold = 0.0
    private var ratio = 1.0
    private var makeup = 1.0
    private var attack = 0.0
    private var release = 0.0
    private var envelope = 0.0

    override fun streamChanged() {
        appliedLevel = -1
        envelope = 0.0
    }

    override fun refresh(): Boolean {
        val level = AudioEffects.params.compressor
        if (level != appliedLevel || appliedRate != sampleRate) {
            appliedLevel = level
            appliedRate = sampleRate
            val (thresholdDb, ratioValue, makeupDb) = when (level) {
                1 -> Triple(-16.0, 2.0, 3.0)
                2 -> Triple(-22.0, 3.0, 6.0)
                else -> Triple(-30.0, 4.0, 9.0)
            }
            threshold = thresholdDb
            ratio = ratioValue
            makeup = 10.0.pow(makeupDb / 20.0)
            attack = exp(-1.0 / (0.005 * sampleRate))
            release = exp(-1.0 / (0.18 * sampleRate))
        }
        return level > 0
    }

    override fun processFrame(frame: DoubleArray) {
        var peak = 0.0
        for (sample in frame) {
            val magnitude = abs(sample)
            if (magnitude > peak) peak = magnitude
        }
        envelope = if (peak > envelope) attack * envelope + (1.0 - attack) * peak else release * envelope + (1.0 - release) * peak
        val levelDb = 20.0 * log10(envelope.coerceAtLeast(1e-9))
        val reductionDb = if (levelDb > threshold) (threshold + (levelDb - threshold) / ratio) - levelDb else 0.0
        val gain = 10.0.pow(reductionDb / 20.0) * makeup
        for (channel in frame.indices) frame[channel] *= gain
    }
}
