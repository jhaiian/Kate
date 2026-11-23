package com.jhaiian.kate.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import okhttp3.Request
import java.net.URLEncoder
import java.text.Normalizer

class AmllEntry(
    val titles: List<String>,
    val artists: List<String>,
    val albums: List<String>,
    val file: String,
    val author: String?
)

object Amll {
    private const val BASE = "https://raw.githubusercontent.com/amll-dev/amll-ttml-db/refs/heads/main"
    private const val INDEX_URL = "$BASE/metadata/raw-lyrics-index.jsonl"
    private const val DIRECTORY = "amll"
    private const val INDEX_NAME = "raw-lyrics-index.jsonl"
    private const val MAX_AGE_MS = 3L * 24L * 60L * 60L * 1_000L
    private const val TIMEOUT_MILLIS = 15_000
    private val Brackets = Regex("""\s*[(\[（【][^)\]）】]*[)\]）】]""")
    private val Featuring = Regex("""\s+(?:feat\.?|ft\.?|featuring)\s+.*$""", RegexOption.IGNORE_CASE)
    private val Suffix = Regex("""\s+-\s+.*$""")

    @Volatile
    private var cached: List<AmllEntry>? = null

    fun search(context: Context, title: String, artist: String, userAgent: String, limit: Int = 3): List<AmllEntry>? {
        val entries = index(context, userAgent) ?: return null
        val wantedTitles = listOf(title, cleanTitle(title)).map { normalize(it) }.filter { it.isNotEmpty() }.distinct()
        if (wantedTitles.isEmpty()) return emptyList()
        val wantedArtist = normalize(artist)
        return entries.mapNotNull { entry ->
            val titles = entry.titles.flatMap { listOf(normalize(it), normalize(cleanTitle(it))) }.filter { it.isNotEmpty() }
            val titleScore = when {
                titles.any { it in wantedTitles } -> 3
                titles.any { candidate -> wantedTitles.any { wanted -> wanted.length >= 4 && candidate.length >= 4 && (candidate.contains(wanted) || wanted.contains(candidate)) } } -> 1
                else -> 0
            }
            if (titleScore == 0) return@mapNotNull null
            val artists = entry.artists.map { normalize(it) }.filter { it.length >= 2 }
            val artistScore = when {
                wantedArtist.isEmpty() -> 0
                artists.any { wantedArtist.contains(it) || it.contains(wantedArtist) } -> 3
                else -> -10
            }
            val score = titleScore + artistScore
            if (score <= 0 && wantedArtist.isNotEmpty()) null else entry to score
        }.sortedByDescending { it.second }.take(limit).map { it.first }
    }

    fun lyrics(entry: AmllEntry, userAgent: String): String? {
        val text = get("$BASE/raw-lyrics/${URLEncoder.encode(entry.file, "UTF-8").replace("+", "%20")}", userAgent) ?: return null
        return WordLyrics.ttmlToLrc(text)
    }

    private fun index(context: Context, userAgent: String): List<AmllEntry>? {
        cached?.let { return it }
        val file = File(File(context.filesDir, DIRECTORY).apply { mkdirs() }, INDEX_NAME)
        val fresh = file.isFile && System.currentTimeMillis() - file.lastModified() < MAX_AGE_MS
        if (!fresh) {
            val downloaded = get(INDEX_URL, userAgent)
            if (downloaded != null && downloaded.contains("rawLyricFile")) {
                runCatching { file.writeText(downloaded) }
            }
        }
        if (!file.isFile) return null
        val parsed = runCatching { parseIndex(file.readText()) }.getOrNull() ?: return null
        cached = parsed
        return parsed
    }

    private fun parseIndex(text: String): List<AmllEntry> {
        val byKey = LinkedHashMap<String, AmllEntry>()
        for (line in text.lineSequence()) {
            if (line.isBlank()) continue
            val json = runCatching { JSONObject(line) }.getOrNull() ?: continue
            val file = json.optString("rawLyricFile").takeIf { it.isNotBlank() } ?: continue
            val metadata = json.optJSONArray("metadata") ?: continue
            val values = HashMap<String, List<String>>()
            for (index in 0 until metadata.length()) {
                val pair = metadata.optJSONArray(index) ?: continue
                val key = pair.optString(0)
                val list = pair.optJSONArray(1) ?: continue
                values[key] = strings(list)
            }
            val titles = values["musicName"].orEmpty()
            if (titles.isEmpty()) continue
            val artists = values["artists"].orEmpty()
            val entry = AmllEntry(
                titles = titles,
                artists = artists,
                albums = values["album"].orEmpty(),
                file = file,
                author = values["ttmlAuthorGithubLogin"]?.firstOrNull()
            )
            val key = normalize(titles.first()) + "|" + artists.map { normalize(it) }.sorted().joinToString(",")
            byKey.remove(key)
            byKey[key] = entry
        }
        return byKey.values.toList()
    }

    private fun strings(array: JSONArray): List<String> =
        (0 until array.length()).mapNotNull { array.optString(it).takeIf { value -> value.isNotBlank() } }

    private fun cleanTitle(title: String): String =
        title.replace(Brackets, "").replace(Featuring, "").replace(Suffix, "").trim()

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase().filter { it.isLetterOrDigit() }

    private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

    private fun get(url: String, userAgent: String): String? {
        return try {
            val request = Request.Builder().url(url).header("User-Agent", userAgent).build()
            http.newCall(request).execute().use { response ->
                if (response.code != 200) null else response.body.string()
            }
        } catch (error: Exception) {
            null
        }
    }
}
