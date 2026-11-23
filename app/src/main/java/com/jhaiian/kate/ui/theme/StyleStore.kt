package com.jhaiian.kate.ui.theme

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.R
import com.jhaiian.kate.data.OutputRoute
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Modifier
import java.util.UUID

data class SavedStyle(val id: String, val name: String, val settings: LookAndFeelSettings)

data class BuiltInStyle(
    val key: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val transform: (LookAndFeelSettings) -> LookAndFeelSettings
)

data class StyleRules(
    val routes: Map<String, String> = emptyMap(),
    val dayStyle: String = "",
    val nightStyle: String = "",
    val nightStart: Int = 21,
    val nightEnd: Int = 7
)

private fun preset(key: String): ButtonPreset = ButtonPresets.first { it.key == key }

val BuiltInStyles = listOf(
    BuiltInStyle("original", R.string.style_original, R.string.style_original_desc) { current ->
        LookAndFeelSettings().copy(
            navTabOrder = current.navTabOrder,
            navHiddenTabs = current.navHiddenTabs,
            startScreen = current.startScreen,
            homeHiddenSections = current.homeHiddenSections,
            twoPane = current.twoPane
        )
    },
    BuiltInStyle("studio", R.string.style_studio, R.string.style_studio_desc) { current ->
        current.withButtonPreset(preset(ButtonStylePreferences.PRESET_MINIMAL)).copy(
            accent = "slate",
            appArtworkColors = false,
            intensity = "no_tint",
            fontStyle = DisplayPreferences.FONT_CONDENSED,
            playerLayout = PlayerStylePreferences.LAYOUT_MINIMAL,
            playerBackground = PlayerStylePreferences.BACKGROUND_PLAIN,
            seekStyle = SeekBarPreferences.STYLE_WAVEFORM
        )
    },
    BuiltInStyle("retro", R.string.style_retro, R.string.style_retro_desc) { current ->
        current.withButtonPreset(preset(ButtonStylePreferences.PRESET_SHARP)).copy(
            accent = "amber",
            appArtworkColors = false,
            fontStyle = DisplayPreferences.FONT_MONO,
            playerLayout = PlayerStylePreferences.LAYOUT_CASSETTE,
            playerBackground = PlayerStylePreferences.BACKGROUND_PLAIN,
            seekStyle = SeekBarPreferences.STYLE_DOTS,
            libraryCorners = LibraryStylePreferences.CORNERS_SHARP
        )
    },
    BuiltInStyle("vinyl", R.string.style_vinyl, R.string.style_vinyl_desc) { current ->
        current.withButtonPreset(preset(ButtonStylePreferences.PRESET_ROUNDED)).copy(
            accent = "copper",
            appArtworkColors = false,
            fontStyle = DisplayPreferences.FONT_SERIF,
            playerLayout = PlayerStylePreferences.LAYOUT_VINYL,
            playerBackground = PlayerStylePreferences.BACKGROUND_DRIFT,
            seekStyle = SeekBarPreferences.STYLE_SQUIGGLE,
            libraryCorners = LibraryStylePreferences.CORNERS_ROUND
        )
    },
    BuiltInStyle("midnight", R.string.style_midnight, R.string.style_midnight_desc) { current ->
        current.withButtonPreset(preset(ButtonStylePreferences.PRESET_PILL)).copy(
            theme = "dark",
            accent = "midnight",
            appArtworkColors = false,
            intensity = "pure_mode",
            playerBackground = PlayerStylePreferences.BACKGROUND_PLAIN,
            playerGlow = false,
            seekStyle = SeekBarPreferences.STYLE_PILL
        )
    },
    BuiltInStyle("pop", R.string.style_pop, R.string.style_pop_desc) { current ->
        current.withButtonPreset(preset(ButtonStylePreferences.PRESET_BOLD)).copy(
            accent = "material_you",
            appArtworkColors = true,
            playerArtworkColors = true,
            paletteStyle = ThemePreferences.STYLE_VIBRANT,
            playerLayout = PlayerStylePreferences.LAYOUT_BIG_ART,
            playerBackground = PlayerStylePreferences.BACKGROUND_ARTWORK,
            seekStyle = SeekBarPreferences.STYLE_SQUIGGLE,
            libraryCorners = LibraryStylePreferences.CORNERS_ROUND
        )
    }
)

object StyleCodec {
    const val FORMAT = "kate-style"
    private val LocalOnlyFields = setOf("appBackgroundImage", "playerBackgroundImage")

    private val fields by lazy {
        LookAndFeelSettings::class.java.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
    }

