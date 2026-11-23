package com.jhaiian.kate.playback

import java.nio.ByteBuffer

internal object NativeAudio {
    const val FORMAT_I16 = 1
    const val FORMAT_FLOAT = 2
    const val FORMAT_I24 = 3
    const val FORMAT_I32 = 4

    val available: Boolean by lazy {
        runCatching { System.loadLibrary("kateaudio") }.isSuccess
    }

    fun bytesPerSample(format: Int): Int = when (format) {
        FORMAT_I16 -> 2
        FORMAT_I24 -> 3
        else -> 4
    }

    @JvmStatic
    external fun open(sampleRate: Int, channels: Int, format: Int, capacityFrames: Int): Long

    @JvmStatic
    external fun start(handle: Long): Boolean

    @JvmStatic
    external fun ensureRunning(handle: Long)

    @JvmStatic
    external fun pause(handle: Long)

    @JvmStatic
    external fun reset(handle: Long)

    @JvmStatic
    external fun close(handle: Long)

    @JvmStatic
    external fun writeDirect(handle: Long, buffer: ByteBuffer, offset: Int, length: Int): Int

    @JvmStatic
    external fun writeArray(handle: Long, data: ByteArray, offset: Int, length: Int): Int

    @JvmStatic
    external fun presentedFrames(handle: Long): Long

    @JvmStatic
    external fun setVolume(handle: Long, volume: Float)

    @JvmStatic
    external fun isDisconnected(handle: Long): Boolean

    @JvmStatic
    external fun underrunCount(handle: Long): Int

    @JvmStatic
    external fun bufferFrames(handle: Long): Int
}

internal class NativeStream(
    val handle: Long,
    val sampleRate: Int,
    val channels: Int,
    val format: Int
) {
    val frameBytes: Int = channels * NativeAudio.bytesPerSample(format)

    fun write(buffer: ByteBuffer): Int {
        val length = buffer.remaining()
        if (length <= 0) return 0
        val written = if (buffer.isDirect) {
            NativeAudio.writeDirect(handle, buffer, buffer.position(), length)
        } else if (buffer.hasArray()) {
            NativeAudio.writeArray(handle, buffer.array(), buffer.arrayOffset() + buffer.position(), length)
        } else {
            val copy = ByteArray(length)
            buffer.duplicate().get(copy)
            NativeAudio.writeArray(handle, copy, 0, length)
        }
        if (written > 0) buffer.position(buffer.position() + written)
        return written
    }

    fun matches(sampleRate: Int, channels: Int, format: Int): Boolean =
        this.sampleRate == sampleRate && this.channels == channels && this.format == format

    fun close() {
        NativeAudio.close(handle)
    }
}
