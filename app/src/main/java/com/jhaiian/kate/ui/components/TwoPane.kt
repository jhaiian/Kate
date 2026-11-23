package com.jhaiian.kate.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class VerticalFold(val leftPx: Float, val rightPx: Float, val separating: Boolean)

private val MinListPaneWidth = 280.dp
private val MinDetailPaneWidth = 320.dp
private val DividerTouchWidth = 16.dp
private const val FoldAcceptance = 0.75f

@Composable
fun rememberVerticalFold(): VerticalFold? {
    val activity = LocalActivity.current
    val layoutInfo: Flow<WindowLayoutInfo?> = remember(activity) {
        if (activity == null) flowOf(null) else WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
    }
    val info by layoutInfo.collectAsState(initial = null)
    return info?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull { it.orientation == FoldingFeature.Orientation.VERTICAL }
        ?.let { VerticalFold(it.bounds.left.toFloat(), it.bounds.right.toFloat(), it.isSeparating) }
}

@Composable
fun TwoPaneLayout(
    fold: VerticalFold?,
    split: Float,
    onSplitChange: (Float) -> Unit,
    onSplitSettled: () -> Unit,
    modifier: Modifier = Modifier,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    val colors = LocalClintColors.current
    val density = LocalDensity.current
    val haptics = LocalKateHaptics.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var originX by remember { mutableFloatStateOf(0f) }
    val totalHolder = remember { FloatArray(1) }
    val currentSplit by rememberUpdatedState(split)
    val currentOnSplitChange by rememberUpdatedState(onSplitChange)
    val minFirstPx = with(density) { MinListPaneWidth.toPx() }
    val minSecondPx = with(density) { MinDetailPaneWidth.toPx() }
    val handlePx = with(density) { DividerTouchWidth.toPx() }
    val dragState = rememberDraggableState { delta ->
        val totalPx = totalHolder[0]
        if (totalPx <= 0f) return@rememberDraggableState
        val signed = if (rtl) -delta else delta
        val next = (currentSplit * totalPx + signed) / totalPx
        val low = minFirstPx / totalPx
        val high = ((totalPx - minSecondPx - handlePx) / totalPx).coerceAtLeast(low)
        currentOnSplitChange(next.coerceIn(low, high))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val dragging by interactionSource.collectIsDraggedAsState()
    BoxWithConstraints(
        modifier = modifier.onGloballyPositioned { originX = it.positionInWindow().x }
    ) {
        val total = constraints.maxWidth.toFloat()
        totalHolder[0] = total
        val foldLocal = fold?.let { (it.leftPx - originX) to (it.rightPx - originX) }
            ?.takeIf { (left, right) ->
                left > minFirstPx * FoldAcceptance && right < total - minSecondPx * FoldAcceptance
            }
        val firstPx: Float
        val gapPx: Float
        if (foldLocal != null) {
            val width = (foldLocal.second - foldLocal.first).coerceAtLeast(with(density) { 1.dp.toPx() })
            val center = (foldLocal.first + foldLocal.second) / 2f
            gapPx = width
            val leading = center - width / 2f
            firstPx = if (rtl) total - leading - width else leading
        } else {
            gapPx = handlePx
            val high = (total - minSecondPx - handlePx).coerceAtLeast(minFirstPx)
            firstPx = (total * split).coerceIn(minFirstPx, high)
        }
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(with(density) { firstPx.toDp() }).fillMaxHeight()) { first() }
            if (foldLocal != null) {
                Box(
                    modifier = Modifier.width(with(density) { gapPx.toDp() }).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.divider))
                }
            } else {
                val handleColor by animateColorAsState(
                    targetValue = if (dragging) colors.primary else colors.secondaryText.copy(alpha = 0.4f),
                    label = "pane_handle_color"
                )
                val handleHeight by animateDpAsState(
                    targetValue = if (dragging) 64.dp else 40.dp,
                    label = "pane_handle_height"
                )
                Box(
                    modifier = Modifier
                        .width(DividerTouchWidth)
                        .fillMaxHeight()
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Horizontal,
                            interactionSource = interactionSource,
                            onDragStarted = { haptics.tick() },
                            onDragStopped = { onSplitSettled() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.divider))
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(handleHeight)
                            .clip(CircleShape)
                            .background(handleColor)
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { second() }
        }
    }
}

@Composable
fun PanePlaceholder(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(bottom = LocalBottomContentPadding.current)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp).padding(top = 20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp).padding(top = 8.dp)
        )
    }
}