    fun toJson(settings: LookAndFeelSettings, portable: Boolean = false): JSONObject {
        val json = JSONObject()
        fields.forEach { field ->
            if (portable && field.name in LocalOnlyFields) return@forEach
            when (val value = field.get(settings)) {
                is String -> if (!portable || !FontStore.isFontKey(value)) json.put(field.name, value)
                is Boolean, is Int -> json.put(field.name, value)
                is Float -> json.put(field.name, value.toDouble())
                is Collection<*> -> json.put(field.name, JSONArray(value.map { it.toString() }))
            }
        }
        return json
    }

    fun fromJson(json: JSONObject, base: LookAndFeelSettings = LookAndFeelSettings()): LookAndFeelSettings {
        val target = base.copy()
        fields.forEach { field ->
            if (!json.has(field.name)) return@forEach
            val value: Any? = when (field.type) {
                String::class.java -> json.optString(field.name)
                java.lang.Boolean.TYPE -> json.optBoolean(field.name)
                Integer.TYPE -> json.optInt(field.name)
                java.lang.Float.TYPE -> json.optDouble(field.name).toFloat().takeIf { !it.isNaN() }
                Set::class.java -> json.optJSONArray(field.name)?.let { array -> List(array.length()) { array.optString(it) }.toSet() }
                List::class.java -> json.optJSONArray(field.name)?.let { array -> List(array.length()) { array.optString(it) } }
                else -> null
            }
            if (value != null) runCatching { field.set(target, value) }
        }
        return target
    }
}

object StyleStore {
    private const val FILE_NAME = "styles.json"
    private const val PREFS_NAME = "style_rules"
    private const val KEY_ROUTE_PREFIX = "route_"
    private const val KEY_DAY = "day_style"
    private const val KEY_NIGHT = "night_style"
    private const val KEY_NIGHT_START = "night_start"
    private const val KEY_NIGHT_END = "night_end"
    private const val KEY_LAST_AUTO = "last_auto"
    private const val KEY_SNAPSHOT = "snapshot"
    const val MAX_STYLES = 40
    val Routes = listOf(OutputRoute.SPEAKER, OutputRoute.WIRED, OutputRoute.BLUETOOTH, OutputRoute.USB)

