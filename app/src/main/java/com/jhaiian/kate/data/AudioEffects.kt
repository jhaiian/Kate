package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class ParametricFilter(
    val type: String = AudioEffects.FILTER_PEAK,
    val frequency: Float = 1_000f,
    val gainDb: Float = 0f,
    val q: Float = 1f,
    val enabled: Boolean = true
)

data class EffectsParams(
    val crossfeed: Int = 0,
    val compressor: Int = 0,
    val loudness: Int = 0,
    val bassHz: Int = AudioEffects.DEFAULT_BASS_HZ,
    val trebleHz: Int = AudioEffects.DEFAULT_TREBLE_HZ,
    val parametricEnabled: Boolean = false,
    val parametricPreampDb: Float = 0f,
    val filters: List<ParametricFilter> = emptyList()
)

object AudioEffects {
    private const val PREFS_NAME = "audio_effects"
    private const val KEY_CROSSFEED = "crossfeed"
    private const val KEY_COMPRESSOR = "compressor"
    private const val KEY_LOUDNESS = "loudness"
    private const val KEY_BASS_HZ = "bass_hz"
    private const val KEY_TREBLE_HZ = "treble_hz"
    private const val KEY_PARAMETRIC_ENABLED = "parametric_enabled"
    private const val KEY_PARAMETRIC_PREAMP = "parametric_preamp"
    private const val KEY_FILTERS = "parametric_filters"

    const val FILTER_PEAK = "peak"
    const val FILTER_LOW_SHELF = "low_shelf"
    const val FILTER_HIGH_SHELF = "high_shelf"
    const val FILTER_LOW_PASS = "low_pass"
    const val FILTER_HIGH_PASS = "high_pass"
    const val FILTER_NOTCH = "notch"
    val FilterTypes = listOf(FILTER_PEAK, FILTER_LOW_SHELF, FILTER_HIGH_SHELF, FILTER_LOW_PASS, FILTER_HIGH_PASS, FILTER_NOTCH)

    const val DEFAULT_BASS_HZ = 90
    const val DEFAULT_TREBLE_HZ = 7_000
    const val MIN_BASS_HZ = 40
    const val MAX_BASS_HZ = 300
    const val MIN_TREBLE_HZ = 2_000
    const val MAX_TREBLE_HZ = 14_000
    const val MAX_FILTERS = 31
    const val MIN_FREQUENCY = 20f
    const val MAX_FREQUENCY = 20_000f
    const val MIN_FILTER_GAIN = -20f
    const val MAX_FILTER_GAIN = 20f
    const val MIN_Q = 0.1f
    const val MAX_Q = 10f
    const val MIN_PREAMP = -24f
    const val MAX_PREAMP = 6f

    private var loaded = false

    var crossfeed by mutableIntStateOf(0)
        private set
    var compressor by mutableIntStateOf(0)
        private set
    var loudness by mutableIntStateOf(0)
        private set
    var bassHz by mutableIntStateOf(DEFAULT_BASS_HZ)
        private set
    var trebleHz by mutableIntStateOf(DEFAULT_TREBLE_HZ)
        private set
    var parametricEnabled by mutableStateOf(false)
        private set
    var parametricPreampDb by mutableStateOf(0f)
        private set
    var filters by mutableStateOf<List<ParametricFilter>>(emptyList())
        private set

    @Volatile
    var params = EffectsParams()
        private set

    @Volatile
    var volumeFraction = 1f
        private set

