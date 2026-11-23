package com.jhaiian.kate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jhaiian.kate.ui.theme.LocalClintColors

private const val ShimmerMillis = 1100
private const val ShimmerHighlightAlpha = 0.09f

@Composable
fun BoxScope.LoadingShimmer() {
    val colors = LocalClintColors.current
    val transition = rememberInfiniteTransition(label = "art_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = ShimmerMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "art_shimmer_progress"
    )
    val base = colors.surfaceVariant
    val highlight = colors.onSurface.copy(alpha = ShimmerHighlightAlpha)
    Box(
        modifier = Modifier
            .matchParentSize()
            .drawBehind {
                drawRect(base)
                val start = -size.width + progress * size.width * 2f
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlight, Color.Transparent),
                        start = Offset(start, 0f),
                        end = Offset(start + size.width, size.height)
                    )
                )
            }
    )
}