    var styles by mutableStateOf<List<SavedStyle>>(emptyList())
        private set
    var rules by mutableStateOf(StyleRules())
        private set

    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val file = File(context.filesDir, FILE_NAME)
        styles = runCatching {
            val array = JSONObject(file.readText()).optJSONArray("styles") ?: JSONArray()
            List(array.length()) { array.getJSONObject(it) }.mapNotNull { parse(it) }
        }.getOrDefault(emptyList())
        val prefs = prefs(context)
        rules = StyleRules(
            routes = Routes.mapNotNull { route -> prefs.getString(KEY_ROUTE_PREFIX + route, null)?.let { route to it } }.toMap(),
            dayStyle = prefs.getString(KEY_DAY, "").orEmpty(),
            nightStyle = prefs.getString(KEY_NIGHT, "").orEmpty(),
            nightStart = prefs.getInt(KEY_NIGHT_START, 21).coerceIn(0, 23),
            nightEnd = prefs.getInt(KEY_NIGHT_END, 7).coerceIn(0, 23)
        )
    }

    fun reload(context: Context) {
        loaded = false
        ensureLoaded(context)
    }

    private fun parse(json: JSONObject): SavedStyle? {
        val settings = json.optJSONObject("settings") ?: return null
        val name = json.optString("name").trim().ifEmpty { return null }
        return SavedStyle(json.optString("id").ifEmpty { UUID.randomUUID().toString() }, name.take(60), StyleCodec.fromJson(settings))
    }

    private fun persist(context: Context) {
        val array = JSONArray()
        styles.forEach { style ->
            array.put(
                JSONObject()
                    .put("id", style.id)
                    .put("name", style.name)
                    .put("settings", StyleCodec.toJson(style.settings))
            )
        }
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            val temp = File(context.filesDir, "$FILE_NAME.tmp")
            temp.writeText(JSONObject().put("styles", array).toString())
            if (!temp.renameTo(file)) {
                file.writeText(temp.readText())
                temp.delete()
            }
        }
    }

    fun sanitize(context: Context, settings: LookAndFeelSettings): LookAndFeelSettings {
        settings.save(context)
        return LookAndFeelSettings.load(context)
    }

    fun save(context: Context, name: String, settings: LookAndFeelSettings): SavedStyle? {
        ensureLoaded(context)
        if (styles.size >= MAX_STYLES) return null
        val style = SavedStyle(UUID.randomUUID().toString(), name.trim().take(60), settings)
        styles = styles + style
        persist(context)
        return style
    }

    fun rename(context: Context, id: String, name: String) {
        ensureLoaded(context)
        styles = styles.map { if (it.id == id) it.copy(name = name.trim().take(60)) else it }
        persist(context)
    }

    fun overwrite(context: Context, id: String, settings: LookAndFeelSettings) {
        ensureLoaded(context)
        styles = styles.map { if (it.id == id) it.copy(settings = settings) else it }
        persist(context)
    }

    fun delete(context: Context, id: String) {
        ensureLoaded(context)
        styles = styles.filterNot { it.id == id }
        persist(context)
        val current = rules
        val cleaned = current.copy(
            routes = current.routes.filterValues { it != id },
            dayStyle = current.dayStyle.takeUnless { it == id }.orEmpty(),
            nightStyle = current.nightStyle.takeUnless { it == id }.orEmpty()
        )
        if (cleaned != current) setRules(context, cleaned)
    }

    fun find(id: String): SavedStyle? = styles.firstOrNull { it.id == id }

    fun encode(style: SavedStyle): String = JSONObject()
        .put("format", StyleCodec.FORMAT)
        .put("version", 1)
        .put("name", style.name)
        .put("settings", StyleCodec.toJson(style.settings, portable = true))
        .toString(2)

    fun export(context: Context, uri: Uri, style: SavedStyle): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri, "w")?.use { it.write(encode(style).toByteArray()) } != null
    }.getOrDefault(false)

    fun shareFile(context: Context, style: SavedStyle): File? = runCatching {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val safe = style.name.replace(Regex("[^A-Za-z0-9 _-]"), "").trim().ifEmpty { "Kate style" }
        File(dir, "$safe.kate-style.json").apply { writeText(encode(style)) }
    }.getOrNull()

    fun import(context: Context, uri: Uri): SavedStyle? {
        ensureLoaded(context)
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream -> stream.readBytes().take(512 * 1024).toByteArray().decodeToString() }
        }.getOrNull() ?: return null
        val json = runCatching { JSONObject(text) }.getOrNull() ?: return null
        if (json.optString("format") != StyleCodec.FORMAT) return null
        val settingsJson = json.optJSONObject("settings") ?: return null
        val name = json.optString("name").trim().ifEmpty { "Kate style" }
        return save(context, name, StyleCodec.fromJson(settingsJson))
    }

    fun setRules(context: Context, value: StyleRules) {
        rules = value
        val editor = prefs(context).edit()
        Routes.forEach { route ->
            val id = value.routes[route]
            if (id.isNullOrEmpty()) editor.remove(KEY_ROUTE_PREFIX + route) else editor.putString(KEY_ROUTE_PREFIX + route, id)
        }
        editor.putString(KEY_DAY, value.dayStyle)
            .putString(KEY_NIGHT, value.nightStyle)
            .putInt(KEY_NIGHT_START, value.nightStart)
            .putInt(KEY_NIGHT_END, value.nightEnd)
            .apply()
    }

    fun isNight(hour: Int, rules: StyleRules = this.rules): Boolean =
        if (rules.nightStart == rules.nightEnd) false
        else if (rules.nightStart < rules.nightEnd) hour >= rules.nightStart && hour < rules.nightEnd
        else hour >= rules.nightStart || hour < rules.nightEnd

    fun targetStyle(route: String, hour: Int): String? {
        val current = rules
        current.routes[route]?.takeIf { find(it) != null }?.let { return it }
        val timed = if (isNight(hour, current)) current.nightStyle else current.dayStyle
        return timed.takeIf { it.isNotEmpty() && find(it) != null }
    }

    fun saveSnapshot(context: Context, settings: LookAndFeelSettings) {
        prefs(context).edit().putString(KEY_SNAPSHOT, StyleCodec.toJson(settings).toString()).apply()
    }

    fun takeSnapshot(context: Context): LookAndFeelSettings? {
        val raw = prefs(context).getString(KEY_SNAPSHOT, null) ?: return null
        prefs(context).edit().remove(KEY_SNAPSHOT).apply()
        return runCatching { StyleCodec.fromJson(JSONObject(raw)) }.getOrNull()
    }

    fun lastAuto(context: Context): String = prefs(context).getString(KEY_LAST_AUTO, "").orEmpty()

    fun setLastAuto(context: Context, value: String) {
        prefs(context).edit().putString(KEY_LAST_AUTO, value).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
