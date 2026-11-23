package com.jhaiian.kate.data

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object WaveformStore {
    const val BUCKETS = 160
    private const val FILE_VERSION = 1
    private const val CACHE_DIR = "waveforms"
    private const val SAMPLED_THRESHOLD_US = 15L * 60L * 1_000_000L
    private const val SAMPLE_WINDOW_US = 300_000L
    private const val TIMEOUT_US = 10_000L
    private const val SAMPLE_STRIDE = 4
    private const val MAX_STALLS = 500
    private const val NORMALIZE_PERCENTILE = 0.98
    private const val CURVE = 0.75
    private const val FLOOR = 0.04f

    private val memory = LruCache<String, FloatArray>(32)
    private val failed = HashSet<String>()
    private val mutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher = Dispatchers.IO.limitedParallelism(1)

    fun cached(trackId: String): FloatArray? = memory.get(trackId)

    suspend fun load(context: Context, track: Track): FloatArray? {
        memory.get(track.id)?.let { return it }
        val appContext = context.applicationContext
        return mutex.withLock {
            memory.get(track.id)?.let { return@withLock it }
            if (track.id in failed) return@withLock null
            val result = withContext(decodeDispatcher) {
                val file = fileFor(appContext, track.id)
                read(file) ?: compute(appContext, track)?.also { write(file, it) }
            }
            if (result != null) memory.put(track.id, result) else failed.add(track.id)
            result
        }
    }

    private fun fileFor(context: Context, trackId: String): File {
        val digest = MessageDigest.getInstance("SHA-1").digest(trackId.toByteArray())
        val name = digest.joinToString("") { "%02x".format(it) }
        return File(File(context.cacheDir, CACHE_DIR), "$name.wf")
    }

    private fun read(file: File): FloatArray? = runCatching {
        if (!file.exists()) return@runCatching null
        DataInputStream(file.inputStream().buffered()).use { input ->
            if (input.readInt() != FILE_VERSION) return@use null
            val count = input.readInt()
            if (count != BUCKETS) return@use null
            FloatArray(count) { input.readFloat() }
        }
    }.getOrNull()

    private fun write(file: File, values: FloatArray) {
        runCatching {
            file.parentFile?.mkdirs()
            DataOutputStream(file.outputStream().buffered()).use { output ->
                output.writeInt(FILE_VERSION)
                output.writeInt(values.size)
                values.forEach { output.writeFloat(it) }
            }
        }
    }

    private suspend fun compute(context: Context, track: Track): FloatArray? {
        val extractor = MediaExtractor()
        try {
            runCatching { extractor.setDataSource(context, track.uri, null) }.getOrElse { return null }
            val trackIndex = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return null
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else track.durationMs * 1_000L
            if (durationUs <= 0L) return null
            val codec = runCatching { MediaCodec.createDecoderByType(mime) }.getOrElse { return null }
            val sums = DoubleArray(BUCKETS)
            val counts = LongArray(BUCKETS)
            try {
                codec.configure(format, null, null, 0)
                codec.start()
                val decoder = Decoder(extractor, codec, durationUs, sums, counts)
                if (durationUs <= SAMPLED_THRESHOLD_US) {
                    decoder.run(endUs = Long.MAX_VALUE)
                } else {
                    for (bucket in 0 until BUCKETS) {
                        coroutineContext.ensureActive()
                        val startUs = durationUs * bucket / BUCKETS
                        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                        codec.flush()
                        decoder.run(endUs = startUs + SAMPLE_WINDOW_US)
                    }
                }
            } catch (cancel: kotlinx.coroutines.CancellationException) {
                throw cancel
            } catch (error: Exception) {
                return null
            } finally {
                runCatching { codec.stop() }
                runCatching { codec.release() }
            }
            return normalize(sums, counts)
        } finally {
            runCatching { extractor.release() }
        }
    }

    private class Decoder(
        private val extractor: MediaExtractor,
        private val codec: MediaCodec,
        private val durationUs: Long,
        private val sums: DoubleArray,
        private val counts: LongArray
    ) {
        private val info = MediaCodec.BufferInfo()
        private var floatPcm = false

        suspend fun run(endUs: Long) {
            var inputDone = false
            var stalls = 0
            while (true) {
                coroutineContext.ensureActive()
                if (!inputDone) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val buffer = codec.getInputBuffer(inputIndex)
                        val size = if (buffer != null) extractor.readSampleData(buffer, 0) else -1
                        val time = extractor.sampleTime
                        if (size < 0 || time < 0 || time > endUs) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, size, time, 0)
                            extractor.advance()
                        }
                    }
                }
                val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outputIndex >= 0 -> {
                        stalls = 0
                        if (info.size > 0) {
                            codec.getOutputBuffer(outputIndex)?.let { accumulate(it) }
                        }
                        codec.releaseOutputBuffer(outputIndex, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val output = codec.outputFormat
                        floatPcm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                            output.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                            output.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                    }
                    else -> {
                        stalls += 1
                        if (inputDone && stalls > MAX_STALLS) return
                    }
                }
            }
        }

        private fun accumulate(buffer: ByteBuffer) {
            val bucket = ((info.presentationTimeUs.coerceAtLeast(0L) * BUCKETS) / durationUs).toInt()
            if (bucket !in 0 until BUCKETS) return
            buffer.position(info.offset)
            buffer.limit(info.offset + info.size)
            val data = buffer.slice().order(ByteOrder.nativeOrder())
            var sum = 0.0
            var count = 0L
            if (floatPcm) {
                val floats = data.asFloatBuffer()
                var index = 0
                while (index < floats.limit()) {
                    val value = floats.get(index).toDouble()
                    sum += value * value
                    count += 1
                    index += SAMPLE_STRIDE
                }
            } else {
                val shorts = data.asShortBuffer()
                var index = 0
                while (index < shorts.limit()) {
                    val value = shorts.get(index) / 32_768.0
                    sum += value * value
                    count += 1
                    index += SAMPLE_STRIDE
                }
            }
            sums[bucket] += sum
            counts[bucket] += count
        }
    }

    private fun normalize(sums: DoubleArray, counts: LongArray): FloatArray? {
        val rms = DoubleArray(BUCKETS) { if (counts[it] > 0) sqrt(sums[it] / counts[it]) else -1.0 }
        if (rms.none { it >= 0.0 }) return null
        for (index in 0 until BUCKETS) {
            if (rms[index] >= 0.0) continue
            val previous = (index - 1 downTo 0).firstOrNull { rms[it] >= 0.0 }?.let { rms[it] }
            val next = (index + 1 until BUCKETS).firstOrNull { rms[it] >= 0.0 }?.let { rms[it] }
            rms[index] = when {
                previous != null && next != null -> (previous + next) / 2.0
                else -> previous ?: next ?: 0.0
            }
        }
        val sorted = rms.sorted()
        val reference = max(sorted[((sorted.size - 1) * NORMALIZE_PERCENTILE).toInt()], 1e-6)
        return FloatArray(BUCKETS) { index ->
            (rms[index] / reference).coerceIn(0.0, 1.0).pow(CURVE).toFloat().coerceAtLeast(FLOOR)
        }
    }
}
