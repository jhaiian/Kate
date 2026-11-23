package com.jhaiian.kate.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class EqPreset(
    val key: String,
    @StringRes val nameRes: Int,
    val gains: List<Float>,
    val preampDb: Float,
    val bassDb: Int = 0,
    val trebleDb: Int = 0
)

data class CustomEqPreset(
    val id: String,
    val name: String,
    val gains: List<Float>,
    val preampDb: Float,
    val bassDb: Int = 0,
    val trebleDb: Int = 0
)

class EqualizerParams(val enabled: Boolean, val gains: FloatArray, val preampDb: Float)

object EqualizerMath {
    const val BAND_Q = 1.41
    const val BASS_SHELF_HZ = 90.0
    const val TREBLE_SHELF_HZ = 7_000.0
    private const val MAX_NYQUIST_FRACTION = 0.45

    fun shelf(gainDb: Double, sampleRate: Double, treble: Boolean): DoubleArray {
        val a = 10.0.pow(gainDb / 40.0)
        val tone = AudioEffects.params
        val frequency = if (treble) tone.trebleHz.toDouble().coerceAtMost(sampleRate * MAX_NYQUIST_FRACTION) else tone.bassHz.toDouble()
        val w0 = 2.0 * PI * frequency / sampleRate
        val cosW = cos(w0)
        val twoRootAAlpha = 2.0 * sqrt(a) * sin(w0) / 2.0 * sqrt(2.0)
        return if (treble) {
            val a0 = (a + 1.0) - (a - 1.0) * cosW + twoRootAAlpha
            doubleArrayOf(
                a * ((a + 1.0) + (a - 1.0) * cosW + twoRootAAlpha) / a0,
                -2.0 * a * ((a - 1.0) + (a + 1.0) * cosW) / a0,
                a * ((a + 1.0) + (a - 1.0) * cosW - twoRootAAlpha) / a0,
                2.0 * ((a - 1.0) - (a + 1.0) * cosW) / a0,
                ((a + 1.0) - (a - 1.0) * cosW - twoRootAAlpha) / a0
            )
        } else {
            val a0 = (a + 1.0) + (a - 1.0) * cosW + twoRootAAlpha
            doubleArrayOf(
                a * ((a + 1.0) - (a - 1.0) * cosW + twoRootAAlpha) / a0,
                2.0 * a * ((a - 1.0) - (a + 1.0) * cosW) / a0,
                a * ((a + 1.0) - (a - 1.0) * cosW - twoRootAAlpha) / a0,
                -2.0 * ((a - 1.0) + (a + 1.0) * cosW) / a0,
                ((a + 1.0) + (a - 1.0) * cosW - twoRootAAlpha) / a0
            )
        }
    }

    fun peaking(frequency: Double, gainDb: Double, sampleRate: Double, q: Double = BAND_Q): DoubleArray {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * frequency / sampleRate
        val alpha = sin(w0) / (2.0 * q)
        val cosW = cos(w0)
        val a0 = 1.0 + alpha / a
        return doubleArrayOf(
            (1.0 + alpha * a) / a0,
            (-2.0 * cosW) / a0,
            (1.0 - alpha * a) / a0,
            (-2.0 * cosW) / a0,
            (1.0 - alpha / a) / a0
        )
    }

    fun magnitudeDb(coefficients: DoubleArray, frequency: Double, sampleRate: Double): Double {
        val w = 2.0 * PI * frequency / sampleRate
        val cos1 = cos(w)
        val cos2 = cos(2.0 * w)
        val sin1 = sin(w)
        val sin2 = sin(2.0 * w)
        val b0 = coefficients[0]
        val b1 = coefficients[1]
        val b2 = coefficients[2]
        val a1 = coefficients[3]
        val a2 = coefficients[4]
        val numReal = b0 + b1 * cos1 + b2 * cos2
        val numImag = b1 * sin1 + b2 * sin2
        val denReal = 1.0 + a1 * cos1 + a2 * cos2
        val denImag = a1 * sin1 + a2 * sin2
        val numerator = numReal * numReal + numImag * numImag
        val denominator = (denReal * denReal + denImag * denImag).coerceAtLeast(1e-12)
        return 10.0 * log10((numerator / denominator).coerceAtLeast(1e-12))
    }

    fun responseDb(
        gains: List<Float>,
        preampDb: Float,
        frequency: Double,
        sampleRate: Double = 48_000.0,
        bassDb: Int = 0,
        trebleDb: Int = 0
    ): Double {
        var total = preampDb.toDouble()
        gains.forEachIndexed { band, gain ->
            if (gain != 0f) {
                total += magnitudeDb(peaking(EqualizerSettings.BandFrequencies[band].toDouble(), gain.toDouble(), sampleRate), frequency, sampleRate)
            }
        }
        if (bassDb > 0) total += magnitudeDb(shelf(bassDb.toDouble(), sampleRate, treble = false), frequency, sampleRate)
        if (trebleDb > 0) total += magnitudeDb(shelf(trebleDb.toDouble(), sampleRate, treble = true), frequency, sampleRate)
        return total
    }
}

