package com.jhaiian.kate.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.settings.EqualizerScreen
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
internal fun EqualizerPanel(
    visible: Boolean,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 220)) + slideInVertically(
            spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
        ) { it / 10 },
        exit = fadeOut(tween(durationMillis = 180)) + slideOutVertically(tween(durationMillis = 180)) { it / 14 }
    ) {
        BackHandler(onBack = onClose)
        val colors = LocalClintColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        ) {
            CompositionLocalProvider(LocalBottomContentPadding provides 0.dp) {
                EqualizerScreen(
                    onBack = onClose,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                )
            }
        }
    }
}
