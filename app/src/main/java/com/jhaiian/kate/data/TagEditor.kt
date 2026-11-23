package com.jhaiian.kate.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.jhaiian.kate.playback.PlayerController
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

enum class TagField(val key: String, val aliases: Set<String> = emptySet()) {
    TITLE("TITLE"),
    ARTIST("ARTIST"),
    ALBUM("ALBUM"),
    ALBUM_ARTIST("ALBUMARTIST", setOf("ALBUM ARTIST")),
    GENRE("GENRE"),
    YEAR("DATE", setOf("YEAR")),
    TRACK("TRACKNUMBER"),
    DISC("DISCNUMBER"),
    COMPOSER("COMPOSER"),
    COMMENT("COMMENT"),
    LYRICS("LYRICS", setOf("UNSYNCEDLYRICS", "UNSYNCED LYRICS", "SYNCEDLYRICS"));

    val multiLine: Boolean get() = this == LYRICS || this == COMMENT
    val numeric: Boolean get() = this == YEAR || this == TRACK || this == DISC
}

class TagSnapshot(val values: Map<TagField, String>, val cover: ByteArray?)

sealed interface CoverEdit {
    data object Keep : CoverEdit
    data object Remove : CoverEdit
    class Replace(val bytes: ByteArray) : CoverEdit
}

object TagEditor {
    private const val COVER_MAX_PX = 1400
    private const val COVER_QUALITY = 92

    suspend fun read(context: Context, uri: Uri): TagSnapshot? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                val metadata = TagLib.getMetadata(descriptor.dup().detachFd(), true) ?: return@use null
                val upper = HashMap<String, List<String>>()
                metadata.propertyMap.forEach { (key, value) ->
                    val clean = value.map { it.trim() }.filter { it.isNotEmpty() }
                    if (clean.isNotEmpty()) upper.putIfAbsent(key.uppercase(), clean)
                }
                val values = TagField.entries.associateWith { field ->
                    val found = (listOf(field.key) + field.aliases).firstNotNullOfOrNull { upper[it] }
                    when {
                        found == null -> ""
                        field.multiLine -> found.first()
                        else -> found.distinct().joinToString(", ")
                    }
                }
                val picture = metadata.pictures.firstOrNull { it.pictureType == "Front Cover" } ?: metadata.pictures.firstOrNull()
                TagSnapshot(values, picture?.data?.takeIf { it.isNotEmpty() })
            }
        }.getOrNull()
    }

    suspend fun save(context: Context, track: Track, changes: Map<TagField, String>, cover: CoverEdit): Boolean {
        val appContext = context.applicationContext
        if (!write(appContext, track.uri, changes, cover)) return false
        val updated = refreshed(appContext, track, cover)
        withContext(Dispatchers.Main) {
            LibraryStore.applyEdits(appContext, listOf(updated))
            PlayerController.refreshTracks(appContext, listOf(updated))
            changes[TagField.LYRICS]?.let { lyrics -> LyricsRepository.update(updated.id, lyrics.trim()) }
        }
        return true
    }

    private suspend fun write(context: Context, uri: Uri, changes: Map<TagField, String>, cover: CoverEdit): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
                    var ok = true
                    if (changes.isNotEmpty()) {
                        val metadata = TagLib.getMetadata(descriptor.dup().detachFd(), false) ?: return@use false
                        val map = HashMap(metadata.propertyMap)
                        changes.forEach { (field, value) ->
                            val names = field.aliases + field.key
                            map.keys.filter { it.uppercase() in names }.forEach { map.remove(it) }
                            val clean = value.trim()
                            if (clean.isNotEmpty()) map[field.key] = arrayOf(clean)
                        }
                        ok = TagLib.savePropertyMap(descriptor.dup().detachFd(), map)
                    }
                    if (ok && cover !is CoverEdit.Keep) {
                        val pictures = if (cover is CoverEdit.Replace) {
                            arrayOf(Picture(cover.bytes, "", "Front Cover", "image/jpeg"))
                        } else {
                            emptyArray()
                        }
                        ok = TagLib.savePictures(descriptor.dup().detachFd(), pictures)
                    }
                    ok
                } ?: false
            }.getOrDefault(false)
        }

    private suspend fun refreshed(context: Context, original: Track, cover: CoverEdit): Track = withContext(Dispatchers.IO) {
        val tags = AudioTagReader.read(context, original.uri, readPicture = true) ?: return@withContext original
        val embedded = LibraryTrackCache.saveArt(context, tags.picture)
        val artKey = when (cover) {
            CoverEdit.Keep -> embedded ?: original.artKey
            else -> embedded
        }
        val fallbackTitle = original.filename.substringBeforeLast('.').ifBlank { original.title }
        original.copy(
            title = tags.title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
            artist = tags.artist,
            album = tags.album,
            albumArtist = tags.albumArtist,
            genre = tags.genre,
            trackNumber = tags.trackNumber,
            discNumber = tags.discNumber,
            year = tags.year,
            hasArt = artKey != null,
            artKey = artKey
        )
    }

    suspend fun prepareCover(bytes: ByteArray): ByteArray? = withContext(Dispatchers.Default) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= COVER_MAX_PX && bounds.outHeight / (sample * 2) >= COVER_MAX_PX) sample *= 2
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
                ?: return@runCatching null
            val rotation = runCatching {
                when (ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }.getOrDefault(0f)
            val scale = (COVER_MAX_PX.toFloat() / maxOf(decoded.width, decoded.height)).coerceAtMost(1f)
            val matrix = Matrix().apply {
                if (scale < 1f) postScale(scale, scale)
                if (rotation != 0f) postRotate(rotation)
            }
            val output = if (matrix.isIdentity) {
                decoded
            } else {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            }
            val stream = ByteArrayOutputStream()
            output.compress(Bitmap.CompressFormat.JPEG, COVER_QUALITY, stream)
            if (output !== decoded) output.recycle()
            decoded.recycle()
            stream.toByteArray()
        }.getOrNull()
    }

    fun decodePreview(bytes: ByteArray, targetPx: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetPx && bounds.outHeight / (sample * 2) >= targetPx) sample *= 2
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()

    fun sizeLabel(bytes: ByteArray): String? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        return "${bounds.outWidth}×${bounds.outHeight}"
    }
}
