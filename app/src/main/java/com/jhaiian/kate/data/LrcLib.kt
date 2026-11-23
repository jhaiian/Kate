package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.Request
import java.net.URLEncoder
import kotlin.math.abs
import kotlin.math.roundToInt

enum class LyricsSource(val creditName: String) {
    AMLL("AMLL TTML DB"),
    LRCRED("lrc.red"),
    LYRICSPLUS("LyricsPlus"),
    NETEASE("NetEase Cloud Music"),
    KUGOU("Kugou"),
    LRCLIB("LRCLIB")
}

data class LrcLibRecord(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Double,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?,
    val wordSyncedLyrics: String? = null,
    val source: LyricsSource = LyricsSource.LRCLIB,
    val credit: String? = null
) {
    val key: String get() = "${source.name}:$id"
    val hasWords: Boolean get() = !wordSyncedLyrics.isNullOrBlank()
    val hasSynced: Boolean get() = hasWords || !syncedLyrics.isNullOrBlank()
    val bestText: String?
        get() = wordSyncedLyrics?.takeIf { it.isNotBlank() }
            ?: syncedLyrics?.takeIf { it.isNotBlank() }
            ?: plainLyrics?.takeIf { it.isNotBlank() }
}

sealed interface LrcLibResult {
    data class Found(val records: List<LrcLibRecord>) : LrcLibResult
    data object NotFound : LrcLibResult
    data class RateLimited(val retryAfterSeconds: Int) : LrcLibResult
    data object Offline : LrcLibResult
}

object LrcLib {
    private const val PREFS_NAME = "lrclib"
    private const val KEY_ENABLED = "enabled"
    private const val BASE_URL = "https://lrclib.net/api"
    private const val HOMEPAGE = "https://github.com/jhaiian/Kate"
    private const val TIMEOUT_MILLIS = 12_000
    private const val MAX_BYTES = 2 * 1024 * 1024

    private const val KEY_SOURCE_PREFIX = "source_"
    private const val NETEASE_FETCH_LIMIT = 3

    private var state by mutableStateOf<Boolean?>(null)
    private var sources by mutableStateOf<Map<LyricsSource, Boolean>>(emptyMap())

