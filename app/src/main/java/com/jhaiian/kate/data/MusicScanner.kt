package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

enum class ScanPhase { FINDING, READING, SAVING }

data class ScanProgress(val phase: ScanPhase, val found: Int, val processed: Int, val total: Int)

object MusicScanner {
    private val AUDIO_EXTENSIONS = setOf(
        "mp3", "flac", "wav", "ogg", "oga", "m4a", "m4b", "aac", "wma", "opus", "alac",
        "aiff", "aif", "aifc", "ape", "dsf"
    )
    private val PLAYLIST_EXTENSIONS = setOf("m3u", "m3u8", "pls", "wpl", "xspf", "cue")
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    private val COVER_NAMES = listOf("cover", "folder", "front", "album", "albumart", "albumartlarge", "albumartsmall")
    private const val NOMEDIA = ".nomedia"
    private const val LIST_CONCURRENCY = 4
    private const val MAX_COVER_BYTES = 20L * 1024 * 1024
    private val PROJECTION = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    class Tracker {
        @Volatile
        var phase: ScanPhase = ScanPhase.FINDING
            internal set

        @Volatile
        var collectTracks: Boolean = false

        @Volatile
        internal var total: Int = 0
        internal val found = AtomicInteger()
        internal val processed = AtomicInteger()
        private val pending = ConcurrentLinkedQueue<Track>()

        fun progress(): ScanProgress = ScanProgress(phase, found.get(), processed.get(), total)

        internal fun emit(track: Track) {
            if (collectTracks) pending.add(track)
        }

        fun drainTracks(): List<Track> {
            val drained = ArrayList<Track>()
            while (true) {
                drained.add(pending.poll() ?: break)
            }
            return drained
        }
    }

    data class ScanResult(val tracks: List<Track>, val cacheEntries: List<LibraryTrackCache.Entry>)

    private class DocumentRow(
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val size: Long,
        val lastModified: Long
    ) {
        val isDirectory: Boolean get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
    }

    private class AudioFile(
        val uri: Uri,
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val size: Long,
        val lastModified: Long,
        val coverUri: Uri?
    )

    suspend fun scan(
        context: Context,
        folders: List<MusicFolder>,
        cacheIndex: Map<String, LibraryTrackCache.Entry>,
        tracker: Tracker
    ): ScanResult = withContext(Dispatchers.IO) {
        tracker.phase = ScanPhase.FINDING
        val files = discover(context, folders, tracker)
        tracker.total = files.size
        tracker.phase = ScanPhase.READING

        val coverKeys = ConcurrentHashMap<String, String>()
        val concurrency = minOf(6, maxOf(2, Runtime.getRuntime().availableProcessors()))
        val semaphore = Semaphore(concurrency)
        val resolved = files.map { file ->
            async {
                semaphore.withPermit {
                    val result = resolveTrack(context, file, cacheIndex[file.uri.toString()], coverKeys)
                    tracker.processed.incrementAndGet()
                    if (result != null) tracker.emit(result.first)
                    result
                }
            }
        }.awaitAll().filterNotNull()

        ScanResult(
            tracks = resolved.map { it.first },
            cacheEntries = resolved.map { it.second }
        )
    }

    private suspend fun discover(context: Context, folders: List<MusicFolder>, tracker: Tracker): List<AudioFile> {
        val seen = ConcurrentHashMap.newKeySet<String>()
        val output = ConcurrentLinkedQueue<AudioFile>()
        val permits = Semaphore(LIST_CONCURRENCY)
        coroutineScope {
            for (folder in folders) {
                val treeUri = folder.uri
                val rootId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: continue
                launch { walk(context, treeUri, rootId, true, permits, seen, output, tracker) }
            }
        }
        return output.toList()
    }