private fun gainsOf(vararg values: Float): List<Float> = values.toList()

val BuiltInEqPresets = listOf(
    EqPreset("flat", R.string.eq_preset_flat, gainsOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), 0f),
    EqPreset("bass_boost", R.string.eq_preset_bass_boost, gainsOf(2f, 2f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), -6f, bassDb = 6),
    EqPreset("bass_reducer", R.string.eq_preset_bass_reducer, gainsOf(-6f, -5f, -4f, -2f, 0f, 0f, 0f, 0f, 0f, 0f), 0f),
    EqPreset("treble_boost", R.string.eq_preset_treble_boost, gainsOf(0f, 0f, 0f, 0f, 0f, 0f, 1f, 2f, 1f, 0f), -4f, trebleDb = 6),
    EqPreset("vocal", R.string.eq_preset_vocal, gainsOf(-2f, -2f, -1f, 0f, 2f, 4f, 4f, 3f, 1f, 0f), -3f, trebleDb = 1),
    EqPreset("rock", R.string.eq_preset_rock, gainsOf(3f, 3f, 2f, 1f, -1f, -1f, 1f, 2f, 2f, 2f), -5f, bassDb = 3, trebleDb = 3),
    EqPreset("pop", R.string.eq_preset_pop, gainsOf(-1f, 0f, 2f, 4f, 5f, 4f, 2f, 0f, -1f, -1f), -5f, bassDb = 2, trebleDb = 1),
    EqPreset("hip_hop", R.string.eq_preset_hip_hop, gainsOf(3f, 3f, 2f, 3f, -1f, -1f, 1f, 0f, 1f, 1f), -7f, bassDb = 5, trebleDb = 2),
    EqPreset("r_and_b", R.string.eq_preset_r_and_b, gainsOf(3f, 3f, 2f, 1f, -1f, 0f, 1f, 1f, 1f, 0f), -6f, bassDb = 4, trebleDb = 1),
    EqPreset("electronic", R.string.eq_preset_electronic, gainsOf(3f, 3f, 1f, 0f, -2f, 2f, 1f, 2f, 2f, 2f), -6f, bassDb = 4, trebleDb = 4),
    EqPreset("dance", R.string.eq_preset_dance, gainsOf(3f, 3f, 1f, 0f, -1f, 1f, 1f, 2f, 2f, 1f), -7f, bassDb = 5, trebleDb = 3),
    EqPreset("metal", R.string.eq_preset_metal, gainsOf(2f, 2f, 1f, -1f, -2f, 0f, 2f, 3f, 3f, 2f), -4f, bassDb = 3, trebleDb = 3),
    EqPreset("jazz", R.string.eq_preset_jazz, gainsOf(3f, 2f, 1f, 2f, -1f, -1f, 0f, 1f, 2f, 2f), -3f, bassDb = 2, trebleDb = 2),
    EqPreset("classical", R.string.eq_preset_classical, gainsOf(3f, 2f, 2f, 1f, -1f, -1f, 0f, 1f, 2f, 2f), -2f, bassDb = 1, trebleDb = 2),
    EqPreset("acoustic", R.string.eq_preset_acoustic, gainsOf(4f, 3f, 2f, 1f, 1f, 1f, 2f, 3f, 3f, 2f), -3f),
    EqPreset("podcast", R.string.eq_preset_podcast, gainsOf(-4f, -3f, -1f, 0f, 2f, 3f, 3f, 2f, 0f, -1f), -2f),
    EqPreset("small_speakers", R.string.eq_preset_small_speakers, gainsOf(-6f, -4f, 0f, 2f, 2f, 1f, 1f, 1f, 0f, 0f), -1f, trebleDb = 2),
    EqPreset("loudness", R.string.eq_preset_loudness, gainsOf(2f, 2f, 1f, 0f, -1f, -1f, 0f, 1f, 1f, 1f), -6f, bassDb = 6, trebleDb = 4),
    EqPreset("late_night", R.string.eq_preset_late_night, gainsOf(-3f, -2f, 0f, 1f, 2f, 3f, 3f, 2f, 0f, -2f), 0f)
)

