package com.jhaiian.kate.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density

object UiVisibility {
    @Volatile
    var visible = false
        private set

    fun update(value: Boolean) {
        visible = value
    }
}

object AnimationSpeed : MotionDurationScale {
    @Volatile
    private var factor = 1f

    override val scaleFactor: Float
        get() = factor

    fun apply(context: Context, speed: String) {
        val system = runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
        factor = when (speed) {
            MotionPreferences.SPEED_OFF -> 0f
            MotionPreferences.SPEED_FAST -> system * 0.5f
            MotionPreferences.SPEED_SLOW -> system * 1.6f
            else -> system
        }
    }
}

fun kateFontFamily(style: String): FontFamily? = if (FontStore.isFontKey(style)) FontStore.family(style) else when (style) {
    DisplayPreferences.FONT_CONDENSED -> FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    DisplayPreferences.FONT_SERIF -> FontFamily.Serif
    DisplayPreferences.FONT_MONO -> FontFamily.Monospace
    DisplayPreferences.FONT_CASUAL -> FontFamily(Typeface.create("casual", Typeface.NORMAL))
    else -> null
}

private fun Typography.withFamily(family: FontFamily): Typography {
    fun TextStyle.f() = copy(fontFamily = family)
    return copy(
        displayLarge = displayLarge.f(),
        displayMedium = displayMedium.f(),
        displaySmall = displaySmall.f(),
        headlineLarge = headlineLarge.f(),
        headlineMedium = headlineMedium.f(),
        headlineSmall = headlineSmall.f(),
        titleLarge = titleLarge.f(),
        titleMedium = titleMedium.f(),
        titleSmall = titleSmall.f(),
        bodyLarge = bodyLarge.f(),
        bodyMedium = bodyMedium.f(),
        bodySmall = bodySmall.f(),
        labelLarge = labelLarge.f(),
        labelMedium = labelMedium.f(),
        labelSmall = labelSmall.f()
    )
}

@Composable
fun KateTextScope(fontStyle: String, textSize: String, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val base = MaterialTheme.typography
    val typography = remember(base, fontStyle) {
        val family = kateFontFamily(fontStyle)
        if (family == null) base else base.withFamily(family)
    }
    val scale = DisplayPreferences.textScale(textSize)
    val scaledDensity = remember(density, scale) { Density(density.density, density.fontScale * scale) }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme,
            shapes = MaterialTheme.shapes,
            typography = typography,
            content = content
        )
    }
}
