package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class SmartFieldKind { TEXT, NUMBER, DATE, FLAG }

enum class SmartOperator(val key: String, val valueCount: Int) {
    CONTAINS("contains", 1),
    NOT_CONTAINS("not_contains", 1),
    IS("is", 1),
    IS_NOT("is_not", 1),
    STARTS_WITH("starts_with", 1),
    AT_LEAST("at_least", 1),
    AT_MOST("at_most", 1),
    BETWEEN("between", 2),
    WITHIN_DAYS("within_days", 1),
    OLDER_THAN_DAYS("older_than_days", 1),
    IS_TRUE("is_true", 0),
    IS_FALSE("is_false", 0);

    companion object {
        fun fromKey(key: String?): SmartOperator? = entries.firstOrNull { it.key == key }
    }
}

enum class SmartField(val key: String, val kind: SmartFieldKind) {
    TITLE("title", SmartFieldKind.TEXT),
    ARTIST("artist", SmartFieldKind.TEXT),
    ALBUM("album", SmartFieldKind.TEXT),
    ALBUM_ARTIST("album_artist", SmartFieldKind.TEXT),
    GENRE("genre", SmartFieldKind.TEXT),
    YEAR("year", SmartFieldKind.NUMBER),
    DURATION("duration", SmartFieldKind.NUMBER),
    PLAY_COUNT("play_count", SmartFieldKind.NUMBER),
    LAST_PLAYED("last_played", SmartFieldKind.DATE),
    DATE_ADDED("date_added", SmartFieldKind.DATE),
    FAVORITE("favorite", SmartFieldKind.FLAG),
    FOLDER("folder", SmartFieldKind.TEXT),
    FORMAT("format", SmartFieldKind.TEXT);

    val operators: List<SmartOperator>
        get() = when {
            kind == SmartFieldKind.TEXT -> listOf(
                SmartOperator.CONTAINS,
                SmartOperator.NOT_CONTAINS,
                SmartOperator.IS,
                SmartOperator.IS_NOT,
                SmartOperator.STARTS_WITH
            )
            this == DURATION -> listOf(SmartOperator.AT_LEAST, SmartOperator.AT_MOST, SmartOperator.BETWEEN)
            kind == SmartFieldKind.NUMBER -> listOf(
                SmartOperator.IS,
                SmartOperator.AT_LEAST,
                SmartOperator.AT_MOST,
                SmartOperator.BETWEEN
            )
            kind == SmartFieldKind.DATE -> listOf(SmartOperator.WITHIN_DAYS, SmartOperator.OLDER_THAN_DAYS)
            else -> listOf(SmartOperator.IS_TRUE, SmartOperator.IS_FALSE)
        }

    val usesPlays: Boolean get() = this == PLAY_COUNT || this == LAST_PLAYED

    companion object {
        fun fromKey(key: String?): SmartField? = entries.firstOrNull { it.key == key }
    }
}

enum class SmartSort(val key: String) {
    TITLE("title"),
    ARTIST("artist"),
    ALBUM("album"),
    YEAR("year"),
    DATE_ADDED("date_added"),
    PLAY_COUNT("play_count"),
    LAST_PLAYED("last_played"),
    DURATION("duration"),
    RANDOM("random");

    val usesPlays: Boolean get() = this == PLAY_COUNT || this == LAST_PLAYED

    companion object {
        fun fromKey(key: String?): SmartSort? = entries.firstOrNull { it.key == key }
    }
}

enum class SmartPreset(val key: String) {
    MOST_PLAYED("most_played"),
    RECENTLY_ADDED("recently_added"),
    NEVER_PLAYED("never_played"),
    FORGOTTEN_GEMS("forgotten_gems");

    companion object {
        fun fromKey(key: String?): SmartPreset? = entries.firstOrNull { it.key == key }
    }
}

data class SmartRule(
    val field: SmartField,
    val operator: SmartOperator,
    val value: String = "",
    val value2: String = ""
) {
    fun withField(next: SmartField): SmartRule =
        if (next == field) this else SmartRule(next, next.operators.first(), defaultValueFor(next), "")

    companion object {
        fun defaultValueFor(field: SmartField): String = when (field) {
            SmartField.PLAY_COUNT -> "1"
            SmartField.LAST_PLAYED, SmartField.DATE_ADDED -> "30"
            else -> ""
        }
    }
}

data class SmartPlaylist(
    val id: String,
    val name: String,
    val rules: List<SmartRule>,
    val matchAll: Boolean = true,
    val sort: SmartSort = SmartSort.TITLE,
    val descending: Boolean = false,
    val limit: Int = 0,
    val preset: SmartPreset? = null,
    val decade: Int? = null,
    val createdAt: Long = 0L
) {
    val isDecade: Boolean get() = decade != null

    val usesPlays: Boolean get() = sort.usesPlays || rules.any { it.field.usesPlays }
}

