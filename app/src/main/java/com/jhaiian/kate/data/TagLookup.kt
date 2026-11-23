package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import okhttp3.Request
import java.net.URLEncoder
import java.text.Normalizer
import kotlin.math.abs

enum class TagSource { MUSICBRAINZ, DEEZER, ITUNES }

class TagMatch(
    val source: TagSource,
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val year: String,
    val trackNumber: Int?,
    val discNumber: Int?,
    val genre: String,
    val durationMs: Long,
    val thumbUrl: String?,
    val coverUrls: List<String>,
    val compilation: Boolean = false
) {
    val key: String get() = "${source.name}:$id"

    fun valueOf(field: TagField): String = when (field) {
        TagField.TITLE -> title
        TagField.ARTIST -> artist
        TagField.ALBUM -> album
        TagField.ALBUM_ARTIST -> albumArtist
        TagField.GENRE -> genre
        TagField.YEAR -> year
        TagField.TRACK -> trackNumber?.toString().orEmpty()
        TagField.DISC -> discNumber?.toString().orEmpty()
        else -> ""
    }
}

sealed interface TagLookupResult {
    data class Found(val matches: List<TagMatch>) : TagLookupResult
    data object NotFound : TagLookupResult
    data object Offline : TagLookupResult
}

object TagLookup {
    private const val PREFS_NAME = "tag_lookup"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_SOURCE_PREFIX = "source_"
    private const val TIMEOUT_MILLIS = 12_000
    private const val MAX_JSON_BYTES = 2 * 1024 * 1024
    private const val MAX_IMAGE_BYTES = 10 * 1024 * 1024
    private const val MAX_RESULTS = 15
    private const val DEEZER_DETAILS = 4
    private const val MIN_SCORE = 15
    private val VariousArtists = setOf("variousartists", "various", "variosartistas", "verschiedeneinterpreten")
    private val Brackets = Regex("""\s*[(\[（【][^)\]）】]*[)\]）】]""")
    private val Featuring = Regex("""\s+(?:feat\.?|ft\.?|featuring)\s+.*$""", RegexOption.IGNORE_CASE)
    private val ReleaseSuffix = Regex("""\s+-\s+(?:Single|EP)$""", RegexOption.IGNORE_CASE)
    private val LuceneSpecial = Regex("""([+\-&|!(){}\[\]^"~*?:\\/])""")

    private var state by mutableStateOf<Boolean?>(null)
    private var sources by mutableStateOf<Map<TagSource, Boolean>>(emptyMap())

    fun isEnabled(context: Context): Boolean {
        state?.let { return it }
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
        state = stored
        return stored
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
        state = enabled
    }

