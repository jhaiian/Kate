package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

data class LyricWord(val startMs: Long, val endMs: Long, val charStart: Int, val charEnd: Int)

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList(),
    val voice: Int = VOICE_MAIN
) {
    val hasWordTiming: Boolean get() = words.isNotEmpty()

    companion object {
        const val VOICE_MAIN = 0
        const val VOICE_SECOND = 1
        const val VOICE_BACKGROUND = 2
    }
}

data class Lyrics(val lines: List<LyricLine>, val synced: Boolean, val source: String? = null) {
    val hasWordTiming: Boolean = lines.any { it.hasWordTiming }
    val duet: Boolean = lines.any { it.voice == LyricLine.VOICE_SECOND }

    fun nextLineStartMs(index: Int): Long? = lines.getOrNull(index + 1)?.timeMs

    fun activeIndex(positionMs: Long): Int {
        if (!synced || lines.isEmpty()) return -1
        var low = 0
        var high = lines.size - 1
        var result = -1
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (lines[middle].timeMs <= positionMs) {
                result = middle
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return result
    }
}

object LrcParser {
    private const val UNKNOWN_END = -1L
    private const val WORD_MS_PER_CHAR = 120L
    private const val WORD_MIN_MS = 400L
    private const val WORD_MAX_MS = 2_500L
    private val TimeTag = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val InlineTag = Regex("""<(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?>|\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val OffsetTag = Regex("""^\[offset:\s*([+-]?\d+)\s*]$""", RegexOption.IGNORE_CASE)
    private val SourceTag = Regex("""^\[source\s*:\s*(.*?)\s*]$""", RegexOption.IGNORE_CASE)
    private val MetadataTag = Regex("""^\[[A-Za-z#][A-Za-z0-9#_ ]*:.*]$""")
    private val WordTag = Regex("""<\d{1,3}:\d{1,2}(?:[.:]\d{1,3})?>""")
    private val VoiceTag = Regex("""^(\s*(?:<\d{1,3}:\d{1,2}(?:[.:]\d{1,3})?>)?)\s*(?:([vV]\d{1,2}|[bB][gG])|([MFD]))\s*:\s*""")

    private class Content(val text: String, val words: List<LyricWord>)

    private class Voiced(val content: String, val voice: Int)

    private fun splitVoice(raw: String): Voiced {
        val match = VoiceTag.find(raw) ?: return Voiced(raw, LyricLine.VOICE_MAIN)
        val tag = match.groupValues[2].lowercase()
        val letter = match.groupValues[3]
        val voice = when {
            tag == "bg" -> LyricLine.VOICE_BACKGROUND
            tag.startsWith("v") -> {
                val number = tag.drop(1).toIntOrNull() ?: 1
                if (number % 2 == 0) LyricLine.VOICE_SECOND else LyricLine.VOICE_MAIN
            }
            letter == "F" -> LyricLine.VOICE_SECOND
            else -> LyricLine.VOICE_MAIN
        }
        return Voiced(match.groupValues[1] + raw.substring(match.range.last + 1), voice)
    }

    fun parse(text: String): Lyrics? {
        var offsetMs = 0L
        var source: String? = null
        val synced = ArrayList<LyricLine>()
        val plain = ArrayList<String>()
        for (raw in text.lines()) {
            val line = raw.trim().trimStart('\uFEFF')
            val offsetMatch = OffsetTag.matchEntire(line)
            if (offsetMatch != null) {
                offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                continue
            }
            val sourceMatch = SourceTag.matchEntire(line)
            if (sourceMatch != null) {
                source = sourceMatch.groupValues[1].takeIf { it.isNotEmpty() }
                continue
            }
            val times = ArrayList<Long>()
            var position = 0
            while (true) {
                val match = TimeTag.find(line, position) ?: break
                if (match.range.first != position) break
                times.add(toMillis(match.groupValues[1], match.groupValues[2], match.groupValues[3]))
                position = match.range.last + 1
            }
            if (times.isNotEmpty()) {
                val base = times.first()
                val voiced = splitVoice(line.substring(position))
                val content = parseContent(voiced.content, base)
                times.forEach { time ->
                    val shift = time - base
                    val words = if (shift == 0L) {
                        content.words
                    } else {
                        content.words.map { word ->
                            word.copy(
                                startMs = word.startMs + shift,
                                endMs = if (word.endMs == UNKNOWN_END) UNKNOWN_END else word.endMs + shift
                            )
                        }
                    }
                    synced.add(LyricLine(time, content.text, words, voiced.voice))
                }
            } else if (!MetadataTag.matches(line)) {
                plain.add(WordTag.replace(splitVoice(line).content, "").trim())
            }
        }
        if (synced.isNotEmpty()) {
            val sorted = synced.map { it.shiftedBy(-offsetMs) }.sortedBy { it.timeMs }
            val finished = sorted.mapIndexed { index, line ->
                if (line.words.isEmpty()) line else line.copy(words = finishWords(line.words, sorted.getOrNull(index + 1)?.timeMs))
            }
            return Lyrics(finished, synced = true, source = source)
        }
        val trimmed = plain.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
        if (trimmed.isEmpty()) return null
        return Lyrics(trimmed.map { LyricLine(-1L, it) }, synced = false, source = source)
    }

    private fun parseContent(raw: String, lineTimeMs: Long): Content {
        val matches = InlineTag.findAll(raw).toList()
        if (matches.isEmpty()) return Content(raw.trim(), emptyList())
        val builder = StringBuilder()
        val words = ArrayList<LyricWord>()
        fun append(startMs: Long, segment: String) {
            val offset = builder.length
            builder.append(segment)
            val first = segment.indexOfFirst { !it.isWhitespace() }
            if (first < 0) return
            val last = segment.indexOfLast { !it.isWhitespace() } + 1
            words.add(LyricWord(startMs, UNKNOWN_END, offset + first, offset + last))
        }
        append(lineTimeMs, raw.substring(0, matches.first().range.first))
        matches.forEachIndexed { index, match ->
            val values = match.groupValues
            val time = if (values[1].isNotEmpty()) {
                toMillis(values[1], values[2], values[3])
            } else {
                toMillis(values[4], values[5], values[6])
            }
            val previous = words.lastOrNull()
            if (previous != null && previous.endMs == UNKNOWN_END) {
                words[words.size - 1] = previous.copy(endMs = time)
            }
            val segmentEnd = if (index + 1 < matches.size) matches[index + 1].range.first else raw.length
            append(time, raw.substring(match.range.last + 1, segmentEnd))
        }
        val full = builder.toString()
        val text = full.trim()
        if (text.isEmpty()) return Content("", emptyList())
        val lead = full.length - full.trimStart().length
        val adjusted = words.map { word ->
            word.copy(
                charStart = (word.charStart - lead).coerceIn(0, text.length),
                charEnd = (word.charEnd - lead).coerceIn(0, text.length)
            )
        }.filter { it.charEnd > it.charStart }
        return Content(text, adjusted)
    }

    private fun finishWords(words: List<LyricWord>, nextLineMs: Long?): List<LyricWord> = words.map { word ->
        if (word.endMs != UNKNOWN_END) {
            word.copy(endMs = word.endMs.coerceAtLeast(word.startMs))
        } else {
            val natural = ((word.charEnd - word.charStart) * WORD_MS_PER_CHAR).coerceIn(WORD_MIN_MS, WORD_MAX_MS)
            val end = nextLineMs?.let { minOf(it, word.startMs + natural) } ?: (word.startMs + natural)
            word.copy(endMs = end.coerceAtLeast(word.startMs))
        }
    }

    private fun LyricLine.shiftedBy(deltaMs: Long): LyricLine {
        if (deltaMs == 0L) return this
        return copy(
            timeMs = (timeMs + deltaMs).coerceAtLeast(0L),
            words = words.map { word ->
                word.copy(
                    startMs = (word.startMs + deltaMs).coerceAtLeast(0L),
                    endMs = if (word.endMs == UNKNOWN_END) UNKNOWN_END else (word.endMs + deltaMs).coerceAtLeast(0L)
                )
            }
        )
    }

    private fun toMillis(minutesText: String, secondsText: String, fraction: String): Long {
        val minutes = minutesText.toLongOrNull() ?: 0L
        val seconds = secondsText.toLongOrNull() ?: 0L
        val millis = if (fraction.isEmpty()) 0L else fraction.padEnd(3, '0').take(3).toLong()
        return minutes * 60_000L + seconds * 1_000L + millis
    }
}

object LyricsRepository {
    private const val MAX_LRC_BYTES = 512 * 1024
    private val LrcExtensions = listOf(".lrc", ".LRC", ".Lrc")

    private class Entry(val lyrics: Lyrics?)

    private val cache = LruCache<String, Entry>(48)

    var version by mutableIntStateOf(0)
        private set

    fun update(trackId: String, text: String) {
        cache.put(trackId, Entry(LrcParser.parse(text)))
        version++
    }

    suspend fun load(context: Context, track: Track): Lyrics? {
        cache.get(track.id)?.let { return it.lyrics }
        val appContext = context.applicationContext
        val lyrics = withContext(Dispatchers.IO) {
            readSidecar(appContext, track.uri)?.let { LrcParser.parse(it) }
                ?: AudioTagReader.readLyrics(appContext, track.uri)?.let { LrcParser.parse(it) }
        }
        cache.put(track.id, Entry(lyrics))
        return lyrics
    }

    private fun readSidecar(context: Context, audioUri: Uri): String? {
        val documentId = runCatching { DocumentsContract.getDocumentId(audioUri) }.getOrNull() ?: return null
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(audioUri) }.getOrNull() ?: return null
        val authority = audioUri.authority ?: return null
        val slash = documentId.lastIndexOf('/')
        val dot = documentId.lastIndexOf('.')
        val base = if (dot > slash) documentId.substring(0, dot) else documentId
        val treeUri = DocumentsContract.buildTreeDocumentUri(authority, treeId)
        for (extension in LrcExtensions) {
            val lrcUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, base + extension)
            val bytes = runCatching {
                context.contentResolver.openInputStream(lrcUri)?.use { input ->
                    val buffer = ByteArray(MAX_LRC_BYTES)
                    var total = 0
                    while (total < buffer.size) {
                        val read = input.read(buffer, total, buffer.size - total)
                        if (read < 0) break
                        total += read
                    }
                    buffer.copyOf(total)
                }
            }.getOrNull() ?: continue
            return decode(bytes)
        }
        return null
    }

    private fun decode(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            String(bytes, Charset.forName("windows-1252"))
        }
    }
}
