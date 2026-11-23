package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class EqRule(
    val scope: String,
    val key: String,
    val label: String,
    val gains: List<Float>,
    val preampDb: Float,
    val bassDb: Int,
    val trebleDb: Int
) {
    val id: String get() = "$scope|$key"
}

object EqAutoPresets {
    const val SCOPE_TRACK = "track"
    const val SCOPE_ALBUM = "album"
    const val SCOPE_FOLDER = "folder"
    const val SCOPE_GENRE = "genre"
    val Scopes = listOf(SCOPE_TRACK, SCOPE_ALBUM, SCOPE_FOLDER, SCOPE_GENRE)

    private const val PREFS_NAME = "eq_auto_presets"
    private const val KEY_RULES = "rules"
    private const val KEY_MANUAL = "manual"
    private const val KEY_LAST = "last"

    private var loaded = false
    var rules by mutableStateOf<List<EqRule>>(emptyList())
        private set

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        rules = runCatching { decode(JSONArray(prefs(context).getString(KEY_RULES, "[]"))) }.getOrDefault(emptyList())
    }

    fun keyFor(scope: String, track: Track): String? = when (scope) {
        SCOPE_TRACK -> track.id
        SCOPE_ALBUM -> track.album.takeIf { it.isNotBlank() }?.let { "${track.albumArtist.ifBlank { track.artist }.lowercase()}\u0001${it.lowercase()}" }
        SCOPE_FOLDER -> track.path.substringBeforeLast('/', "").takeIf { it.isNotEmpty() }
        SCOPE_GENRE -> track.genre.takeIf { it.isNotBlank() }?.lowercase()
        else -> null
    }

    fun labelFor(scope: String, track: Track): String = when (scope) {
        SCOPE_TRACK -> track.title
        SCOPE_ALBUM -> track.album
        SCOPE_FOLDER -> track.path.substringBeforeLast('/', "").substringAfterLast('/').substringAfterLast(':')
        else -> track.genre
    }

    fun link(context: Context, scope: String, track: Track): Boolean {
        ensureLoaded(context)
        EqualizerSettings.ensureLoaded(context)
        val key = keyFor(scope, track) ?: return false
        val rule = EqRule(
            scope = scope,
            key = key,
            label = labelFor(scope, track),
            gains = EqualizerSettings.gains,
            preampDb = EqualizerSettings.preampDb,
            bassDb = AudioSettings.bassBoostDb,
            trebleDb = AudioSettings.trebleBoostDb
        )
        rules = rules.filterNot { it.id == rule.id } + rule
        persist(context)
        prefs(context).edit().putString(KEY_LAST, rule.id).apply()
        return true
    }

    fun remove(context: Context, rule: EqRule) {
        ensureLoaded(context)
        rules = rules.filterNot { it.id == rule.id }
        persist(context)
    }

    fun resolve(track: Track): EqRule? {
        val current = rules
        if (current.isEmpty()) return null
        for (scope in Scopes) {
            val key = keyFor(scope, track) ?: continue
            current.firstOrNull { it.scope == scope && it.key == key }?.let { return it }
        }
        return null
    }

    fun onTrackChanged(context: Context, track: Track?) {
        ensureLoaded(context)
        if (rules.isEmpty()) return
        val prefs = prefs(context)
        val last = prefs.getString(KEY_LAST, "").orEmpty()
        val rule = track?.let { resolve(it) }
        if (rule == null) {
            if (last.isEmpty()) return
            prefs.getString(KEY_MANUAL, null)?.let { raw ->
                runCatching { decodeRule(JSONObject(raw)) }.getOrNull()?.let { apply(context, it) }
            }
            prefs.edit().putString(KEY_LAST, "").remove(KEY_MANUAL).apply()
            return
        }
        if (rule.id == last) return
        if (last.isEmpty()) {
            val manual = EqRule("manual", "", "", EqualizerSettings.gains, EqualizerSettings.preampDb, AudioSettings.bassBoostDb, AudioSettings.trebleBoostDb)
            prefs.edit().putString(KEY_MANUAL, encodeRule(manual).toString()).apply()
        }
        apply(context, rule)
        prefs.edit().putString(KEY_LAST, rule.id).apply()
    }

    private fun apply(context: Context, rule: EqRule) {
        EqualizerSettings.applySnapshot(context, rule.gains, rule.preampDb, rule.bassDb, rule.trebleDb)
    }

    private fun persist(context: Context) {
        prefs(context).edit().putString(KEY_RULES, JSONArray().apply { rules.forEach { put(encodeRule(it)) } }.toString()).apply()
    }

    private fun encodeRule(rule: EqRule): JSONObject = JSONObject()
        .put("scope", rule.scope)
        .put("key", rule.key)
        .put("label", rule.label)
        .put("gains", JSONArray(rule.gains.map { it.toDouble() }))
        .put("preamp", rule.preampDb.toDouble())
        .put("bass", rule.bassDb)
        .put("treble", rule.trebleDb)

    private fun decodeRule(json: JSONObject): EqRule {
        val gains = json.optJSONArray("gains") ?: JSONArray()
        return EqRule(
            scope = json.optString("scope"),
            key = json.optString("key"),
            label = json.optString("label"),
            gains = List(gains.length()) { gains.optDouble(it, 0.0).toFloat() },
            preampDb = json.optDouble("preamp", 0.0).toFloat(),
            bassDb = json.optInt("bass", 0),
            trebleDb = json.optInt("treble", 0)
        )
    }

    private fun decode(array: JSONArray): List<EqRule> =
        List(array.length()) { decodeRule(array.getJSONObject(it)) }.filter { it.scope in Scopes && it.key.isNotEmpty() }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
