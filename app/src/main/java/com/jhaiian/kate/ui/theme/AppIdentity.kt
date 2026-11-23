package com.jhaiian.kate.ui.theme

import android.app.Activity
import android.app.LocaleManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object AppIcons {
    const val DEFAULT = "default"
    val Variants = listOf(DEFAULT, "midnight", "sunset", "ocean", "mint", "light")
    val Swatches = mapOf(
        DEFAULT to (0xFF1A1226 to 0xFF8B5CF6),
        "midnight" to (0xFF0B0B0F to 0xFF8B5CF6),
        "sunset" to (0xFFF97316 to 0xFF0F0A16),
        "ocean" to (0xFF0284C7 to 0xFF0F0A16),
        "mint" to (0xFF10B981 to 0xFF0F0A16),
        "light" to (0xFFF4F1FF to 0xFF8B5CF6)
    )

    private fun component(context: Context, variant: String): ComponentName =
        ComponentName(context.packageName, "com.jhaiian.kate.Launcher" + variant.replaceFirstChar { it.uppercase() })

    fun current(context: Context): String {
        val pm = context.packageManager
        return Variants.firstOrNull { variant ->
            val state = runCatching { pm.getComponentEnabledSetting(component(context, variant)) }.getOrDefault(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && variant == DEFAULT)
        } ?: DEFAULT
    }

    fun apply(context: Context, variant: String) {
        if (variant !in Variants) return
        val pm = context.packageManager
        runCatching {
            pm.setComponentEnabledSetting(component(context, variant), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            Variants.filter { it != variant }.forEach { other ->
                pm.setComponentEnabledSetting(component(context, other), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
        }
    }
}

object AppLanguage {
    const val SYSTEM = ""
    val Tags = listOf(SYSTEM, "en", "zh-CN", "fil", "hi", "id", "pt-BR", "ru", "es", "vi", "fr", "de", "ar")
    private const val PREFS_NAME = "app_language"
    private const val KEY_TAG = "tag"

    fun get(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java)?.applicationLocales
            val tag = locales?.takeIf { !it.isEmpty }?.get(0)?.language.orEmpty()
            return Tags.firstOrNull { it.isNotEmpty() && Locale.forLanguageTag(it).language == tag } ?: SYSTEM
        }
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_TAG, SYSTEM)?.takeIf { it in Tags } ?: SYSTEM
    }

    fun set(activity: Activity, tag: String) {
        if (tag !in Tags) return
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TAG, tag).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.getSystemService(LocaleManager::class.java)?.applicationLocales =
                if (tag.isEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        } else {
            activity.recreate()
        }
    }

    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val tag = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_TAG, SYSTEM).orEmpty()
        if (tag.isEmpty()) return base
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        return base.createConfigurationContext(configuration)
    }
}
