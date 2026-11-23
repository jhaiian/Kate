package com.jhaiian.kate.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import com.kyant.taglib.AudioPropertiesReadStyle
import com.kyant.taglib.Picture
import com.kyant.taglib.PropertyMap
import com.kyant.taglib.TagLib
import java.nio.ByteBuffer

internal class AudioTags(
    val title: String?,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val genre: String,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val durationMs: Long,
    val sampleRate: Int?,
    val bitDepth: Int?,
    val mimeType: String?,
    val formatHint: String?,
    val picture: ByteArray?
)

private class HeaderInfo(val bitDepth: Int?, val formatHint: String?)

internal object AudioTagReader {
    private const val HEADER_BYTES = 64 * 1024
    private val YEAR_PATTERN = Regex("""\d{4}""")
    private val LYRICS_KEYS = listOf("LYRICS", "UNSYNCEDLYRICS", "UNSYNCED LYRICS", "SYNCEDLYRICS")

    private val tagLibAvailable: Boolean by lazy {
        runCatching {
            TagLib.hashCode()
            true
        }.getOrDefault(false)
    }

    fun read(context: Context, uri: Uri, readPicture: Boolean): AudioTags? {
        val viaTagLib = if (tagLibAvailable) readWithTagLib(context, uri, readPicture) else null
        return viaTagLib ?: readWithRetriever(context, uri, readPicture)
    }