    val blocksOffload: Boolean
        get() = crossfeed > 0 || compressor > 0 || loudness > 0 || (parametricEnabled && filters.any { it.enabled })

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        crossfeed = prefs.getInt(KEY_CROSSFEED, 0).coerceIn(0, 3)
        compressor = prefs.getInt(KEY_COMPRESSOR, 0).coerceIn(0, 3)
        loudness = prefs.getInt(KEY_LOUDNESS, 0).coerceIn(0, 100)
        bassHz = prefs.getInt(KEY_BASS_HZ, DEFAULT_BASS_HZ).coerceIn(MIN_BASS_HZ, MAX_BASS_HZ)
        trebleHz = prefs.getInt(KEY_TREBLE_HZ, DEFAULT_TREBLE_HZ).coerceIn(MIN_TREBLE_HZ, MAX_TREBLE_HZ)
        parametricEnabled = prefs.getBoolean(KEY_PARAMETRIC_ENABLED, false)
        parametricPreampDb = prefs.getFloat(KEY_PARAMETRIC_PREAMP, 0f).coerceIn(MIN_PREAMP, MAX_PREAMP)
        filters = runCatching { decodeFilters(JSONArray(prefs.getString(KEY_FILTERS, "[]"))) }.getOrDefault(emptyList())
        publish()
    }

    fun setVolumeFraction(value: Float) {
        volumeFraction = value.coerceIn(0f, 1f)
    }

    fun setCrossfeed(context: Context, value: Int) {
        ensureLoaded(context)
        crossfeed = value.coerceIn(0, 3)
        prefs(context).edit().putInt(KEY_CROSSFEED, crossfeed).apply()
        publish()
    }

    fun setCompressor(context: Context, value: Int) {
        ensureLoaded(context)
        compressor = value.coerceIn(0, 3)
        prefs(context).edit().putInt(KEY_COMPRESSOR, compressor).apply()
        publish()
    }

    fun setLoudness(context: Context, value: Int) {
        ensureLoaded(context)
        loudness = value.coerceIn(0, 100)
        prefs(context).edit().putInt(KEY_LOUDNESS, loudness).apply()
        publish()
    }

    fun setToneFrequencies(context: Context, bass: Int, treble: Int) {
        ensureLoaded(context)
        bassHz = bass.coerceIn(MIN_BASS_HZ, MAX_BASS_HZ)
        trebleHz = treble.coerceIn(MIN_TREBLE_HZ, MAX_TREBLE_HZ)
        prefs(context).edit().putInt(KEY_BASS_HZ, bassHz).putInt(KEY_TREBLE_HZ, trebleHz).apply()
        publish()
    }

    fun setParametricEnabled(context: Context, value: Boolean) {
        ensureLoaded(context)
        parametricEnabled = value
        prefs(context).edit().putBoolean(KEY_PARAMETRIC_ENABLED, value).apply()
        publish()
    }

    fun setParametricPreamp(context: Context, value: Float) {
        ensureLoaded(context)
        parametricPreampDb = value.coerceIn(MIN_PREAMP, MAX_PREAMP)
        prefs(context).edit().putFloat(KEY_PARAMETRIC_PREAMP, parametricPreampDb).apply()
        publish()
    }

    fun setFilters(context: Context, value: List<ParametricFilter>) {
        ensureLoaded(context)
        filters = value.take(MAX_FILTERS).map { sanitize(it) }
        prefs(context).edit().putString(KEY_FILTERS, encodeFilters(filters).toString()).apply()
        publish()
    }

    fun resetEffects(context: Context) {
        ensureLoaded(context)
        crossfeed = 0
        compressor = 0
        loudness = 0
        bassHz = DEFAULT_BASS_HZ
        trebleHz = DEFAULT_TREBLE_HZ
        prefs(context).edit()
            .putInt(KEY_CROSSFEED, 0)
            .putInt(KEY_COMPRESSOR, 0)
            .putInt(KEY_LOUDNESS, 0)
            .putInt(KEY_BASS_HZ, DEFAULT_BASS_HZ)
            .putInt(KEY_TREBLE_HZ, DEFAULT_TREBLE_HZ)
            .apply()
        publish()
    }

    private fun sanitize(filter: ParametricFilter): ParametricFilter = filter.copy(
        type = filter.type.takeIf { it in FilterTypes } ?: FILTER_PEAK,
        frequency = filter.frequency.coerceIn(MIN_FREQUENCY, MAX_FREQUENCY),
        gainDb = filter.gainDb.coerceIn(MIN_FILTER_GAIN, MAX_FILTER_GAIN),
        q = filter.q.coerceIn(MIN_Q, MAX_Q)
    )

    private fun publish() {
        params = EffectsParams(
            crossfeed = crossfeed,
            compressor = compressor,
            loudness = loudness,
            bassHz = bassHz,
            trebleHz = trebleHz,
            parametricEnabled = parametricEnabled,
            parametricPreampDb = parametricPreampDb,
            filters = filters
        )
    }

    private fun encodeFilters(list: List<ParametricFilter>): JSONArray = JSONArray().apply {
        list.forEach { filter ->
            put(
                JSONObject()
                    .put("type", filter.type)
                    .put("frequency", filter.frequency.toDouble())
                    .put("gain", filter.gainDb.toDouble())
                    .put("q", filter.q.toDouble())
                    .put("enabled", filter.enabled)
            )
        }
    }

    private fun decodeFilters(array: JSONArray): List<ParametricFilter> = List(array.length()) { index ->
        val item = array.getJSONObject(index)
        sanitize(
            ParametricFilter(
                type = item.optString("type", FILTER_PEAK),
                frequency = item.optDouble("frequency", 1_000.0).toFloat(),
                gainDb = item.optDouble("gain", 0.0).toFloat(),
                q = item.optDouble("q", 1.0).toFloat(),
                enabled = item.optBoolean("enabled", true)
            )
        )
    }

    private val AutoEqLine = Regex("""Filter\s*\d*\s*:\s*(ON|OFF)\s+([A-Z]+)\s+Fc\s+([\d.]+)\s*Hz(?:\s+Gain\s+(-?[\d.]+)\s*dB)?(?:\s+Q\s+([\d.]+))?""", RegexOption.IGNORE_CASE)
    private val PreampLine = Regex("""Preamp\s*:\s*(-?[\d.]+)\s*dB""", RegexOption.IGNORE_CASE)

    fun parseAutoEq(text: String): Pair<Float, List<ParametricFilter>>? {
        val preamp = PreampLine.find(text)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
        val parsed = AutoEqLine.findAll(text).mapNotNull { match ->
            val type = when (match.groupValues[2].uppercase(Locale.US)) {
                "PK", "PEQ" -> FILTER_PEAK
                "LS", "LSC", "LSQ" -> FILTER_LOW_SHELF
                "HS", "HSC", "HSQ" -> FILTER_HIGH_SHELF
                "LP", "LPQ" -> FILTER_LOW_PASS
                "HP", "HPQ" -> FILTER_HIGH_PASS
                "NO" -> FILTER_NOTCH
                else -> return@mapNotNull null
            }
            ParametricFilter(
                type = type,
                frequency = match.groupValues[3].toFloatOrNull() ?: return@mapNotNull null,
                gainDb = match.groupValues[4].toFloatOrNull() ?: 0f,
                q = match.groupValues[5].toFloatOrNull() ?: 0.707f,
                enabled = match.groupValues[1].equals("ON", ignoreCase = true)
            )
        }.toList()
        if (parsed.isEmpty()) return null
        return preamp to parsed
    }

    fun encodeAutoEq(preamp: Float, list: List<ParametricFilter>): String = buildString {
        append(String.format(Locale.US, "Preamp: %.1f dB\n", preamp))
        list.forEachIndexed { index, filter ->
            val code = when (filter.type) {
                FILTER_LOW_SHELF -> "LSC"
                FILTER_HIGH_SHELF -> "HSC"
                FILTER_LOW_PASS -> "LPQ"
                FILTER_HIGH_PASS -> "HPQ"
                FILTER_NOTCH -> "NO"
                else -> "PK"
            }
            append(
                String.format(
                    Locale.US,
                    "Filter %d: %s %s Fc %.0f Hz Gain %.1f dB Q %.2f\n",
                    index + 1,
                    if (filter.enabled) "ON" else "OFF",
                    code,
                    filter.frequency,
                    filter.gainDb,
                    filter.q
                )
            )
        }
    }

    fun importAutoEq(context: Context, uri: Uri): Boolean {
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().take(256 * 1024).toByteArray().decodeToString() }
        }.getOrNull() ?: return false
        val parsed = parseAutoEq(text) ?: return false
        setParametricPreamp(context, parsed.first)
        setFilters(context, parsed.second)
        setParametricEnabled(context, true)
        return true
    }

    fun exportAutoEq(context: Context, uri: Uri): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri, "w")?.use {
            it.write(encodeAutoEq(parametricPreampDb, filters).toByteArray())
        } != null
    }.getOrDefault(false)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