object SmartPlaylistEngine {
    private const val DAY_MS = 86_400_000L

    fun evaluate(
        playlist: SmartPlaylist,
        tracks: List<Track>,
        plays: Map<String, TrackPlays>,
        favorites: Set<String>,
        now: Long = System.currentTimeMillis()
    ): List<Track> {
        val rules = playlist.rules.filter { it.isComplete() }
        val matched = if (rules.isEmpty()) {
            tracks
        } else {
            tracks.filter { track ->
                if (playlist.matchAll) {
                    rules.all { matches(it, track, plays, favorites, now) }
                } else {
                    rules.any { matches(it, track, plays, favorites, now) }
                }
            }
        }
        val sorted = sort(matched, playlist, plays, now)
        return if (playlist.limit > 0) sorted.take(playlist.limit) else sorted
    }

    private fun SmartRule.isComplete(): Boolean = when (operator.valueCount) {
        0 -> true
        1 -> if (field.kind == SmartFieldKind.TEXT) value.isNotBlank() else value.toDoubleOrNull() != null
        else -> value.toDoubleOrNull() != null && value2.toDoubleOrNull() != null
    }

    private fun matches(
        rule: SmartRule,
        track: Track,
        plays: Map<String, TrackPlays>,
        favorites: Set<String>,
        now: Long
    ): Boolean = when (rule.field.kind) {
        SmartFieldKind.TEXT -> matchesText(rule, track)
        SmartFieldKind.NUMBER -> {
            val value = when (rule.field) {
                SmartField.YEAR -> track.year?.toDouble()
                SmartField.DURATION -> track.durationMs / 60_000.0
                else -> (plays[track.id]?.count ?: 0).toDouble()
            }
            value != null && matchesNumber(rule, value)
        }
        SmartFieldKind.DATE -> {
            val at = when (rule.field) {
                SmartField.LAST_PLAYED -> plays[track.id]?.lastPlayedAt
                else -> track.dateAddedMs.takeIf { it > 0L }
            }
            val days = rule.value.toDoubleOrNull() ?: 0.0
            val cutoff = now - (days * DAY_MS).toLong()
            when (rule.operator) {
                SmartOperator.WITHIN_DAYS -> at != null && at >= cutoff
                else -> at == null || at < cutoff
            }
        }
        SmartFieldKind.FLAG -> {
            val favorite = track.id in favorites
            if (rule.operator == SmartOperator.IS_TRUE) favorite else !favorite
        }
    }

    private fun matchesNumber(rule: SmartRule, value: Double): Boolean {
        val first = rule.value.toDoubleOrNull() ?: return true
        return when (rule.operator) {
            SmartOperator.IS -> value == first
            SmartOperator.AT_LEAST -> value >= first
            SmartOperator.AT_MOST -> value <= first
            SmartOperator.BETWEEN -> {
                val second = rule.value2.toDoubleOrNull() ?: return true
                value >= min(first, second) && value <= max(first, second)
            }
            else -> true
        }
    }

    private fun matchesText(rule: SmartRule, track: Track): Boolean {
        val needle = rule.value.trim()
        val whole = textOf(rule.field, track)
        val parts = when (rule.field) {
            SmartField.ARTIST, SmartField.ALBUM_ARTIST -> ArtistNames.split(whole)
            SmartField.GENRE -> ArtistNames.splitGenres(whole)
            else -> emptyList()
        }
        val equals = whole.trim().equals(needle, ignoreCase = true) || parts.any { it.equals(needle, ignoreCase = true) }
        return when (rule.operator) {
            SmartOperator.CONTAINS -> whole.contains(needle, ignoreCase = true)
            SmartOperator.NOT_CONTAINS -> !whole.contains(needle, ignoreCase = true)
            SmartOperator.IS -> equals
            SmartOperator.IS_NOT -> !equals
            SmartOperator.STARTS_WITH -> whole.trim().startsWith(needle, ignoreCase = true) ||
                parts.any { it.startsWith(needle, ignoreCase = true) }
            else -> true
        }
    }

    private fun textOf(field: SmartField, track: Track): String = when (field) {
        SmartField.TITLE -> track.title
        SmartField.ARTIST -> track.artist
        SmartField.ALBUM -> track.album
        SmartField.ALBUM_ARTIST -> track.albumArtist.ifBlank { track.artist }
        SmartField.GENRE -> track.genre
        SmartField.FOLDER -> track.path.substringBeforeLast('/', track.path)
        SmartField.FORMAT -> track.format
        else -> ""
    }