object EqualizerSettings {
    val BandFrequencies = intArrayOf(31, 62, 125, 250, 500, 1_000, 2_000, 4_000, 8_000, 16_000)
    val BAND_COUNT = BandFrequencies.size
    const val MIN_GAIN_DB = -12f
    const val MAX_GAIN_DB = 12f
    const val MIN_PREAMP_DB = -12f
    const val MAX_PREAMP_DB = 6f
    const val PRESET_CUSTOM = "custom"
    const val MAX_CUSTOM_PRESETS = 20
    private const val MATCH_TOLERANCE = 0.05f
    private const val PREFS_NAME = "equalizer"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_GAINS = "gains"
    private const val KEY_PREAMP = "preamp"
    private const val KEY_CUSTOM_PRESETS = "custom_presets"
    private const val KEY_PER_DEVICE = "per_device"
    private const val KEY_ACTIVE_ROUTE = "active_route"
    private const val KEY_PROFILE_PREFIX = "profile_"
    private const val PROFILE_SEPARATOR = "|"
    private const val FIELD_ID = "id"
    private const val FIELD_NAME = "name"
    private const val FIELD_GAINS = "gains"
    private const val FIELD_PREAMP = "preamp"
    private const val FIELD_BASS = "bass"
    private const val FIELD_TREBLE = "treble"
    private const val SEPARATOR = ","

    private val flat = List(BAND_COUNT) { 0f }

    var enabled by mutableStateOf(false)
        private set
    var gains by mutableStateOf(flat)
        private set
    var preampDb by mutableFloatStateOf(0f)
        private set
    var customPresets by mutableStateOf<List<CustomEqPreset>>(emptyList())
        private set
    var perDevice by mutableStateOf(false)
        private set
    var activeRoute by mutableStateOf(OutputRoute.SPEAKER)
        private set

    @Volatile
    var params = EqualizerParams(false, FloatArray(BAND_COUNT), 0f)
        private set

    private var loaded = false