    private suspend fun walk(
        context: Context,
        treeUri: Uri,
        documentId: String,
        isRoot: Boolean,
        permits: Semaphore,
        seen: MutableSet<String>,
        output: ConcurrentLinkedQueue<AudioFile>,
        tracker: Tracker
    ) {
        val authority = treeUri.authority.orEmpty()
        if (!seen.add("dir:$authority/$documentId")) return
        val rows = permits.withPermit { queryChildren(context, treeUri, documentId) } ?: return
        if (!isRoot && rows.any { it.name.equals(NOMEDIA, ignoreCase = true) }) return
        val cover = pickCover(rows)?.let { DocumentsContract.buildDocumentUriUsingTree(treeUri, it.documentId) }
        for (row in rows) {
            if (row.isDirectory || !isAudio(row.name, row.mimeType)) continue
            if (!seen.add("file:$authority/${row.documentId}")) continue
            output.add(
                AudioFile(
                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, row.documentId),
                    documentId = row.documentId,
                    name = row.name,
                    mimeType = row.mimeType,
                    size = row.size,
                    lastModified = row.lastModified,
                    coverUri = cover
                )
            )
            tracker.found.incrementAndGet()
        }
        coroutineScope {
            for (row in rows) {
                if (!row.isDirectory || row.name.startsWith(".")) continue
                launch { walk(context, treeUri, row.documentId, false, permits, seen, output, tracker) }
            }
        }
    }

    private fun queryChildren(context: Context, treeUri: Uri, parentId: String): List<DocumentRow>? = runCatching {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        context.contentResolver.query(childrenUri, PROJECTION, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            if (idColumn < 0) return@use null
            val rows = ArrayList<DocumentRow>(cursor.count.coerceAtLeast(0))
            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn) ?: continue
                rows.add(
                    DocumentRow(
                        documentId = id,
                        name = if (nameColumn >= 0) cursor.getString(nameColumn).orEmpty() else "",
                        mimeType = if (mimeColumn >= 0) cursor.getString(mimeColumn) else null,
                        size = if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) cursor.getLong(sizeColumn) else 0L,
                        lastModified = if (modifiedColumn >= 0 && !cursor.isNull(modifiedColumn)) cursor.getLong(modifiedColumn) else 0L
                    )
                )
            }
            rows
        }
    }.getOrNull()

    private fun extensionOf(name: String): String = name.substringAfterLast('.', "").lowercase()

    private fun isAudio(name: String, mimeType: String?): Boolean {
        val ext = extensionOf(name)
        if (ext in PLAYLIST_EXTENSIONS) return false
        if (ext in AUDIO_EXTENSIONS) return true
        val mime = mimeType?.lowercase() ?: return false
        return mime.startsWith("audio/") && !mime.contains("mpegurl") && !mime.contains("scpls")
    }

    private fun pickCover(rows: List<DocumentRow>): DocumentRow? {
        var best: DocumentRow? = null
        var bestRank = Int.MAX_VALUE
        for (row in rows) {
            if (row.isDirectory) continue
            val ext = extensionOf(row.name)
            val isImage = ext in IMAGE_EXTENSIONS || row.mimeType?.startsWith("image/") == true
            if (!isImage) continue
            val base = row.name.substringBeforeLast('.').lowercase()
            val rank = COVER_NAMES.indexOf(base).takeIf { it >= 0 }
                ?: if (base.startsWith("albumart")) COVER_NAMES.size else continue
            if (rank < bestRank) {
                best = row
                bestRank = rank
            }
        }
        return best
    }

    private fun coverArtKey(context: Context, coverUri: Uri, coverKeys: ConcurrentHashMap<String, String>): String? {
        val key = coverKeys.computeIfAbsent(coverUri.toString()) {
            val bytes = runCatching {
                context.contentResolver.openInputStream(coverUri)?.use { stream ->
                    val data = stream.readBytes()
                    data.takeIf { it.size <= MAX_COVER_BYTES }
                }
            }.getOrNull()
            LibraryTrackCache.saveArt(context, bytes).orEmpty()
        }
        return key.ifEmpty { null }
    }

    private fun resolveTrack(
        context: Context,
        file: AudioFile,
        cached: LibraryTrackCache.Entry?,
        coverKeys: ConcurrentHashMap<String, String>
    ): Pair<Track, LibraryTrackCache.Entry>? {
        if (cached != null &&
            cached.version >= LibraryTrackCache.CURRENT_VERSION &&
            cached.lastModified == file.lastModified &&
            cached.sizeBytes == file.size
        ) {
            val entry = if ((cached.artKey == null || ScanFilters.preferFolderArt) && file.coverUri != null) {
                coverArtKey(context, file.coverUri, coverKeys)?.let { cached.copy(artKey = it, hasArt = true) } ?: cached
            } else {
                cached
            }
            val track = LibraryTrackCache.toTrack(context, entry) ?: return null
            return track to entry
        }
        val dateAddedMs = cached?.dateAddedMs ?: System.currentTimeMillis()
        return extractTrack(context, file, dateAddedMs, coverKeys)
    }

    private fun extractTrack(
        context: Context,
        file: AudioFile,
        dateAddedMs: Long,
        coverKeys: ConcurrentHashMap<String, String>
    ): Pair<Track, LibraryTrackCache.Entry>? {
        val tags = AudioTagReader.read(context, file.uri, readPicture = true)
        if (tags == null && extensionOf(file.name) !in AUDIO_EXTENSIONS) return null
        val fallbackTitle = file.name.substringBeforeLast('.').ifBlank { file.name }
        val title = tags?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
        val folderArt = if (ScanFilters.preferFolderArt) file.coverUri?.let { coverArtKey(context, it, coverKeys) } else null
        val artKey = folderArt
            ?: LibraryTrackCache.saveArt(context, tags?.picture)
            ?: file.coverUri?.let { coverArtKey(context, it, coverKeys) }
        val uriString = file.uri.toString()
        val entry = LibraryTrackCache.Entry(
            uri = uriString,
            lastModified = file.lastModified,
            sizeBytes = file.size,
            title = title,
            artist = tags?.artist.orEmpty(),
            album = tags?.album.orEmpty(),
            durationMs = tags?.durationMs ?: 0L,
            format = AudioTagReader.formatLabel(tags?.mimeType ?: file.mimeType, file.name, tags?.formatHint),
            bitDepth = tags?.bitDepth,
            hasArt = artKey != null,
            trackNumber = tags?.trackNumber,
            discNumber = tags?.discNumber,
            year = tags?.year,
            filename = file.name.ifBlank { file.uri.lastPathSegment ?: fallbackTitle },
            path = file.documentId,
            dateAddedMs = dateAddedMs,
            artKey = artKey,
            albumArtist = tags?.albumArtist.orEmpty(),
            genre = tags?.genre.orEmpty(),
            sampleRate = tags?.sampleRate
        )
        val track = LibraryTrackCache.toTrack(context, entry) ?: return null
        return track to entry
    }
}
