package com.jhaiian.kate.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jhaiian.kate.data.ArtworkColors

data class ClintColors(
    val background: Color,
    val onSurface: Color,
    val secondaryText: Color,
    val cardBackground: Color,
    val buttonBackground: Color,
    val surfaceVariant: Color,
    val popupBackground: Color,
    val primary: Color,
    val iconTint: Color,
    val divider: Color,
    val popupText: Color,
    val surface: Color,
    val buttonIconTint: Color,
    val popupStroke: Color,
    val popupCheck: Color,
    val onPrimary: Color,
    val colorError: Color,
    val colorErrorContainer: Color,
    val colorOnErrorContainer: Color,
    val buttonTextColor: Color,
    val addressBarColor: Color,
    val isLight: Boolean
)

val LocalClintColors = compositionLocalOf<ClintColors> {
    error("ClintComposeTheme not applied")
}

private fun ClintColors(resolved: ClintResolvedTheme) = ClintColors(
    background = resolved.background,
    onSurface = resolved.onSurface,
    secondaryText = resolved.secondaryText,
    cardBackground = resolved.cardBackground,
    buttonBackground = resolved.buttonBackground,
    surfaceVariant = resolved.surfaceVariant,
    popupBackground = resolved.popupBackground,
    primary = resolved.primary,
    iconTint = resolved.iconTint,
    divider = resolved.divider,
    popupText = resolved.popupText,
    surface = resolved.surface,
    buttonIconTint = resolved.buttonIconTint,
    popupStroke = resolved.popupStroke,
    popupCheck = resolved.popupCheck,
    onPrimary = resolved.onPrimary,
    colorError = resolved.error,
    colorErrorContainer = resolved.errorContainer,
    colorOnErrorContainer = resolved.onErrorContainer,
    buttonTextColor = resolved.buttonTextColor,
    addressBarColor = resolved.addressBar,
    isLight = resolved.isLight
)

private data class ClintThemeConfig(val theme: String, val intensity: String, val tuning: ThemeTuning = ThemeTuning())

private val LocalClintThemeConfig = staticCompositionLocalOf { ClintThemeConfig("dark", "soft_tint") }

private fun Int.orColor(fallback: Color): Color = if (this == ThemePreferences.NO_COLOR) fallback else Color(this)

private fun ClintColors.withOverrides(overrides: ColorOverrides): ClintColors {
    if (overrides == ColorOverrides()) return this
    val card = overrides.card.orColor(cardBackground)
    val text = overrides.text.orColor(onSurface)
    return copy(
        background = overrides.background.orColor(background),
        cardBackground = card,
        popupBackground = overrides.card.orColor(popupBackground),
        onSurface = text,
        popupText = overrides.text.orColor(popupText),
        secondaryText = overrides.secondaryText.orColor(secondaryText)
    )
}

private const val ColorFadeMillis = 650

@Composable
fun ClintComposeTheme(
    theme: String,
    appArtworkColors: Boolean = false,
    tuningOverride: ThemeTuning? = null,
    overridesOverride: ColorOverrides? = null,
    accentOverride: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val resolvedTheme = when (theme) {
        "light" -> "light"
        "dark" -> "dark"
        else -> if (systemDark) "dark" else "light"
    }
    val accent = accentOverride ?: ThemePreferences.getAccent(context)
    val intensity = ThemePreferences.getIntensity(context)
    val tuning = tuningOverride ?: ThemeTuning(
        ThemePreferences.getPaletteStyle(context),
        ThemePreferences.getContrast(context),
        ThemePreferences.getCustomAccent(context)
    )
    val overrides = overridesOverride ?: ThemePreferences.getColorOverrides(context)
    val customRevision = CustomThemeRepository.revision
    val customColors = remember(accent, resolvedTheme, customRevision) {
        resolveCustomColors(context, accent, resolvedTheme == "light")
    }
    val artworkSeed = if (appArtworkColors && customColors == null) ArtworkColors.seed else null
    val effectiveAccent = if (artworkSeed != null) "artwork" else accent

    val resolved = remember(resolvedTheme, effectiveAccent, intensity, artworkSeed, tuning) {
        resolveClintTheme(context, resolvedTheme, effectiveAccent, intensity, artworkSeed, tuning)
    }
    val clintColors = remember(resolved, overrides, customColors) {
        (customColors ?: ClintColors(resolved)).withOverrides(overrides)
    }

    CompositionLocalProvider(LocalClintThemeConfig provides ClintThemeConfig(resolvedTheme, intensity, tuning)) {
        ProvideClintColors(animateClintColors(clintColors), content)
    }
}

@Composable
fun ArtworkColorScope(enabled: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val parent = LocalClintColors.current
    val config = LocalClintThemeConfig.current
    val artworkSeed = if (enabled) ArtworkColors.seed else null
    val target = if (artworkSeed == null) {
        parent
    } else {
        remember(config, artworkSeed) {
            ClintColors(resolveClintTheme(context, config.theme, "artwork", config.intensity, artworkSeed, config.tuning))
        }
    }
    ProvideClintColors(animateClintColors(target), content)
}

@Composable
private fun animatedColor(target: Color): Color =
    animateColorAsState(
        target,
        if (UiVisibility.visible) tween(durationMillis = ColorFadeMillis) else snap(),
        label = "clint_color"
    ).value

@Composable
private fun animateClintColors(target: ClintColors): ClintColors = ClintColors(
    background = animatedColor(target.background),
    onSurface = animatedColor(target.onSurface),
    secondaryText = animatedColor(target.secondaryText),
    cardBackground = animatedColor(target.cardBackground),
    buttonBackground = animatedColor(target.buttonBackground),
    surfaceVariant = animatedColor(target.surfaceVariant),
    popupBackground = animatedColor(target.popupBackground),
    primary = animatedColor(target.primary),
    iconTint = animatedColor(target.iconTint),
    divider = animatedColor(target.divider),
    popupText = animatedColor(target.popupText),
    surface = animatedColor(target.surface),
    buttonIconTint = animatedColor(target.buttonIconTint),
    popupStroke = animatedColor(target.popupStroke),
    popupCheck = animatedColor(target.popupCheck),
    onPrimary = animatedColor(target.onPrimary),
    colorError = animatedColor(target.colorError),
    colorErrorContainer = animatedColor(target.colorErrorContainer),
    colorOnErrorContainer = animatedColor(target.colorOnErrorContainer),
    buttonTextColor = animatedColor(target.buttonTextColor),
    addressBarColor = animatedColor(target.addressBarColor),
    isLight = target.isLight
)

@Composable
internal fun ProvideClintColors(clintColors: ClintColors, content: @Composable () -> Unit) {
    val isLight = clintColors.isLight
    val base = if (isLight) lightColorScheme() else darkColorScheme()
    val colorScheme = base.copy(
        primary = clintColors.primary,
        onPrimary = clintColors.onPrimary,
        background = clintColors.background,
        onBackground = clintColors.onSurface,
        surface = clintColors.cardBackground,
        onSurface = clintColors.onSurface,
        surfaceVariant = clintColors.surfaceVariant,
        onSurfaceVariant = clintColors.secondaryText,
        error = clintColors.colorError,
        errorContainer = clintColors.colorErrorContainer,
        onErrorContainer = clintColors.colorOnErrorContainer
    )

    CompositionLocalProvider(LocalClintColors provides clintColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