    fun readLyrics(context: Context, uri: Uri): String? {
        if (!tagLibAvailable) return null
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                val map = TagLib.getMetadata(descriptor.dup().detachFd(), false)?.propertyMap ?: return@use null
                LYRICS_KEYS.firstNotNullOfOrNull { key ->
                    map[key]?.firstOrNull { it.isNotBlank() }
                }
            }
        }.getOrNull()
    }

    fun formatLabel(mime: String?, name: String?, hint: String?): String {
        if (hint != null) return hint
        val m = mime?.lowercase().orEmpty()
        val ext = name?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return when {
            m.contains("flac") || ext == "flac" -> "FLAC"
            m.contains("mpeg") || m.contains("mp3") || ext == "mp3" -> "MP3"
            m.contains("wav") || ext == "wav" -> "WAV"
            m.contains("opus") || ext == "opus" -> "OPUS"
            m.contains("ogg") || ext == "ogg" || ext == "oga" -> "OGG"
            m.contains("aiff") || ext == "aiff" || ext == "aif" || ext == "aifc" -> "AIFF"
            m.contains("mp4") || m.contains("m4a") || m.contains("aac") || ext == "m4a" || ext == "m4b" || ext == "aac" -> "AAC"
            m.contains("wma") || ext == "wma" -> "WMA"
            else -> ext.uppercase().takeIf { it.isNotBlank() } ?: "AUDIO"
        }
    }

    private fun readWithTagLib(context: Context, uri: Uri, readPicture: Boolean): AudioTags? = runCatching {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            val metadata = TagLib.getMetadata(descriptor.dup().detachFd(), readPicture) ?: return@use null
            val properties = TagLib.getAudioProperties(descriptor.dup().detachFd(), AudioPropertiesReadStyle.Average)
            val header = readHeader(descriptor)?.let { probeHeader(it) }
            val map = metadata.propertyMap
            val artist = map.joinedTags("ARTIST")
            val albumArtist = map.joinedTags("ALBUMARTIST")
            AudioTags(
                title = map.firstTag("TITLE"),
                artist = artist.ifBlank { albumArtist },
                album = map.firstTag("ALBUM").orEmpty(),
                albumArtist = albumArtist,
                genre = map.joinedTags("GENRE"),
                trackNumber = parseLeadingNumber(map.firstTag("TRACKNUMBER")),
                discNumber = parseLeadingNumber(map.firstTag("DISCNUMBER")),
                year = parseYear(map.firstTag("DATE")) ?: parseYear(map.firstTag("ORIGINALDATE")) ?: parseYear(map.firstTag("YEAR")),
                durationMs = properties?.length?.toLong()?.coerceAtLeast(0L) ?: 0L,
                sampleRate = properties?.sampleRate?.takeIf { it > 0 },
                bitDepth = header?.bitDepth,
                mimeType = null,
                formatHint = header?.formatHint,
                picture = if (readPicture) pickPicture(metadata.pictures) else null
            )
        }
    }.getOrNull()

    private fun readWithRetriever(context: Context, uri: Uri, readPicture: Boolean): AudioTags? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            fun text(key: Int): String? = retriever.extractMetadata(key)?.trim()?.takeIf { it.isNotBlank() }
            val albumArtist = text(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST).orEmpty()
            val bitDepth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                text(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.toIntOrNull()?.takeIf { it > 0 }
            } else {
                null
            }
            val sampleRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                text(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.takeIf { it > 0 }
            } else {
                null
            }
            AudioTags(
                title = text(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = text(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: albumArtist,
                album = text(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty(),
                albumArtist = albumArtist,
                genre = text(MediaMetadataRetriever.METADATA_KEY_GENRE).orEmpty(),
                trackNumber = parseLeadingNumber(text(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)),
                discNumber = parseLeadingNumber(text(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)),
                year = parseYear(text(MediaMetadataRetriever.METADATA_KEY_YEAR))
                    ?: parseYear(text(MediaMetadataRetriever.METADATA_KEY_DATE)),
                durationMs = text(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                mimeType = text(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                formatHint = null,
                picture = if (readPicture) retriever.embeddedPicture else null
            )
        } catch (e: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun PropertyMap.tagValues(key: String): List<String> =
        this[key]?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()

    private fun PropertyMap.firstTag(key: String): String? = tagValues(key).firstOrNull()

    private fun PropertyMap.joinedTags(key: String): String = tagValues(key).distinct().joinToString(", ")

    private fun pickPicture(pictures: Array<Picture>): ByteArray? {
        val picture = pictures.firstOrNull { it.pictureType == "Front Cover" } ?: pictures.firstOrNull()
        return picture?.data?.takeIf { it.isNotEmpty() }
    }

    private fun parseLeadingNumber(raw: String?): Int? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return value.substringBefore('/').trim().toIntOrNull()
    }

    private fun parseYear(raw: String?): Int? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return YEAR_PATTERN.find(value)?.value?.toIntOrNull()?.takeIf { it > 0 }
    }

    private fun readHeader(descriptor: ParcelFileDescriptor): ByteArray? = runCatching {
        ParcelFileDescriptor.AutoCloseInputStream(descriptor.dup()).use { stream ->
            val channel = stream.channel
            val buffer = ByteBuffer.allocate(HEADER_BYTES)
            var total = 0
            while (buffer.hasRemaining()) {
                val read = channel.read(buffer, total.toLong())
                if (read <= 0) break
                total += read
            }
            buffer.array().copyOf(total)
        }
    }.getOrNull()

    private fun probeHeader(header: ByteArray): HeaderInfo? =
        probeFlac(header) ?: probeWav(header) ?: probeAiff(header) ?: probeAlac(header)

    private fun probeFlac(header: ByteArray): HeaderInfo? {
        val start = indexOf(header, "fLaC".toByteArray(), 0)
        if (start < 0 || (start > 0 && !matches(header, 0, "ID3"))) return null
        val info = start + 18
        if (info + 4 > header.size) return null
        val packed = ((header[info + 2].toInt() and 0x01) shl 4) or ((header[info + 3].toInt() and 0xF0) ushr 4)
        return HeaderInfo(bitDepth = (packed + 1).takeIf { it in 4..32 }, formatHint = null)
    }

    private fun probeWav(header: ByteArray): HeaderInfo? {
        if (header.size < 12 || !matches(header, 0, "RIFF") || !matches(header, 8, "WAVE")) return null
        var offset = 12
        while (offset + 8 <= header.size) {
            val size = littleEndianInt(header, offset + 4)
            if (matches(header, offset, "fmt ")) {
                val bits = offset + 8 + 14
                if (bits + 2 > header.size) return null
                val value = littleEndianShort(header, bits)
                return HeaderInfo(bitDepth = value.takeIf { it in 4..64 }, formatHint = null)
            }
            if (size < 0) return null
            offset += 8 + size + (size and 1)
        }
        return null
    }

    private fun probeAiff(header: ByteArray): HeaderInfo? {
        if (header.size < 12 || !matches(header, 0, "FORM")) return null
        if (!matches(header, 8, "AIFF") && !matches(header, 8, "AIFC")) return null
        var offset = 12
        while (offset + 8 <= header.size) {
            val size = bigEndianInt(header, offset + 4)
            if (matches(header, offset, "COMM")) {
                val bits = offset + 8 + 6
                if (bits + 2 > header.size) return null
                val value = bigEndianShort(header, bits)
                return HeaderInfo(bitDepth = value.takeIf { it in 4..64 }, formatHint = null)
            }
            if (size < 0) return null
            offset += 8 + size + (size and 1)
        }
        return null
    }

    private fun probeAlac(header: ByteArray): HeaderInfo? {
        if (header.size < 12 || !matches(header, 4, "ftyp")) return null
        val start = indexOf(header, "alac".toByteArray(), 8)
        if (start < 4) return null
        val sampleSize = start + 22
        val bits = if (sampleSize + 2 <= header.size) bigEndianShort(header, sampleSize).takeIf { it in 8..32 } else null
        return HeaderInfo(bitDepth = bits, formatHint = "ALAC")
    }

    private fun matches(data: ByteArray, offset: Int, text: String): Boolean {
        if (offset < 0 || offset + text.length > data.size) return false
        for (i in text.indices) {
            if (data[offset + i] != text[i].code.toByte()) return false
        }
        return true
    }

    private fun indexOf(data: ByteArray, pattern: ByteArray, from: Int): Int {
        if (pattern.isEmpty()) return -1
        var i = from.coerceAtLeast(0)
        val last = data.size - pattern.size
        while (i <= last) {
            var found = true
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
            i++
        }
        return -1
    }

    private fun littleEndianInt(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)

    private fun littleEndianShort(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)

    private fun bigEndianInt(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 24) or
            ((data[offset + 1].toInt() and 0xFF) shl 16) or
            ((data[offset + 2].toInt() and 0xFF) shl 8) or
            (data[offset + 3].toInt() and 0xFF)

    private fun bigEndianShort(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
}