    private fun sort(tracks: List<Track>, playlist: SmartPlaylist, plays: Map<String, TrackPlays>, now: Long): List<Track> {
        if (playlist.sort == SmartSort.RANDOM) {
            val seed = playlist.id.hashCode().toLong() * 31L + now / DAY_MS
            return tracks.shuffled(Random(seed))
        }
        val byTitle = compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
        val primary: Comparator<Track> = when (playlist.sort) {
            SmartSort.TITLE -> byTitle
            SmartSort.ARTIST -> compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
            SmartSort.ALBUM -> compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.album }
                .thenBy { it.discNumber ?: 0 }
                .thenBy { it.trackNumber ?: 0 }
            SmartSort.YEAR -> compareBy<Track> { it.year ?: 0 }
            SmartSort.DATE_ADDED -> compareBy<Track> { it.dateAddedMs }
            SmartSort.PLAY_COUNT -> compareBy<Track> { plays[it.id]?.count ?: 0 }
                .thenBy { plays[it.id]?.lastPlayedAt ?: 0L }
            SmartSort.LAST_PLAYED -> compareBy<Track> { plays[it.id]?.lastPlayedAt ?: 0L }
            SmartSort.DURATION -> compareBy<Track> { it.durationMs }
            SmartSort.RANDOM -> byTitle
        }
        val directed = if (playlist.descending) primary.reversed() else primary
        return tracks.sortedWith(directed.then(byTitle))
    }

    fun decades(tracks: List<Track>): List<Int> =
        tracks.mapNotNullTo(HashSet()) { track -> track.year?.takeIf { it in 1000..9999 }?.let { it / 10 * 10 } }
            .sortedDescending()
}

object SmartPlaylistStore {
    private const val FILE_NAME = "smart_playlists.json"
    private const val DECADE_PREFIX = "decade:"
    private const val KEY_ID = "id"
    private const val KEY_NAME = "name"
    private const val KEY_RULES = "rules"
    private const val KEY_MATCH_ALL = "match_all"
    private const val KEY_SORT = "sort"
    private const val KEY_DESCENDING = "descending"
    private const val KEY_LIMIT = "limit"
    private const val KEY_PRESET = "preset"
    private const val KEY_CREATED = "created"
    private const val KEY_FIELD = "field"
    private const val KEY_OPERATOR = "operator"
    private const val KEY_VALUE = "value"
    private const val KEY_VALUE2 = "value2"

