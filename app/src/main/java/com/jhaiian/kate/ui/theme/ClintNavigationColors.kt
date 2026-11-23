package com.jhaiian.kate.ui.theme

import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

@Composable
private fun navUnselectedColor(): Color {
    val colors = LocalClintColors.current
    return if (LocalLookAndFeel.current.buttonIconColor == ButtonStylePreferences.COLOR_ACCENT) {
        colors.primary.copy(alpha = 0.55f)
    } else {
        colors.secondaryText
    }
}

@Composable
private fun navIndicatorColor(): Color {
    val colors = LocalClintColors.current
    return if (LocalLookAndFeel.current.navIndicator == ButtonStylePreferences.NAV_PILL) {
        colors.primary.copy(alpha = 0.16f)
    } else {
        Color.Transparent
    }
}

@Composable
fun kateNavBarColor(): Color {
    val colors = LocalClintColors.current
    return when (LocalLookAndFeel.current.navBarColor) {
        ButtonStylePreferences.NAV_BAR_BACKGROUND -> colors.background
        ButtonStylePreferences.NAV_BAR_TINTED -> colors.primary.copy(alpha = 0.12f).compositeOver(colors.cardBackground)
        else -> colors.cardBackground
    }
}

@Composable
fun clintNavigationBarItemColors(): NavigationBarItemColors {
    val colors = LocalClintColors.current
    val unselected = navUnselectedColor()
    return NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = navIndicatorColor(),
        unselectedIconColor = unselected,
        unselectedTextColor = unselected,
        disabledIconColor = unselected.copy(alpha = 0.4f),
        disabledTextColor = unselected.copy(alpha = 0.4f)
    )
}

@Composable
fun clintNavigationRailItemColors(): NavigationRailItemColors {
    val colors = LocalClintColors.current
    val unselected = navUnselectedColor()
    return NavigationRailItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = navIndicatorColor(),
        unselectedIconColor = unselected,
        unselectedTextColor = unselected,
        disabledIconColor = unselected.copy(alpha = 0.4f),
        disabledTextColor = unselected.copy(alpha = 0.4f)
    )
}