    val presetKey: String
        get() {
            val current = gains
            val preamp = preampDb
            val bass = AudioSettings.bassBoostDb
            val treble = AudioSettings.trebleBoostDb
            BuiltInEqPresets.firstOrNull {
                it.bassDb == bass && it.trebleDb == treble && matches(it.gains, it.preampDb, current, preamp)
            }?.let { return it.key }
            customPresets.firstOrNull {
                it.bassDb == bass && it.trebleDb == treble && matches(it.gains, it.preampDb, current, preamp)
            }?.let { return it.id }
            return PRESET_CUSTOM
        }

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        enabled = prefs.getBoolean(KEY_ENABLED, false)
        gains = parseGains(prefs.getString(KEY_GAINS, null)) ?: flat
        preampDb = prefs.getFloat(KEY_PREAMP, 0f).coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB)
        customPresets = parseCustom(prefs.getString(KEY_CUSTOM_PRESETS, null))
        perDevice = prefs.getBoolean(KEY_PER_DEVICE, false)
        activeRoute = prefs.getString(KEY_ACTIVE_ROUTE, null) ?: OutputRoute.SPEAKER
        publish()
    }

    fun setPerDevice(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (perDevice == value) return
        perDevice = value
        persist(context)
    }

    fun onRouteChanged(context: Context, route: String) {
        ensureLoaded(context)
        if (route == activeRoute) return
        activeRoute = route
        if (perDevice) {
            val raw = prefs(context).getString(KEY_PROFILE_PREFIX + route, null)
            val parts = raw?.split(PROFILE_SEPARATOR)
            val profileGains = parts?.getOrNull(2)?.let { parseGains(it) }
            if (parts != null && profileGains != null) {
                enabled = parts[0].toBooleanStrictOrNull() ?: enabled
                preampDb = (parts[1].toFloatOrNull() ?: preampDb).coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB)
                gains = profileGains
                publish()
            }
        }
        persist(context)
    }

    fun setEnabled(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (enabled == value) return
        enabled = value
        publish()
        persist(context)
    }

    fun setBandGain(context: Context, band: Int, gainDb: Float) {
        ensureLoaded(context)
        if (band !in 0 until BAND_COUNT) return
        val value = gainDb.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
        if (gains[band] == value && enabled) return
        gains = gains.toMutableList().also { it[band] = value }
        enabled = true
        publish()
        persist(context)
    }

    fun setPreamp(context: Context, gainDb: Float) {
        ensureLoaded(context)
        val value = gainDb.coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB)
        if (preampDb == value && enabled) return
        preampDb = value
        enabled = true
        publish()
        persist(context)
    }

    fun applyPreset(context: Context, key: String) {
        ensureLoaded(context)
        val builtIn = BuiltInEqPresets.firstOrNull { it.key == key }
        val custom = customPresets.firstOrNull { it.id == key }
        when {
            builtIn != null -> {
                gains = builtIn.gains
                preampDb = builtIn.preampDb
                AudioSettings.setBassBoostDb(context, builtIn.bassDb)
                AudioSettings.setTrebleBoostDb(context, builtIn.trebleDb)
            }
            custom != null -> {
                gains = custom.gains
                preampDb = custom.preampDb
                AudioSettings.setBassBoostDb(context, custom.bassDb)
                AudioSettings.setTrebleBoostDb(context, custom.trebleDb)
            }
            else -> return
        }
        enabled = true
        publish()
        persist(context)
    }

    fun applySnapshot(context: Context, snapshotGains: List<Float>, snapshotPreamp: Float, bassDb: Int, trebleDb: Int) {
        ensureLoaded(context)
        gains = List(BAND_COUNT) { snapshotGains.getOrElse(it) { 0f }.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB) }
        preampDb = snapshotPreamp.coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB)
        AudioSettings.setBassBoostDb(context, bassDb)
        AudioSettings.setTrebleBoostDb(context, trebleDb)
        enabled = true
        publish()
        persist(context)
    }

    fun reset(context: Context) {
        ensureLoaded(context)
        gains = flat
        preampDb = 0f
        publish()
        persist(context)
    }

    fun resetToDefaults(context: Context) {
        ensureLoaded(context)
        enabled = false
        gains = flat
        preampDb = 0f
        perDevice = false
        publish()
        persist(context)
    }

    fun saveCustomPreset(context: Context, name: String): CustomEqPreset? {
        ensureLoaded(context)
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        val existing = customPresets.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        val preset = CustomEqPreset(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = trimmed,
            gains = gains,
            preampDb = preampDb,
            bassDb = AudioSettings.bassBoostDb,
            trebleDb = AudioSettings.trebleBoostDb
        )
        customPresets = if (existing != null) {
            customPresets.map { if (it.id == existing.id) preset else it }
        } else {
            (customPresets + preset).takeLast(MAX_CUSTOM_PRESETS)
        }
        persist(context)
        return preset
    }

    fun deleteCustomPreset(context: Context, id: String) {
        ensureLoaded(context)
        val updated = customPresets.filterNot { it.id == id }
        if (updated.size == customPresets.size) return
        customPresets = updated
        persist(context)
    }

    private fun matches(presetGains: List<Float>, presetPreamp: Float, current: List<Float>, preamp: Float): Boolean {
        if (presetGains.size != current.size) return false
        if (abs(presetPreamp - preamp) > MATCH_TOLERANCE) return false
        return presetGains.indices.all { abs(presetGains[it] - current[it]) <= MATCH_TOLERANCE }
    }

    private fun publish() {
        params = EqualizerParams(enabled, gains.toFloatArray(), preampDb)
    }

    private fun persist(context: Context) {
        val custom = JSONArray()
        customPresets.forEach { preset ->
            custom.put(
                JSONObject()
                    .put(FIELD_ID, preset.id)
                    .put(FIELD_NAME, preset.name)
                    .put(FIELD_GAINS, preset.gains.joinToString(SEPARATOR))
                    .put(FIELD_PREAMP, preset.preampDb.toDouble())
                    .put(FIELD_BASS, preset.bassDb)
                    .put(FIELD_TREBLE, preset.trebleDb)
            )
        }
        val editor = prefs(context).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putString(KEY_GAINS, gains.joinToString(SEPARATOR))
            .putFloat(KEY_PREAMP, preampDb)
            .putString(KEY_CUSTOM_PRESETS, custom.toString())
            .putBoolean(KEY_PER_DEVICE, perDevice)
            .putString(KEY_ACTIVE_ROUTE, activeRoute)
        if (perDevice) {
            editor.putString(
                KEY_PROFILE_PREFIX + activeRoute,
                listOf(enabled.toString(), preampDb.toString(), gains.joinToString(SEPARATOR)).joinToString(PROFILE_SEPARATOR)
            )
        }
        editor.apply()
    }

    private fun parseGains(raw: String?): List<Float>? {
        val values = raw?.split(SEPARATOR)?.mapNotNull { it.trim().toFloatOrNull() } ?: return null
        if (values.size != BAND_COUNT) return null
        return values.map { it.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB) }
    }

    private fun parseCustom(raw: String?): List<CustomEqPreset> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val presetGains = parseGains(item.optString(FIELD_GAINS)) ?: return@mapNotNull null
                val name = item.optString(FIELD_NAME).takeIf { it.isNotBlank() } ?: return@mapNotNull null
                CustomEqPreset(
                    id = item.optString(FIELD_ID).ifBlank { UUID.randomUUID().toString() },
                    name = name,
                    gains = presetGains,
                    preampDb = item.optDouble(FIELD_PREAMP, 0.0).toFloat().coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB),
                    bassDb = item.optInt(FIELD_BASS, 0).coerceIn(0, AudioSettings.MAX_BASS_DB),
                    trebleDb = item.optInt(FIELD_TREBLE, 0).coerceIn(0, AudioSettings.MAX_TREBLE_DB)
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
