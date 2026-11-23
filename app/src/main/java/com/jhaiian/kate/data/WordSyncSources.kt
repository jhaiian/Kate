package com.jhaiian.kate.data

import android.util.Base64
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import okhttp3.Request
import java.net.URLEncoder
import java.text.Normalizer
import java.util.zip.InflaterInputStream
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TIMEOUT_MILLIS = 15_000
private const val MAX_BYTES = 2 * 1024 * 1024
private const val FETCH_LIMIT = 2
private const val DURATION_TOLERANCE_SECONDS = 8.0

private class Reply(val code: Int, val body: String)

private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

private fun fetch(url: String, userAgent: String, accept: String = "application/json"): Reply? {
    return try {
        val request = Request.Builder().url(url).header("User-Agent", userAgent).header("Accept", accept).build()
        http.newCall(request).execute().use { response ->
            val code = response.code
            if (code != 200) return Reply(code, "")
            val bytes = response.body.byteStream().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    if (output.size() > MAX_BYTES) break
                }
                output.toByteArray()
            }
            Reply(code, String(bytes, Charsets.UTF_8))
        }
    } catch (error: Exception) {
        null
    }
}

private fun query(params: List<Pair<String, String>>): String =
    params.joinToString("&") { (key, value) -> key + "=" + URLEncoder.encode(value, "UTF-8") }