    fun isSourceEnabled(context: Context, source: TagSource): Boolean {
        sources[source]?.let { return it }
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SOURCE_PREFIX + source.name.lowercase(), true)
        sources = sources + (source to stored)
        return stored
    }

    fun setSourceEnabled(context: Context, source: TagSource, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SOURCE_PREFIX + source.name.lowercase(), enabled).apply()
        sources = sources + (source to enabled)
    }

    suspend fun search(context: Context, title: String, artist: String, album: String, durationMs: Long): TagLookupResult =
        withContext(Dispatchers.IO) {
            val cleanTitle = title.trim()
            if (cleanTitle.isEmpty()) return@withContext TagLookupResult.NotFound
            val cleanArtist = artist.trim()
            val userAgent = LrcLib.userAgent(context)
            val enabled = TagSource.entries.filter { isSourceEnabled(context, it) }
            if (enabled.isEmpty()) return@withContext TagLookupResult.NotFound
            val results = coroutineScope {
                enabled.map { source ->
                    async {
                        runCatching {
                            when (source) {
                                TagSource.MUSICBRAINZ -> musicBrainz(cleanTitle, cleanArtist, album, userAgent)
                                TagSource.DEEZER -> deezer(cleanTitle, cleanArtist, userAgent)
                                TagSource.ITUNES -> iTunes(cleanTitle, cleanArtist, userAgent)
                            }
                        }.getOrNull()
                    }
                }.map { it.await() }
            }
            if (results.all { it == null }) return@withContext TagLookupResult.Offline
            val ranked = results.filterNotNull().flatten()
                .map { it to score(it, cleanTitle, cleanArtist, album, durationMs) }
                .filter { it.second >= MIN_SCORE }
                .sortedWith(
                    compareByDescending<Pair<TagMatch, Int>> { it.second }
                        .thenBy { it.first.year.toIntOrNull() ?: Int.MAX_VALUE }
                        .thenBy { it.first.source.ordinal }
                )
                .map { it.first }
            val seen = HashSet<String>()
            val unique = ranked.filter { match ->
                seen.add(listOf(match.source.name, normalize(match.title), normalize(match.artist), normalize(match.album), match.year).joinToString("|"))
            }.take(MAX_RESULTS)
            if (unique.isEmpty()) TagLookupResult.NotFound else TagLookupResult.Found(unique)
        }

    suspend fun downloadImage(context: Context, urls: List<String>): ByteArray? = withContext(Dispatchers.IO) {
        val userAgent = LrcLib.userAgent(context)
        urls.firstNotNullOfOrNull { url -> getBytes(url, userAgent, MAX_IMAGE_BYTES, "image/*")?.takeIf { it.isNotEmpty() } }
    }

    private fun score(match: TagMatch, title: String, artist: String, album: String, durationMs: Long): Int {
        val wantedTitle = normalize(title)
        val wantedClean = normalize(cleanTitle(title))
        val gotTitle = normalize(match.title)
        val gotClean = normalize(cleanTitle(match.title))
        val titleScore = when {
            gotTitle.isEmpty() -> 0
            gotTitle == wantedTitle -> 40
            gotClean == wantedClean && gotClean.isNotEmpty() -> 32
            wantedClean.length >= 3 && gotClean.length >= 3 && (gotClean.contains(wantedClean) || wantedClean.contains(gotClean)) -> 18
            else -> 0
        }
        if (titleScore == 0) return 0
        val wantedArtist = normalize(artist)
        val gotArtist = normalize(match.artist)
        val artistScore = when {
            wantedArtist.isEmpty() -> 10
            gotArtist == wantedArtist -> 30
            gotArtist.isNotEmpty() && (gotArtist.contains(wantedArtist) || wantedArtist.contains(gotArtist)) -> 22
            ArtistNames.split(match.artist).map { normalize(it) }.any { it.length >= 2 && wantedArtist.contains(it) } -> 15
            else -> -15
        }
        val durationScore = if (durationMs > 0 && match.durationMs > 0) {
            val diff = abs(durationMs - match.durationMs)
            when {
                diff <= 2_000L -> 20
                diff <= 5_000L -> 14
                diff <= 10_000L -> 6
                diff > 30_000L -> -10
                else -> 0
            }
        } else {
            0
        }
        val wantedAlbum = normalize(album)
        val albumScore = if (wantedAlbum.isNotEmpty() && normalize(match.album) == wantedAlbum) 10 else 0
        val compilationScore = if (match.compilation || normalize(match.albumArtist) in VariousArtists) -12 else 0
        return titleScore + artistScore + durationScore + albumScore + compilationScore
    }

    private suspend fun musicBrainz(title: String, artist: String, album: String, userAgent: String): List<TagMatch>? {
        val query = buildString {
            append("recording:\"").append(escape(title)).append('"')
            if (artist.isNotEmpty()) append(" AND artist:\"").append(escape(artist)).append('"')
        }
        val url = "https://musicbrainz.org/ws/2/recording?fmt=json&limit=15&query=" + encode(query)
        var json = getJson(url, userAgent)
        if (json == null) {
            delay(1_200L)
            json = getJson(url, userAgent)
        }
        val recordings = json?.optJSONArray("recordings") ?: return if (json == null) null else emptyList()
        val wantedAlbum = normalize(album)
        return objects(recordings).mapNotNull { recording ->
            val releases = recording.optJSONArray("releases")?.let { objects(it) }.orEmpty()
            val release = releases.maxByOrNull { releaseScore(it, wantedAlbum) } ?: return@mapNotNull null
            val group = release.optJSONObject("release-group")
            val medium = release.optJSONArray("media")?.optJSONObject(0)
            val trackNumber = medium?.optJSONArray("track")?.optJSONObject(0)?.optString("number")?.toIntOrNull()
            val disc = medium?.optInt("position", 0)?.takeIf { it > 1 }
            val artistName = credit(recording.optJSONArray("artist-credit"))
            val genre = recording.optJSONArray("tags")?.let { objects(it) }
                ?.maxByOrNull { it.optInt("count", 0) }
                ?.optString("name")
                ?.split(' ')
                ?.joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
                .orEmpty()
            val groupId = group?.optString("id")?.takeIf { it.isNotBlank() }
            val releaseId = release.optString("id").takeIf { it.isNotBlank() }
            val covers = buildList {
                if (groupId != null) {
                    add("https://coverartarchive.org/release-group/$groupId/front-1200")
                    add("https://coverartarchive.org/release-group/$groupId/front-500")
                }
                if (releaseId != null) add("https://coverartarchive.org/release/$releaseId/front-1200")
            }
            TagMatch(
                source = TagSource.MUSICBRAINZ,
                id = recording.optString("id") + ":" + (releaseId ?: ""),
                title = recording.optString("title"),
                artist = artistName,
                album = release.optString("title"),
                albumArtist = credit(release.optJSONArray("artist-credit")).ifBlank { artistName },
                year = (release.optString("date").ifBlank { recording.optString("first-release-date") }).take(4).takeIf { it.length == 4 }.orEmpty(),
                trackNumber = trackNumber,
                discNumber = disc,
                genre = genre,
                durationMs = recording.optLong("length", 0L),
                thumbUrl = groupId?.let { "https://coverartarchive.org/release-group/$it/front-250" },
                coverUrls = covers,
                compilation = group?.optJSONArray("secondary-types")?.let { types ->
                    (0 until types.length()).any { types.optString(it).equals("Compilation", ignoreCase = true) || types.optString(it).equals("DJ-mix", ignoreCase = true) }
                } == true
            )
        }
    }

    private fun releaseScore(release: JSONObject, wantedAlbum: String): Int {
        val group = release.optJSONObject("release-group")
        var score = 0
        if (release.optString("status").equals("Official", ignoreCase = true)) score += 3
        when (group?.optString("primary-type")?.lowercase()) {
            "album" -> score += 3
            "single", "ep" -> score += 1
        }
        if ((group?.optJSONArray("secondary-types")?.length() ?: 0) > 0) score -= 4
        if (wantedAlbum.isNotEmpty() && normalize(release.optString("title")) == wantedAlbum) score += 6
        if (release.optString("date").isNotBlank()) score += 1
        return score
    }

    private fun credit(array: JSONArray?): String {
        if (array == null) return ""
        return objects(array).joinToString("") { it.optString("name") + it.optString("joinphrase") }.trim()
    }

    private fun deezer(title: String, artist: String, userAgent: String): List<TagMatch>? {
        val url = "https://api.deezer.com/search?limit=12&q=" + encode(listOf(title, artist).filter { it.isNotEmpty() }.joinToString(" "))
        val json = getJson(url, userAgent) ?: return null
        if (json.has("error")) return null
        val items = json.optJSONArray("data")?.let { objects(it) }.orEmpty()
        val wantedTitle = normalize(cleanTitle(title))
        val relevant = items.filter { item ->
            val got = normalize(cleanTitle(item.optString("title")))
            got.isNotEmpty() && (got == wantedTitle || got.contains(wantedTitle) || wantedTitle.contains(got))
        }
        return relevant.mapIndexed { index, item ->
            val albumJson = item.optJSONObject("album")
            val detail = if (index < DEEZER_DETAILS) getJson("https://api.deezer.com/track/" + item.optLong("id"), userAgent) else null
            val albumDetail = if (index < DEEZER_DETAILS && albumJson != null) {
                getJson("https://api.deezer.com/album/" + albumJson.optLong("id"), userAgent)?.takeIf { !it.has("error") }
            } else {
                null
            }
            val genre = albumDetail?.optJSONObject("genres")?.optJSONArray("data")?.optJSONObject(0)?.optString("name").orEmpty()
            val disc = detail?.optInt("disk_number", 0)?.takeIf { it > 1 }
            val releaseDate = detail?.optString("release_date").orEmpty().ifBlank { albumDetail?.optString("release_date").orEmpty() }
            TagMatch(
                source = TagSource.DEEZER,
                id = item.optLong("id").toString(),
                title = item.optString("title"),
                artist = item.optJSONObject("artist")?.optString("name").orEmpty(),
                album = albumJson?.optString("title").orEmpty(),
                albumArtist = albumDetail?.optJSONObject("artist")?.optString("name").orEmpty(),
                year = releaseDate.take(4).takeIf { it.length == 4 && it != "0000" }.orEmpty(),
                trackNumber = detail?.optInt("track_position", 0)?.takeIf { it > 0 },
                discNumber = disc,
                genre = genre,
                durationMs = item.optLong("duration", 0L) * 1_000L,
                thumbUrl = albumJson?.optString("cover_medium")?.takeIf { it.isNotBlank() },
                coverUrls = listOfNotNull(
                    albumJson?.optString("cover_xl")?.takeIf { it.isNotBlank() },
                    albumJson?.optString("cover_big")?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    private fun iTunes(title: String, artist: String, userAgent: String): List<TagMatch>? {
        val term = listOf(title, artist).filter { it.isNotEmpty() }.joinToString(" ")
        val url = "https://itunes.apple.com/search?media=music&entity=song&limit=15&term=" + encode(term)
        val json = getJson(url, userAgent) ?: return null
        val items = json.optJSONArray("results")?.let { objects(it) }.orEmpty()
        return items.filter { it.optString("kind") == "song" || it.optString("wrapperType") == "track" }.map { item ->
            val art = item.optString("artworkUrl100").takeIf { it.isNotBlank() }
            val discCount = item.optInt("discCount", 1)
            TagMatch(
                source = TagSource.ITUNES,
                id = item.optLong("trackId").toString(),
                title = item.optString("trackName"),
                artist = item.optString("artistName"),
                album = item.optString("collectionName").replace(ReleaseSuffix, ""),
                albumArtist = item.optString("collectionArtistName").ifBlank { item.optString("artistName") },
                year = item.optString("releaseDate").take(4).takeIf { it.length == 4 }.orEmpty(),
                trackNumber = item.optInt("trackNumber", 0).takeIf { it > 0 },
                discNumber = item.optInt("discNumber", 0).takeIf { it > 1 || discCount > 1 },
                genre = item.optString("primaryGenreName"),
                durationMs = item.optLong("trackTimeMillis", 0L),
                thumbUrl = art?.replace("100x100bb", "250x250bb"),
                coverUrls = listOfNotNull(art?.replace("100x100bb", "1400x1400bb"), art?.replace("100x100bb", "600x600bb"))
            )
        }
    }

    private fun objects(array: JSONArray): List<JSONObject> = (0 until array.length()).mapNotNull { array.optJSONObject(it) }

    private fun cleanTitle(title: String): String = title.replace(Brackets, "").replace(Featuring, "").trim()

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase().filter { it.isLetterOrDigit() }

    private fun escape(text: String): String = text.replace(LuceneSpecial, "\\\\$1")

    private fun encode(text: String): String = URLEncoder.encode(text, "UTF-8").replace("+", "%20")

    private fun getJson(url: String, userAgent: String): JSONObject? {
        val bytes = getBytes(url, userAgent, MAX_JSON_BYTES, "application/json") ?: return null
        return runCatching { JSONObject(bytes.toString(Charsets.UTF_8)) }.getOrNull()
    }

    private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

    private fun getBytes(url: String, userAgent: String, maxBytes: Int, accept: String): ByteArray? {
        return try {
            val call = Request.Builder().url(url).header("User-Agent", userAgent).header("Accept", accept).build()
            http.newCall(call).execute().use { reply ->
                if (reply.code != 200) return null
                reply.body.byteStream().use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(16 * 1024)
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > maxBytes) return null
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
            }
        } catch (error: Exception) {
            null
        }
    }
}