    var playlists by mutableStateOf<List<SmartPlaylist>>(emptyList())
        private set
    private var loaded = false
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val app = context.applicationContext
        appContext = app
        val file = File(app.filesDir, FILE_NAME)
        if (!file.exists()) {
            playlists = presets()
            persist()
            return
        }
        playlists = runCatching { parse(file.readText()) }.getOrDefault(presets())
    }

    fun decadeId(start: Int): String = "$DECADE_PREFIX$start"

    fun decade(start: Int): SmartPlaylist = SmartPlaylist(
        id = decadeId(start),
        name = "",
        rules = listOf(SmartRule(SmartField.YEAR, SmartOperator.BETWEEN, start.toString(), (start + 9).toString())),
        sort = SmartSort.YEAR,
        decade = start
    )

    fun get(id: String): SmartPlaylist? {
        if (id.startsWith(DECADE_PREFIX)) return id.removePrefix(DECADE_PREFIX).toIntOrNull()?.let { decade(it) }
        return playlists.firstOrNull { it.id == id }
    }

    fun newDraft(): SmartPlaylist = SmartPlaylist(
        id = "",
        name = "",
        rules = listOf(SmartRule(SmartField.GENRE, SmartOperator.CONTAINS))
    )

    fun save(context: Context, playlist: SmartPlaylist): SmartPlaylist {
        ensureLoaded(context)
        val existing = playlists.indexOfFirst { it.id == playlist.id }
        val stored = if (existing >= 0) {
            playlist
        } else {
            playlist.copy(id = UUID.randomUUID().toString(), decade = null, createdAt = System.currentTimeMillis())
        }
        playlists = if (existing >= 0) {
            playlists.toMutableList().also { it[existing] = stored }
        } else {
            playlists + stored
        }
        persist()
        return stored
    }

    fun delete(id: String) {
        playlists = playlists.filterNot { it.id == id }
        persist()
    }

    private fun presets(): List<SmartPlaylist> {
        val now = System.currentTimeMillis()
        return listOf(
            SmartPlaylist(
                id = UUID.randomUUID().toString(),
                name = "",
                rules = listOf(SmartRule(SmartField.PLAY_COUNT, SmartOperator.AT_LEAST, "1")),
                sort = SmartSort.PLAY_COUNT,
                descending = true,
                limit = 50,
                preset = SmartPreset.MOST_PLAYED,
                createdAt = now
            ),
            SmartPlaylist(
                id = UUID.randomUUID().toString(),
                name = "",
                rules = emptyList(),
                sort = SmartSort.DATE_ADDED,
                descending = true,
                limit = 50,
                preset = SmartPreset.RECENTLY_ADDED,
                createdAt = now
            ),
            SmartPlaylist(
                id = UUID.randomUUID().toString(),
                name = "",
                rules = listOf(SmartRule(SmartField.PLAY_COUNT, SmartOperator.IS, "0")),
                sort = SmartSort.RANDOM,
                limit = 100,
                preset = SmartPreset.NEVER_PLAYED,
                createdAt = now
            ),
            SmartPlaylist(
                id = UUID.randomUUID().toString(),
                name = "",
                rules = listOf(
                    SmartRule(SmartField.PLAY_COUNT, SmartOperator.AT_LEAST, "3"),
                    SmartRule(SmartField.LAST_PLAYED, SmartOperator.OLDER_THAN_DAYS, "90")
                ),
                sort = SmartSort.PLAY_COUNT,
                descending = true,
                limit = 50,
                preset = SmartPreset.FORGOTTEN_GEMS,
                createdAt = now
            )
        )
    }

    private fun persist() {
        val context = appContext ?: return
        val snapshot = playlists
        scope.launch {
            runCatching {
                val target = File(context.filesDir, FILE_NAME)
                val temp = File(context.filesDir, "$FILE_NAME.tmp")
                temp.writeText(serialize(snapshot))
                if (!temp.renameTo(target)) {
                    target.writeText(temp.readText())
                    temp.delete()
                }
            }
        }
    }

    private fun serialize(items: List<SmartPlaylist>): String {
        val array = JSONArray()
        items.forEach { playlist ->
            val rules = JSONArray()
            playlist.rules.forEach { rule ->
                rules.put(
                    JSONObject()
                        .put(KEY_FIELD, rule.field.key)
                        .put(KEY_OPERATOR, rule.operator.key)
                        .put(KEY_VALUE, rule.value)
                        .put(KEY_VALUE2, rule.value2)
                )
            }
            array.put(
                JSONObject()
                    .put(KEY_ID, playlist.id)
                    .put(KEY_NAME, playlist.name)
                    .put(KEY_RULES, rules)
                    .put(KEY_MATCH_ALL, playlist.matchAll)
                    .put(KEY_SORT, playlist.sort.key)
                    .put(KEY_DESCENDING, playlist.descending)
                    .put(KEY_LIMIT, playlist.limit)
                    .put(KEY_PRESET, playlist.preset?.key ?: "")
                    .put(KEY_CREATED, playlist.createdAt)
            )
        }
        return array.toString()
    }

    private fun parse(raw: String): List<SmartPlaylist> {
        val array = JSONArray(raw)
        return (0 until array.length()).mapNotNull { position ->
            val item = array.optJSONObject(position) ?: return@mapNotNull null
            val id = item.optString(KEY_ID).takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val rulesArray = item.optJSONArray(KEY_RULES)
            val rules = if (rulesArray == null) {
                emptyList()
            } else {
                (0 until rulesArray.length()).mapNotNull { index ->
                    val rule = rulesArray.optJSONObject(index) ?: return@mapNotNull null
                    val field = SmartField.fromKey(rule.optString(KEY_FIELD)) ?: return@mapNotNull null
                    val operator = SmartOperator.fromKey(rule.optString(KEY_OPERATOR))
                        ?.takeIf { it in field.operators } ?: field.operators.first()
                    SmartRule(field, operator, rule.optString(KEY_VALUE), rule.optString(KEY_VALUE2))
                }
            }
            SmartPlaylist(
                id = id,
                name = item.optString(KEY_NAME),
                rules = rules,
                matchAll = item.optBoolean(KEY_MATCH_ALL, true),
                sort = SmartSort.fromKey(item.optString(KEY_SORT)) ?: SmartSort.TITLE,
                descending = item.optBoolean(KEY_DESCENDING, false),
                limit = item.optInt(KEY_LIMIT, 0).coerceAtLeast(0),
                preset = SmartPreset.fromKey(item.optString(KEY_PRESET)),
                createdAt = item.optLong(KEY_CREATED)
            )
        }
    }
}