    fun isSourceEnabled(context: Context, source: LyricsSource): Boolean {
        sources[source]?.let { return it }
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SOURCE_PREFIX + source.name.lowercase(), true)
        sources = sources + (source to stored)
        return stored
    }

    fun setSourceEnabled(context: Context, source: LyricsSource, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SOURCE_PREFIX + source.name.lowercase(), enabled).apply()
        sources = sources + (source to enabled)
    }

    private fun normalize(text: String): String =
        java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKC).lowercase().filter { it.isLetterOrDigit() }

    private fun fromAmll(context: Context, title: String, artist: String, userAgent: String): List<LrcLibRecord>? {
        val entries = Amll.search(context, title, artist, userAgent) ?: return null
        return entries.mapNotNull { entry ->
            val lyrics = Amll.lyrics(entry, userAgent) ?: return@mapNotNull null
            LrcLibRecord(
                id = entry.file.hashCode().toLong(),
                trackName = entry.titles.first(),
                artistName = entry.artists.take(3).joinToString(", "),
                albumName = entry.albums.firstOrNull().orEmpty(),
                durationSeconds = 0.0,
                instrumental = false,
                plainLyrics = null,
                syncedLyrics = null,
                wordSyncedLyrics = lyrics,
                source = LyricsSource.AMLL,
                credit = entry.author
            )
        }
    }

    private fun fromNetEase(context: Context, title: String, artist: String): List<LrcLibRecord>? {
        val found = NetEase.search(context, listOf(title, artist).filter { it.isNotBlank() }.joinToString(" "))
        val songs = (found as? NetEase.Outcome.Ok)?.value ?: return null
        val wantedTitle = normalize(title)
        val wantedArtist = normalize(artist)
        val matching = songs.filter { song ->
            val name = normalize(song.name)
            val titleOk = name.isNotEmpty() && (name == wantedTitle || name.contains(wantedTitle) || wantedTitle.contains(name))
            val singers = normalize(song.artists)
            val artistOk = wantedArtist.isEmpty() || singers.isEmpty() || singers.contains(wantedArtist) || wantedArtist.contains(singers) ||
                song.artists.split(",").map { normalize(it) }.any { it.length >= 2 && wantedArtist.contains(it) }
            titleOk && artistOk
        }.take(NETEASE_FETCH_LIMIT)
        return matching.mapNotNull { song ->
            val lyrics = (NetEase.lyrics(context, song.id) as? NetEase.Outcome.Ok)?.value ?: return@mapNotNull null
            if (lyrics.wordSynced == null && lyrics.lineSynced == null) return@mapNotNull null
            LrcLibRecord(
                id = song.id,
                trackName = song.name,
                artistName = song.artists,
                albumName = song.album,
                durationSeconds = song.durationMs / 1_000.0,
                instrumental = false,
                plainLyrics = null,
                syncedLyrics = lyrics.lineSynced,
                wordSyncedLyrics = lyrics.wordSynced,
                source = LyricsSource.NETEASE
            )
        }
    }

    fun isEnabled(context: Context): Boolean {
        val known = state
        if (known != null) return known
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
        state = stored
        return stored
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
        state = enabled
    }

    suspend fun find(context: Context, track: Track): LrcLibResult = withContext(Dispatchers.IO) {
        val userAgent = userAgent(context)
        val title = track.title.trim()
        val artist = track.artist.trim()
        if (title.isEmpty()) return@withContext LrcLibResult.NotFound
        val seconds = track.durationMs / 1_000.0
        val fetchers = listOf<Pair<LyricsSource, () -> List<LrcLibRecord>?>>(
            LyricsSource.AMLL to { fromAmll(context, title, artist, userAgent) },
            LyricsSource.LRCRED to { LrcRed.find(title, artist, seconds, userAgent) },
            LyricsSource.LYRICSPLUS to { LyricsPlus.find(title, artist, seconds, userAgent) },
            LyricsSource.NETEASE to { fromNetEase(context, title, artist) },
            LyricsSource.KUGOU to { Kugou.find(title, artist, seconds, userAgent) }
        ).filter { isSourceEnabled(context, it.first) }.map { (_, fetch) ->
            async {
                try {
                    fetch()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    null
                }
            }
        }
        val lrcLib = if (isSourceEnabled(context, LyricsSource.LRCLIB)) async { fromLrcLib(track, title, artist, userAgent) } else null
        val collected = ArrayList<LrcLibRecord>()
        var reached = false
        var limitedFor: Int? = null
        fetchers.awaitAll().forEach { records ->
            if (records != null) {
                reached = true
                collected += records
            }
        }
        when (val result = lrcLib?.await()) {
            is LrcLibResult.Found -> {
                reached = true
                collected += result.records
            }
            is LrcLibResult.RateLimited -> limitedFor = result.retryAfterSeconds
            LrcLibResult.NotFound -> reached = true
            LrcLibResult.Offline, null -> Unit
        }
        val usable = collected
            .filter { !it.instrumental && it.bestText != null }
            .distinctBy { it.key }
            .sortedWith(
                compareByDescending<LrcLibRecord> { it.hasWords }
                    .thenBy { it.source == LyricsSource.LRCRED }
                    .thenBy { it.source.ordinal }
                    .thenByDescending { it.hasSynced }
                    .thenBy { if (seconds > 0 && it.durationSeconds > 0) abs(it.durationSeconds - seconds) else 0.0 }
            )
        when {
            usable.isNotEmpty() -> LrcLibResult.Found(usable)
            limitedFor != null && !reached -> LrcLibResult.RateLimited(limitedFor)
            reached -> LrcLibResult.NotFound
            else -> LrcLibResult.Offline
        }
    }

    private fun fromLrcLib(track: Track, title: String, artist: String, userAgent: String): LrcLibResult {
        try {
            val results = LinkedHashMap<Long, LrcLibRecord>()
            if (artist.isNotEmpty()) {
                val params = buildList {
                    add("track_name" to title)
                    add("artist_name" to artist)
                    if (track.album.isNotBlank()) add("album_name" to track.album.trim())
                    val seconds = (track.durationMs / 1_000.0).roundToInt()
                    if (seconds in 1..3600) add("duration" to seconds.toString())
                }
                when (val response = request("$BASE_URL/get?${query(params)}", userAgent)) {
                    is Response.Ok -> parseRecord(JSONObject(response.body))?.let { results[it.id] = it }
                    is Response.Limited -> return LrcLibResult.RateLimited(response.retryAfter)
                    else -> Unit
                }
            }
            val searchParams = if (artist.isNotEmpty()) {
                listOf("track_name" to title, "artist_name" to artist)
            } else {
                listOf("q" to title)
            }
            when (val response = request("$BASE_URL/search?${query(searchParams)}", userAgent)) {
                is Response.Ok -> {
                    val array = JSONArray(response.body)
                    (0 until array.length()).mapNotNull { parseRecord(array.optJSONObject(it)) }
                        .forEach { record -> if (record.id !in results) results[record.id] = record }
                }
                is Response.Limited -> if (results.isEmpty()) return LrcLibResult.RateLimited(response.retryAfter)
                is Response.Missing -> Unit
                is Response.Failed -> if (results.isEmpty()) return LrcLibResult.Offline
            }
            val usable = results.values.filter { !it.instrumental && it.bestText != null }
            return if (usable.isEmpty()) LrcLibResult.NotFound else LrcLibResult.Found(usable)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return LrcLibResult.Offline
        }
    }

    private sealed interface Response {
        class Ok(val body: String) : Response
        data object Missing : Response
        class Limited(val retryAfter: Int) : Response
        data object Failed : Response
    }

    private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

    private fun request(url: String, userAgent: String): Response {
        return try {
            val call = Request.Builder().url(url).header("User-Agent", userAgent).header("Accept", "application/json").build()
            http.newCall(call).execute().use { reply ->
                when (val code = reply.code) {
                    200 -> {
                        val bytes = reply.body.byteStream().use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(16 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                if (output.size() > MAX_BYTES) break
                            }
                            output.toByteArray()
                        }
                        Response.Ok(String(bytes, Charsets.UTF_8))
                    }
                    404 -> Response.Missing
                    429 -> Response.Limited(reply.header("Retry-After")?.trim()?.toIntOrNull()?.coerceAtLeast(1) ?: 30)
                    else -> if (code in 400..499) Response.Missing else Response.Failed
                }
            }
        } catch (error: Exception) {
            Response.Failed
        }
    }

    private fun parseRecord(json: JSONObject?): LrcLibRecord? {
        if (json == null) return null
        val id = json.optLong("id", -1L).takeIf { it >= 0 } ?: return null
        fun text(key: String): String? = if (json.isNull(key)) null else json.optString(key).takeIf { it.isNotBlank() }
        return LrcLibRecord(
            id = id,
            trackName = text("trackName") ?: text("name").orEmpty(),
            artistName = text("artistName").orEmpty(),
            albumName = text("albumName").orEmpty(),
            durationSeconds = json.optDouble("duration", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            instrumental = json.optBoolean("instrumental", false),
            plainLyrics = text("plainLyrics"),
            syncedLyrics = text("syncedLyrics"),
            wordSyncedLyrics = text("lyricsfile")?.takeIf { json.optBoolean("hasWordSync", false) || Lyricsfile.hasWords(it) }
                ?.let { Lyricsfile.toLrc(it) }
        )
    }

    private fun query(params: List<Pair<String, String>>): String =
        params.joinToString("&") { (key, value) -> key + "=" + URLEncoder.encode(value, "UTF-8") }

    fun userAgent(context: Context): String {
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
        return "Kate v$version ($HOMEPAGE)"
    }
}
