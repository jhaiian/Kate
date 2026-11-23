package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import com.jhaiian.kate.playback.PlayerController
import com.kyant.taglib.TagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DraftLine(val text: String, val timeMs: Long?, val words: List<Long?>? = null, val ends: List<Long?>? = null)

object LyricsTagWriter {
    private const val MIN_PAUSE_MS = 200L
    private val LyricsKeys = setOf("LYRICS", "UNSYNCEDLYRICS", "UNSYNCED LYRICS", "SYNCEDLYRICS")
    private val VoicePrefix = Regex("""^\s*(?:[vV]\d{1,2}|[bB][gG]|[MFD])\s*:\s*""")
    private val Spaces = Regex("""\s+""")
    private val SyncedLine = Regex("""^\[\d{1,3}:\d{1,2}""")
    private val CreditTag = Regex("""^\[(?:ti|ar|al|by|source)\s*:.*]$""", RegexOption.IGNORE_CASE)
    private val TagUnsafe = Regex("""[\[\]\r\n]+""")
    private const val CREDIT_BY = "Kate"
    const val MAKER_SOURCE = "Kate Lyrics Maker"

    suspend fun write(context: Context, uri: Uri, lyricsText: String): Boolean {
        val ok = writeFile(context, uri, lyricsText)
        if (ok) withContext(Dispatchers.Main) { PlayerController.reloadAfterWrite(context, uri.toString()) }
        return ok
    }

    private suspend fun writeFile(context: Context, uri: Uri, lyricsText: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
                val metadata = TagLib.getMetadata(descriptor.dup().detachFd(), false) ?: return@use false
                val map = HashMap(metadata.propertyMap)
                map.keys.filter { it.uppercase() in LyricsKeys }.forEach { map.remove(it) }
                if (lyricsText.isNotBlank()) map["LYRICS"] = arrayOf(lyricsText)
                TagLib.savePropertyMap(descriptor.dup().detachFd(), map)
            } ?: false
        }.getOrDefault(false)
    }

    fun withCredits(text: String, track: Track, source: String): String {
        val lines = text.lines()
        if (lines.none { SyncedLine.containsMatchIn(it.trim()) }) return text
        val body = lines.filterNot { CreditTag.matches(it.trim().trimStart('\uFEFF')) }.joinToString("\n").trim()
        val header = buildList {
            tagValue(track.title)?.let { add("[ti:$it]") }
            tagValue(track.artist)?.let { add("[ar:$it]") }
            tagValue(track.album)?.let { add("[al:$it]") }
            add("[by:$CREDIT_BY]")
            tagValue(source)?.let { add("[source:$it]") }
        }
        return (header + body).joinToString("\n")
    }

    private fun tagValue(value: String): String? = value.replace(TagUnsafe, " ").trim().takeIf { it.isNotEmpty() }

    fun stripVoice(text: String): String = VoicePrefix.replace(text, "")

    fun voiceOf(text: String): String = text.substring(0, text.length - stripVoice(text).length)

    fun wordsOf(text: String): List<String> = stripVoice(text).trim().split(Spaces).filter { it.isNotEmpty() }

    fun buildText(lines: List<DraftLine>): String {
        val content = lines.filter { it.text.isNotBlank() }
        if (content.none { it.timeMs != null }) return content.joinToString("\n") { it.text.trim() }
        val times = fillMissingTimes(content.map { it.timeMs })
        return content.indices.joinToString("\n") { index ->
            "[${formatTag(times[index])}]${lineBody(content[index], times.getOrNull(index + 1))}"
        }
    }

    private fun lineBody(line: DraftLine, nextLineMs: Long?): String {
        val text = line.text.trim()
        val wordTimes = line.words ?: return text
        val words = wordsOf(text)
        if (words.size != wordTimes.size || wordTimes.none { it != null }) return text
        val endTimes = line.ends?.takeIf { it.size == words.size }
        val body = words.indices.joinToString(" ") { index ->
            val time = wordTimes[index]
            val nextStart = (index + 1 until words.size).firstNotNullOfOrNull { wordTimes[it] } ?: nextLineMs
            val end = endTimes?.get(index)?.takeIf { value ->
                time != null && value > time && (nextStart == null || nextStart - value >= MIN_PAUSE_MS)
            }
            val start = if (time != null) "<${formatTag(time)}>${words[index]}" else words[index]
            if (end != null) start + "<${formatTag(end)}>" else start
        }
        return voiceOf(text) + body
    }

    fun fillMissingTimes(times: List<Long?>): List<Long> {
        val result = LongArray(times.size)
        var index = 0
        while (index < times.size) {
            val known = times[index]
            if (known != null) {
                result[index] = known
                index++
                continue
            }
            val previousIndex = index - 1
            var nextIndex = index
            while (nextIndex < times.size && times[nextIndex] == null) nextIndex++
            val start = if (previousIndex >= 0) result[previousIndex] else 0L
            val end = if (nextIndex < times.size) times[nextIndex]!! else start + (nextIndex - previousIndex) * 3_000L
            val steps = nextIndex - previousIndex
            for (fill in index until nextIndex) {
                result[fill] = start + (end - start) * (fill - previousIndex) / steps
            }
            index = nextIndex
        }
        for (position in 1 until result.size) {
            if (result[position] < result[position - 1]) result[position] = result[position - 1]
        }
        return result.toList()
    }

    fun formatTag(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        val minutes = safe / 60_000L
        val seconds = (safe / 1_000L) % 60L
        val hundredths = (safe % 1_000L) / 10L
        return "%02d:%02d.%02d".format(java.util.Locale.US, minutes, seconds, hundredths)
    }

    fun voicePrefix(voice: Int, duet: Boolean): String = when (voice) {
        LyricLine.VOICE_SECOND -> "v2: "
        LyricLine.VOICE_BACKGROUND -> "bg: "
        else -> if (duet) "v1: " else ""
    }

    fun wordStarts(line: LyricLine): List<Long?>? {
        if (!line.hasWordTiming) return null
        val words = wordsOf(line.text)
        if (words.isEmpty()) return null
        val result = arrayOfNulls<Long>(words.size)
        var cursor = 0
        words.forEachIndexed { index, word ->
            val position = line.text.indexOf(word, cursor)
            if (position >= 0) {
                result[index] = line.words.firstOrNull { it.charStart <= position && it.charEnd > position }?.startMs
                cursor = position + word.length
            }
        }
        return result.toList().takeIf { list -> list.any { it != null } }
    }

    fun wordEnds(line: LyricLine): List<Long?>? {
        val starts = wordStarts(line) ?: return null
        val words = wordsOf(line.text)
        val result = arrayOfNulls<Long>(words.size)
        var cursor = 0
        words.forEachIndexed { index, word ->
            val position = line.text.indexOf(word, cursor)
            if (position >= 0) {
                val timing = line.words.lastOrNull { it.charStart < position + word.length && it.charEnd > position }
                val nextStart = (index + 1 until starts.size).firstNotNullOfOrNull { starts[it] }
                val end = timing?.endMs
                if (end != null && nextStart != null && nextStart - end >= MIN_PAUSE_MS) result[index] = end
                cursor = position + word.length
            }
        }
        return result.toList().takeIf { list -> list.any { it != null } }
    }
}
