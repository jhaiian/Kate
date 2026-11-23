package com.jhaiian.kate.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import kotlin.math.roundToInt

object LibraryTrackCache {
    const val CURRENT_VERSION = 2

    data class Entry(
        val uri: String,
        val lastModified: Long,
        val sizeBytes: Long,
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val format: String,
        val bitDepth: Int?,
        val hasArt: Boolean,
        val trackNumber: Int?,
        val discNumber: Int?,
        val year: Int?,
        val filename: String,
        val path: String,
        val dateAddedMs: Long,
        val artKey: String?,
        val albumArtist: String = "",
        val genre: String = "",
        val sampleRate: Int? = null,
        val version: Int = CURRENT_VERSION
    )

    private const val CACHE_FILE_NAME = "library_scan_cache.tsv"
    private const val ART_DIR_NAME = "library_album_art"
    private const val ENTRY_DELIMITER = "\u001D"
    private const val FIELD_DELIMITER = "\u001E"
    private const val ART_SIZE_PX = 1024
    private const val ART_JPEG_QUALITY = 88
    private const val ART_KEY_SUFFIX = "s1024"
    private val LEGACY_UNKNOWN_VALUES = setOf("Unknown Artist", "Unknown Album")

    private fun cacheFile(context: Context) = File(context.filesDir, CACHE_FILE_NAME)

    private fun artDir(context: Context): File {
        val dir = File(context.filesDir, ART_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun artFile(context: Context, artKey: String): File = File(artDir(context), "$artKey.jpg")

    private fun computeArtKey(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun legacyText(value: String, version: Int): String =
        if (version < CURRENT_VERSION && value in LEGACY_UNKNOWN_VALUES) "" else value

    fun loadIndex(context: Context): Map<String, Entry> {
        val raw = runCatching { cacheFile(context).takeIf { it.exists() }?.readText() }.getOrNull()
        if (raw.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<String, Entry>()
        raw.split(ENTRY_DELIMITER).forEach { record ->
            val parts = record.split(FIELD_DELIMITER)
            if (parts.size < 17) return@forEach
            runCatching {
                val version = parts.getOrNull(20)?.toIntOrNull() ?: 0
                val entry = Entry(
                    uri = parts[0],
                    lastModified = parts[1].toLong(),
                    sizeBytes = parts[2].toLong(),
                    title = parts[3],
                    artist = legacyText(parts[4], version),
                    album = legacyText(parts[5], version),
                    durationMs = parts[6].toLong(),
                    format = parts[7],
                    bitDepth = parts[8].toIntOrNull(),
                    hasArt = parts[9] == "1",
                    trackNumber = parts[10].toIntOrNull(),
                    discNumber = parts[11].toIntOrNull(),
                    year = parts[12].toIntOrNull(),
                    filename = parts[13],
                    path = parts[14],
                    dateAddedMs = parts[15].toLongOrNull() ?: 0L,
                    artKey = parts[16].takeIf { it.isNotEmpty() },
                    albumArtist = parts.getOrNull(17).orEmpty(),
                    genre = parts.getOrNull(18).orEmpty(),
                    sampleRate = parts.getOrNull(19)?.toIntOrNull(),
                    version = version
                )
                result[entry.uri] = entry
            }
        }
        return result
    }

    fun loadTracks(context: Context): List<Track> =
        loadIndex(context).values.mapNotNull { toTrack(context, it) }

    fun toTrack(context: Context, entry: Entry): Track? {
        val uri = runCatching { Uri.parse(entry.uri) }.getOrNull() ?: return null
        return Track(
            id = entry.uri,
            uri = uri,
            title = entry.title,
            artist = entry.artist,
            album = entry.album,
            durationMs = entry.durationMs,
            sizeBytes = entry.sizeBytes,
            format = entry.format,
            bitDepth = entry.bitDepth,
            hasArt = entry.hasArt,
            trackNumber = entry.trackNumber,
            discNumber = entry.discNumber,
            year = entry.year,
            filename = entry.filename,
            path = entry.path,
            dateAddedMs = entry.dateAddedMs,
            artKey = entry.artKey,
            albumArtist = entry.albumArtist,
            genre = entry.genre,
            sampleRate = entry.sampleRate
        )
    }

    fun artFileUriOrNull(context: Context, artKey: String?): Uri? {
        if (artKey == null) return null
        val file = artFile(context, artKey)
        return if (file.exists()) Uri.fromFile(file) else null
    }

    fun saveArt(context: Context, imageBytes: ByteArray?): String? {
        if (imageBytes == null || imageBytes.isEmpty()) return null
        val artKey = computeArtKey(imageBytes) + ART_KEY_SUFFIX
        val file = artFile(context, artKey)
        if (file.exists()) return artKey
        val thumbnail = encodeThumbnail(imageBytes) ?: return null
        val temp = File(file.parentFile, "${file.name}.${System.nanoTime()}.tmp")
        return runCatching {
            temp.outputStream().use { it.write(thumbnail) }
            if (!temp.renameTo(file)) temp.delete()
            if (file.exists()) artKey else null
        }.getOrElse {
            temp.delete()
            null
        }
    }

    private fun encodeThumbnail(bytes: ByteArray): ByteArray? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= ART_SIZE_PX && bounds.outHeight / (sampleSize * 2) >= ART_SIZE_PX) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@runCatching null
        val scale = ART_SIZE_PX.toFloat() / maxOf(decoded.width, decoded.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).roundToInt().coerceAtLeast(1),
                (decoded.height * scale).roundToInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, ART_JPEG_QUALITY, output)
        if (scaled !== decoded) scaled.recycle()
        decoded.recycle()
        output.toByteArray()
    }.getOrNull()

    fun save(context: Context, entries: List<Entry>) {
        val keepNames = entries.mapNotNull { it.artKey }.map { "$it.jpg" }.toSet()
        runCatching {
            artDir(context).listFiles()?.forEach { file ->
                if (file.name !in keepNames) file.delete()
            }
        }
        val raw = entries.joinToString(ENTRY_DELIMITER) { entry ->
            listOf(
                entry.uri,
                entry.lastModified.toString(),
                entry.sizeBytes.toString(),
                entry.title,
                entry.artist,
                entry.album,
                entry.durationMs.toString(),
                entry.format,
                entry.bitDepth?.toString().orEmpty(),
                if (entry.hasArt) "1" else "0",
                entry.trackNumber?.toString().orEmpty(),
                entry.discNumber?.toString().orEmpty(),
                entry.year?.toString().orEmpty(),
                entry.filename,
                entry.path,
                entry.dateAddedMs.toString(),
                entry.artKey.orEmpty(),
                entry.albumArtist,
                entry.genre,
                entry.sampleRate?.toString().orEmpty(),
                entry.version.toString()
            ).joinToString(FIELD_DELIMITER)
        }
        runCatching {
            val file = cacheFile(context)
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(raw)
            if (!temp.renameTo(file)) {
                file.writeText(raw)
                temp.delete()
            }
        }
    }

    fun clear(context: Context) {
        runCatching { cacheFile(context).delete() }
        runCatching { artDir(context).listFiles()?.forEach { it.delete() } }
    }
}