private fun normalize(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase().filter { it.isLetterOrDigit() }

private fun titleMatches(wanted: String, found: String): Boolean {
    val a = normalize(wanted)
    val b = normalize(found)
    return a.isNotEmpty() && b.isNotEmpty() && (a == b || a.contains(b) || b.contains(a))
}

private fun artistMatches(wanted: String, found: String): Boolean {
    val a = normalize(wanted)
    val b = normalize(found)
    return a.isEmpty() || b.isEmpty() || a.contains(b) || b.contains(a) ||
        found.split(',', '、', '&', '/', ';').map { normalize(it) }.any { it.length >= 2 && a.contains(it) }
}

private fun durationMatches(wanted: Double, found: Double): Boolean =
    wanted <= 0.0 || found <= 0.0 || abs(found - wanted) <= DURATION_TOLERANCE_SECONDS

private fun JSONObject.secondsOf(key: String, scale: Double = 1.0): Double =
    optDouble(key, 0.0).takeIf { !it.isNaN() && it > 0.0 }?.div(scale) ?: 0.0

private fun closeness(wantedTitle: String, title: String, wanted: Double, found: Double): Double {
    val exact = if (normalize(wantedTitle) == normalize(title)) 0.0 else 1_000.0
    val gap = if (wanted > 0.0 && found > 0.0) abs(found - wanted) else 0.0
    return exact + gap
}

object Kugou {
    private const val HOST = "lyrics.kugou.com"
    private val Key = intArrayOf(0x40, 0x47, 0x61, 0x77, 0x5e, 0x32, 0x74, 0x47, 0x51, 0x36, 0x31, 0x2d, 0xce, 0xd2, 0x6e, 0x69)

    fun find(title: String, artist: String, seconds: Double, userAgent: String): List<LrcLibRecord>? {
        val keyword = listOf(artist, title).filter { it.isNotBlank() }.joinToString(" - ")
        val params = listOf(
            "ver" to "1",
            "man" to "yes",
            "client" to "pc",
            "keyword" to keyword,
            "duration" to (seconds * 1_000.0).roundToInt().coerceAtLeast(0).toString(),
            "hash" to ""
        )
        val body = get("search?" + query(params), userAgent) ?: return null
        val candidates = JSONObject(body).optJSONArray("candidates") ?: return emptyList()
        return (0 until candidates.length())
            .mapNotNull { candidates.optJSONObject(it) }
            .filter { item ->
                item.optString("id").isNotBlank() && item.optString("accesskey").isNotBlank() &&
                    titleMatches(title, item.optString("song")) &&
                    artistMatches(artist, item.optString("singer")) &&
                    durationMatches(seconds, item.secondsOf("duration", 1_000.0))
            }
            .sortedBy { closeness(title, it.optString("song"), seconds, it.secondsOf("duration", 1_000.0)) }
            .distinctBy { it.optString("id") }
            .take(FETCH_LIMIT)
            .mapNotNull { item -> download(item, userAgent) }
    }

    private fun download(item: JSONObject, userAgent: String): LrcLibRecord? {
        val id = item.optString("id")
        val accessKey = item.optString("accesskey")
        val words = lyrics(id, accessKey, "krc", userAgent)?.let { decodeKrc(it) }?.let { WordLyrics.krcToLrc(it) }
        val lines = if (words == null) {
            lyrics(id, accessKey, "lrc", userAgent)?.let { runCatching { String(Base64.decode(it, Base64.DEFAULT), Charsets.UTF_8) }.getOrNull() }
                ?.trimStart('\uFEFF')?.takeIf { it.isNotBlank() }
        } else {
            null
        }
        if (words == null && lines == null) return null
        return LrcLibRecord(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            trackName = item.optString("song"),
            artistName = item.optString("singer"),
            albumName = "",
            durationSeconds = item.secondsOf("duration", 1_000.0),
            instrumental = false,
            plainLyrics = null,
            syncedLyrics = lines,
            wordSyncedLyrics = words,
            source = LyricsSource.KUGOU
        )
    }

    private fun lyrics(id: String, accessKey: String, format: String, userAgent: String): String? {
        val params = listOf(
            "ver" to "1",
            "client" to "pc",
            "id" to id,
            "accesskey" to accessKey,
            "fmt" to format,
            "charset" to "utf8"
        )
        val body = get("download?" + query(params), userAgent) ?: return null
        val json = JSONObject(body)
        if (json.optInt("status") != 200) return null
        return json.optString("content").takeIf { it.isNotBlank() }
    }

    private fun decodeKrc(content: String): String? {
        val bytes = runCatching { Base64.decode(content, Base64.DEFAULT) }.getOrNull() ?: return null
        if (bytes.size <= 4 || String(bytes, 0, 4, Charsets.US_ASCII) != "krc1") return null
        val mixed = ByteArray(bytes.size - 4) { index -> (bytes[index + 4].toInt() xor Key[index % Key.size]).toByte() }
        return runCatching {
            InflaterInputStream(ByteArrayInputStream(mixed)).use { it.readBytes() }.toString(Charsets.UTF_8).trimStart('\uFEFF')
        }.getOrNull()
    }

    private fun get(path: String, userAgent: String): String? {
        for (scheme in listOf("https", "http")) {
            val reply = fetch("$scheme://$HOST/$path", userAgent) ?: continue
            return reply.body.takeIf { reply.code == 200 && it.isNotBlank() }
        }
        return null
    }
}

object LrcRed {
    private const val BASE = "https://lrc.red"
    private val Isrc = Regex("""^[A-Z0-9]{12}$""")

    fun find(title: String, artist: String, seconds: Double, userAgent: String): List<LrcLibRecord>? {
        val reply = fetch("$BASE/search.json?" + query(listOf("q" to "$title $artist".trim())), userAgent) ?: return null
        if (reply.code != 200) return if (reply.code in 400..499) emptyList() else null
        val hits = JSONObject(reply.body).optJSONArray("hits") ?: return emptyList()
        return (0 until hits.length())
            .mapNotNull { hits.optJSONObject(it) }
            .filter { hit ->
                Isrc.matches(hit.optString("isrc").trim().uppercase()) &&
                    titleMatches(title, hit.optString("title")) &&
                    artistMatches(artist, hit.optString("artist")) &&
                    durationMatches(seconds, hit.secondsOf("duration"))
            }
            .sortedBy { closeness(title, it.optString("title"), seconds, it.secondsOf("duration")) }
            .distinctBy { it.optString("isrc").trim().uppercase() }
            .take(FETCH_LIMIT)
            .mapNotNull { hit -> download(hit, userAgent) }
    }

    private fun download(hit: JSONObject, userAgent: String): LrcLibRecord? {
        val isrc = hit.optString("isrc").trim().uppercase()
        val reply = fetch("$BASE/s/$isrc.ttml", userAgent, "application/ttml+xml, application/xml") ?: return null
        if (reply.code != 200 || reply.body.isBlank()) return null
        val words = WordLyrics.ttmlToLrc(reply.body) ?: return null
        return LrcLibRecord(
            id = isrc.hashCode().toLong(),
            trackName = hit.optString("title"),
            artistName = hit.optString("artist"),
            albumName = hit.optString("album"),
            durationSeconds = hit.secondsOf("duration"),
            instrumental = false,
            plainLyrics = null,
            syncedLyrics = null,
            wordSyncedLyrics = words,
            source = LyricsSource.LRCRED
        )
    }
}

object LyricsPlus {
    private val Servers = listOf("https://lyricsplus.prjktla.my.id", "https://lyricsplus.binimum.org")

    fun find(title: String, artist: String, seconds: Double, userAgent: String): List<LrcLibRecord>? {
        if (artist.isBlank()) return emptyList()
        val params = buildList {
            add("title" to title)
            add("artist" to artist)
            if (seconds > 0.0) add("duration" to seconds.roundToInt().toString())
        }
        var reached = false
        for (server in Servers) {
            val reply = fetch("$server/v2/lyrics/get?" + query(params), userAgent) ?: continue
            if (reply.code == 404) {
                reached = true
                continue
            }
            if (reply.code != 200) continue
            reached = true
            val record = runCatching { parse(JSONObject(reply.body), title, artist) }.getOrNull() ?: continue
            return listOf(record)
        }
        return if (reached) emptyList() else null
    }

    private fun parse(json: JSONObject, title: String, artist: String): LrcLibRecord? {
        val lines = json.optJSONArray("lyrics") ?: return null
        val wordType = json.optString("type").lowercase() in setOf("word", "syllable")
        val timed = ArrayList<TimedLine>()
        val synced = ArrayList<String>()
        for (index in 0 until lines.length()) {
            val line = lines.optJSONObject(index) ?: continue
            val start = line.optLong("time", -1L).takeIf { it >= 0L } ?: continue
            val prefix = if (line.optJSONObject("element")?.optString("singer") == "v2") "v2: " else ""
            val text = line.optString("text").trim()
            if (text.isNotEmpty()) synced.add("[${LyricsTagWriter.formatTag(start)}]$prefix$text")
            val syllables = line.optJSONArray("syllabus") ?: continue
            if (!wordType) continue
            val main = ArrayList<TimedWord>()
            val background = ArrayList<TimedWord>()
            for (position in 0 until syllables.length()) {
                val syllable = syllables.optJSONObject(position) ?: continue
                val value = syllable.optString("text")
                val begin = syllable.optLong("time", -1L).takeIf { it >= 0L } ?: continue
                if (value.isBlank()) continue
                val end = begin + syllable.optLong("duration", 0L).coerceAtLeast(0L)
                val word = TimedWord(value, begin, end, value.endsWith(" "))
                if (syllable.optBoolean("isBackground", false)) background.add(word) else main.add(word)
            }
            if (main.isNotEmpty()) timed.add(TimedLine(main.first().startMs, main, prefix))
            if (background.isNotEmpty()) timed.add(TimedLine(background.first().startMs, WordLyrics.trimParentheses(background), "bg: "))
        }
        val words = if (timed.isEmpty()) null else WordLyrics.toLrc(timed)
        val lineSynced = synced.takeIf { it.isNotEmpty() }?.joinToString("\n")
        if (words == null && lineSynced == null) return null
        val metadata = json.optJSONObject("metadata")
        val foundTitle = metadata?.optString("title")?.takeIf { it.isNotBlank() } ?: title
        val foundArtist = metadata?.optString("artist")?.takeIf { it.isNotBlank() } ?: artist
        return LrcLibRecord(
            id = (normalize(foundTitle) + "|" + normalize(foundArtist)).hashCode().toLong(),
            trackName = foundTitle,
            artistName = foundArtist,
            albumName = metadata?.optString("album").orEmpty(),
            durationSeconds = WordLyrics.parseTime(metadata?.optString("totalDuration"))?.div(1_000.0) ?: 0.0,
            instrumental = false,
            plainLyrics = null,
            syncedLyrics = lineSynced,
            wordSyncedLyrics = words,
            source = LyricsSource.LYRICSPLUS
        )
    }
}
