package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object VisualizerTap {
    const val TARGET_RATE = 22_050
    private const val RING = 1 shl 16
    private const val MASK = RING - 1
    private const val DEFAULT_LATENCY_SEC = 0.25
    private const val NATIVE_LATENCY_FRESH_NS = 1_000_000_000L

    private val users = AtomicInteger(0)
    private val ring = FloatArray(RING)

    @Volatile private var written = 0L
    @Volatile private var lastWriteNanos = 0L
    @Volatile private var lastLatencySamples = 0L
    @Volatile private var nativeLatencySec = 0.0
    @Volatile private var nativeLatencyAt = 0L
    @Volatile var rate = TARGET_RATE
        private set

    private var decimation = 1
    private var phase = 0
    private var accumulator = 0f

    val active: Boolean get() = users.get() > 0

    fun acquire() {
        users.incrementAndGet()
    }

    fun release() {
        if (users.decrementAndGet() < 0) users.set(0)
    }

    fun reportNativeLatency(seconds: Double) {
        nativeLatencySec = seconds
        nativeLatencyAt = System.nanoTime()
    }

    @Synchronized
    fun capture(buffer: ByteBuffer, encoding: Int, channels: Int, sampleRate: Int) {
        if (channels <= 0 || sampleRate <= 0) return
        val data = buffer.duplicate().order(ByteOrder.nativeOrder())
        val bytesPerSample = if (encoding == C.ENCODING_PCM_FLOAT) 4 else if (encoding == C.ENCODING_PCM_16BIT) 2 else return
        val frameBytes = bytesPerSample * channels
        val frames = data.remaining() / frameBytes
        if (frames <= 0) return
        val step = (sampleRate / TARGET_RATE).coerceAtLeast(1)
        if (step != decimation) {
            decimation = step
            phase = 0
            accumulator = 0f
        }
        rate = sampleRate / step
        var index = written
        var position = data.position()
        repeat(frames) {
            var sum = 0f
            for (channel in 0 until channels) {
                sum += if (bytesPerSample == 4) data.getFloat(position) else data.getShort(position) / 32_768f
                position += bytesPerSample
            }
            accumulator += sum / channels
            phase++
            if (phase >= decimation) {
                ring[(index and MASK.toLong()).toInt()] = accumulator / decimation
                index++
                phase = 0
                accumulator = 0f
            }
        }
        val now = System.nanoTime()
        val latencySec = if (now - nativeLatencyAt < NATIVE_LATENCY_FRESH_NS) nativeLatencySec else DEFAULT_LATENCY_SEC
        lastLatencySamples = (latencySec * rate).toLong()
        lastWriteNanos = now
        written = index
    }

    fun read(destination: FloatArray): Boolean {
        val end = written
        if (end <= 0L) return false
        val elapsed = ((System.nanoTime() - lastWriteNanos) / 1e9 * rate).toLong()
        val playIndex = (end - lastLatencySamples + elapsed).coerceAtMost(end)
        val start = playIndex - destination.size
        if (start < 0L || end - start > RING) return false
        for (i in destination.indices) destination[i] = ring[((start + i) and MASK.toLong()).toInt()]
        return true
    }

    fun reset() {
        written = 0L
    }
}

internal class TapGate(open: Boolean) {
    @Volatile
    var open = open
}

@OptIn(UnstableApi::class)
internal class VisualizerTapProcessor(private val gate: TapGate) : BaseAudioProcessor() {
    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT && inputAudioFormat.encoding != C.ENCODING_PCM_FLOAT) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return
        if (gate.open && VisualizerTap.active) {
            VisualizerTap.capture(inputBuffer, inputAudioFormat.encoding, inputAudioFormat.channelCount, inputAudioFormat.sampleRate)
        }
        replaceOutputBuffer(remaining).put(inputBuffer).flip()
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        if (gate.open) VisualizerTap.reset()
    }
}

class SpectrumAnalyzer(private val size: Int = 1024) {
    private val real = FloatArray(size)
    private val imag = FloatArray(size)
    private val window = FloatArray(size) { (0.5 - 0.5 * cos(2.0 * PI * it / (size - 1))).toFloat() }
    private val bits = Integer.numberOfTrailingZeros(size)
    val samples = FloatArray(size)

    fun magnitudes(output: FloatArray): FloatArray {
        for (i in 0 until size) {
            real[reverse(i)] = samples[i] * window[i]
            imag[i] = 0f
        }
        var half = 1
        while (half < size) {
            val step = half * 2
            val angle = -PI / half
            for (k in 0 until half) {
                val wr = cos(angle * k).toFloat()
                val wi = sin(angle * k).toFloat()
                var i = k
                while (i < size) {
                    val j = i + half
                    val tr = wr * real[j] - wi * imag[j]
                    val ti = wr * imag[j] + wi * real[j]
                    real[j] = real[i] - tr
                    imag[j] = imag[i] - ti
                    real[i] += tr
                    imag[i] += ti
                    i += step
                }
            }
            half = step
        }
        val bins = size / 2
        for (i in 0 until minOf(bins, output.size)) {
            output[i] = sqrt(real[i] * real[i] + imag[i] * imag[i]) / bins
        }
        return output
    }

    private fun reverse(value: Int): Int = Integer.reverse(value) ushr (32 - bits)
}
