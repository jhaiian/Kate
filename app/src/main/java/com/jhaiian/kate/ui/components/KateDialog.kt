package com.jhaiian.kate.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

@Composable
fun KateDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    cancelable: Boolean = true,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    BackHandler(enabled = visible) { if (cancelable) onDismiss() }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 220)),
        exit = fadeOut(tween(durationMillis = 180))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { if (cancelable) onDismiss() }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .animateEnterExit(
                        enter = scaleIn(
                            initialScale = 0.92f,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                        ) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        ) { it / 10 },
                        exit = scaleOut(
                            targetScale = 0.95f,
                            animationSpec = tween(durationMillis = 180)
                        ) + slideOutVertically(tween(durationMillis = 180)) { it / 16 }
                    )
            ) {
                content(this@AnimatedVisibility)
            }
        }
    }
}
