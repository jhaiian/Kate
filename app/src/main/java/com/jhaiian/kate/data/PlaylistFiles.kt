package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream
import java.net.URLDecoder

object PlaylistFiles {
    const val EXPORT_MIME_TYPE = "audio/x-mpegurl"
    const val EXPORT_EXTENSION = "m3u8"
    private const val PRIMARY_VOLUME = "primary"
    private const val PRIMARY_ROOT = "/storage/emulated/0/"
    private const val VOLUME_ROOT = "/storage/"

    class ImportedPlaylist(val name: String?, val trackIds: List<String>, val entryCount: Int)

    fun read(context: Context, uri: Uri, library: List<Track>): ImportedPlaylist? = runCatching {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } ?: return@runCatching null
        val entries = text.removePrefix("﻿")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
        val name = DocumentFile.fromSingleUri(context, uri)?.name
            ?.substringBeforeLast('.')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        ImportedPlaylist(name, match(entries, library), entries.size)
    }.getOrNull()

    fun write(context: Context, uri: Uri, tracks: List<Track>): Boolean = runCatching {
        val resolver = context.contentResolver
        val stream: OutputStream = runCatching { resolver.openOutputStream(uri, "wt") }.getOrNull()
            ?: resolver.openOutputStream(uri)
            ?: return@runCatching false
        stream.use { it.write(build(tracks).toByteArray()) }
        true
    }.getOrDefault(false)

    private fun build(tracks: List<Track>): String = buildString {
        append("#EXTM3U\n")
        tracks.forEach { track ->
            val label = listOf(track.artist, track.title).filter { it.isNotBlank() }.joinToString(" - ")
            append("#EXTINF:").append(track.durationMs / 1000).append(',').append(label).append('\n')
            append(exportPath(track)).append('\n')
        }
    }

    private fun exportPath(track: Track): String {
        val volume = track.path.substringBefore(':', "")
        val relative = track.path.substringAfter(':', "")
        if (volume.isEmpty() || relative.isEmpty()) return track.filename.ifBlank { track.title }
        return if (volume == PRIMARY_VOLUME) PRIMARY_ROOT + relative else "$VOLUME_ROOT$volume/$relative"
    }

    private fun match(entries: List<String>, library: List<Track>): List<String> {
        val byName = library.groupBy { it.filename.lowercase() }
        return entries.mapNotNull { entry ->
            val path = normalize(entry)
            val candidates = byName[path.substringAfterLast('/').lowercase()].orEmpty()
            when (candidates.size) {
                0 -> null
                1 -> candidates.first().id
                else -> candidates.maxBy { matchingSegments(it.path.substringAfter(':'), path) }.id
            }
        }
    }

    private fun normalize(entry: String): String {
        val slashed = entry.replace('\\', '/').removePrefix("file://")
        return if ('%' in slashed) runCatching { URLDecoder.decode(slashed, "UTF-8") }.getOrDefault(slashed) else slashed
    }

    private fun matchingSegments(trackPath: String, entryPath: String): Int {
        val trackParts = trackPath.lowercase().split('/').filter { it.isNotEmpty() }.asReversed()
        val entryParts = entryPath.lowercase().split('/').filter { it.isNotEmpty() }.asReversed()
        return trackParts.zip(entryParts).takeWhile { (a, b) -> a == b }.size
    }
}
