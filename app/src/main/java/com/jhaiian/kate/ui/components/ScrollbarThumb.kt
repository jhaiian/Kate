package com.jhaiian.kate.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

private data class ThumbMetrics(
    val top: Float,
    val height: Float,
    val maxOffset: Float,
    val averageItemHeight: Float,
    val totalItems: Int
)

@Composable
fun BoxScope.VerticalScrollbar(
    state: LazyListState,
    thumbColor: Color,
    thickness: Dp = 9.dp,
    hitAreaWidth: Dp = 32.dp,
    minThumbHeight: Dp = 48.dp,
    edgePadding: Dp = 5.dp
) {
    if (!LocalLookAndFeel.current.showScrollbar) return
    var visible by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        var hideJob: Job? = null
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .collect {
                visible = true
                hideJob?.cancel()
                hideJob = launch {
                    delay(900)
                    visible = false
                }
            }
    }

    val shown = visible || dragging
    val thumbAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = if (shown) 120 else 400),
        label = "scrollbar_alpha"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.4f,
        animationSpec = tween(durationMillis = if (shown) 160 else 300),
        label = "scrollbar_scale"
    )
    val thumbWidth by animateDpAsState(
        targetValue = if (dragging) thickness * 1.4f else thickness,
        animationSpec = tween(durationMillis = 150),
        label = "scrollbar_width"
    )
    val gradientBrush = remember(thumbColor) {
        Brush.verticalGradient(
            listOf(
                lerp(thumbColor, Color.White, 0.28f),
                thumbColor,
                lerp(thumbColor, Color.Black, 0.18f)
            )
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(hitAreaWidth)
            .pointerInput(state) {
                val minThumbPx = minThumbHeight.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val viewportHeightPx = state.layoutInfo.viewportSize.height.toFloat()
                    val metrics = computeThumbMetrics(state, viewportHeightPx, minThumbPx)
                        ?: return@awaitEachGesture
                    val downY = down.position.y
                    val grabOffset = if (downY in metrics.top..(metrics.top + metrics.height)) {
                        downY - metrics.top
                    } else {
                        metrics.height / 2f
                    }
                    down.consume()
                    dragging = true
                    moveThumbTo(state, scope, metrics, downY - grabOffset)
                    drag(down.id) { change ->
                        val liveViewport = state.layoutInfo.viewportSize.height.toFloat()
                        val liveMetrics = computeThumbMetrics(state, liveViewport, minThumbPx) ?: metrics
                        moveThumbTo(state, scope, liveMetrics, change.position.y - grabOffset)
                        change.consume()
                    }
                    dragging = false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = edgePadding)
                .layout { measurable, constraints ->
                    val viewportHeightPx = state.layoutInfo.viewportSize.height.toFloat()
                    val minThumbPx = minThumbHeight.toPx()
                    val metrics = computeThumbMetrics(state, viewportHeightPx, minThumbPx)
                    val thumbHeightPx = metrics?.height?.roundToInt()?.coerceAtLeast(0) ?: 0
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = thumbHeightPx, maxHeight = thumbHeightPx)
                    )
                    layout(placeable.width, viewportHeightPx.roundToInt().coerceAtLeast(0)) {
                        placeable.placeRelative(0, metrics?.top?.roundToInt() ?: 0)
                    }
                }
                .width(thumbWidth)
                .graphicsLayer {
                    alpha = thumbAlpha
                    scaleX = thumbScale
                }
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = thumbColor.copy(alpha = 0.45f),
                    spotColor = thumbColor.copy(alpha = 0.45f)
                )
                .background(brush = gradientBrush, shape = RoundedCornerShape(50))
        )
    }
}

@Composable
fun BoxScope.VerticalScrollbar(
    state: LazyGridState,
    thumbColor: Color,
    thickness: Dp = 9.dp,
    hitAreaWidth: Dp = 32.dp,
    minThumbHeight: Dp = 48.dp,
    edgePadding: Dp = 5.dp
) {
    if (!LocalLookAndFeel.current.showScrollbar) return
    var visible by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        var hideJob: Job? = null
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .collect {
                visible = true
                hideJob?.cancel()
                hideJob = launch {
                    delay(900)
                    visible = false
                }
            }
    }

    val shown = visible || dragging
    val thumbAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = if (shown) 120 else 400),
        label = "scrollbar_alpha"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.4f,
        animationSpec = tween(durationMillis = if (shown) 160 else 300),
        label = "scrollbar_scale"
    )
    val thumbWidth by animateDpAsState(
        targetValue = if (dragging) thickness * 1.4f else thickness,
        animationSpec = tween(durationMillis = 150),
        label = "scrollbar_width"
    )
    val gradientBrush = remember(thumbColor) {
        Brush.verticalGradient(
            listOf(
                lerp(thumbColor, Color.White, 0.28f),
                thumbColor,
                lerp(thumbColor, Color.Black, 0.18f)
            )
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(hitAreaWidth)
            .pointerInput(state) {
                val minThumbPx = minThumbHeight.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val viewportHeightPx = state.layoutInfo.viewportSize.height.toFloat()
                    val metrics = computeThumbMetrics(state, viewportHeightPx, minThumbPx)
                        ?: return@awaitEachGesture
                    val downY = down.position.y
                    val grabOffset = if (downY in metrics.top..(metrics.top + metrics.height)) {
                        downY - metrics.top
                    } else {
                        metrics.height / 2f
                    }
                    down.consume()
                    dragging = true
                    moveThumbTo(state, scope, metrics, downY - grabOffset)
                    drag(down.id) { change ->
                        val liveViewport = state.layoutInfo.viewportSize.height.toFloat()
                        val liveMetrics = computeThumbMetrics(state, liveViewport, minThumbPx) ?: metrics
                        moveThumbTo(state, scope, liveMetrics, change.position.y - grabOffset)
                        change.consume()
                    }
                    dragging = false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = edgePadding)
                .layout { measurable, constraints ->
                    val viewportHeightPx = state.layoutInfo.viewportSize.height.toFloat()
                    val minThumbPx = minThumbHeight.toPx()
                    val metrics = computeThumbMetrics(state, viewportHeightPx, minThumbPx)
                    val thumbHeightPx = metrics?.height?.roundToInt()?.coerceAtLeast(0) ?: 0
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = thumbHeightPx, maxHeight = thumbHeightPx)
                    )
                    layout(placeable.width, viewportHeightPx.roundToInt().coerceAtLeast(0)) {
                        placeable.placeRelative(0, metrics?.top?.roundToInt() ?: 0)
                    }
                }
                .width(thumbWidth)
                .graphicsLayer {
                    alpha = thumbAlpha
                    scaleX = thumbScale
                }
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = thumbColor.copy(alpha = 0.45f),
                    spotColor = thumbColor.copy(alpha = 0.45f)
                )
                .background(brush = gradientBrush, shape = RoundedCornerShape(50))
        )
    }
}

private fun computeThumbMetrics(
    state: LazyGridState,
    viewportHeightPx: Float,
    minThumbHeightPx: Float
): ThumbMetrics? {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0 || visibleItems.isEmpty()) return null
    var maxColumn = 0
    var currentRow = -1
    var currentRowHeight = 0
    var rowHeightSum = 0L
    var rowCount = 0
    for (item in visibleItems) {
        if (item.column > maxColumn) maxColumn = item.column
        if (item.row != currentRow) {
            if (currentRow != -1) {
                rowHeightSum += currentRowHeight
                rowCount++
            }
            currentRow = item.row
            currentRowHeight = item.size.height
        } else if (item.size.height > currentRowHeight) {
            currentRowHeight = item.size.height
        }
    }
    if (currentRow != -1) {
        rowHeightSum += currentRowHeight
        rowCount++
    }
    if (rowCount == 0) return null
    val columns = (maxColumn + 1).coerceAtLeast(1)
    val totalRows = (totalItems + columns - 1) / columns
    val averageRowHeight = rowHeightSum.toFloat() / rowCount
    val totalContentHeight = averageRowHeight * totalRows
    if (totalContentHeight <= viewportHeightPx) return null
    val firstItem = visibleItems.first()
    val scrolledPast = (firstItem.row * averageRowHeight) - firstItem.offset.y
    val thumbHeightRatio = (viewportHeightPx / totalContentHeight).coerceIn(0.05f, 1f)
    val thumbHeightPx = (viewportHeightPx * thumbHeightRatio).coerceAtLeast(minThumbHeightPx)
    val maxThumbOffset = (viewportHeightPx - thumbHeightPx).coerceAtLeast(0f)
    val scrollRange = (totalContentHeight - viewportHeightPx).coerceAtLeast(1f)
    val scrollProgress = (scrolledPast / scrollRange).coerceIn(0f, 1f)
    val thumbOffsetY = maxThumbOffset * scrollProgress
    return ThumbMetrics(thumbOffsetY, thumbHeightPx, maxThumbOffset, averageRowHeight, totalRows)
}

private fun moveThumbTo(
    state: LazyGridState,
    scope: CoroutineScope,
    metrics: ThumbMetrics,
    rawTop: Float
) {
    if (metrics.maxOffset <= 0f || metrics.averageItemHeight <= 0f || metrics.totalItems <= 0) return
    val clampedTop = rawTop.coerceIn(0f, metrics.maxOffset)
    val progress = clampedTop / metrics.maxOffset
    val scrollRange = metrics.averageItemHeight * metrics.totalItems
    val targetOffsetPx = progress * scrollRange
    val targetRow = (targetOffsetPx / metrics.averageItemHeight).toInt()
        .coerceIn(0, metrics.totalItems - 1)
    val rowOffset = (targetOffsetPx - targetRow * metrics.averageItemHeight)
        .toInt()
        .coerceAtLeast(0)
    val columns = (state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.column }?.plus(1)) ?: 1
    scope.launch {
        state.scrollToItem(targetRow * columns, rowOffset)
    }
}

private fun computeThumbMetrics(
    state: LazyListState,
    viewportHeightPx: Float,
    minThumbHeightPx: Float
): ThumbMetrics? {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0 || visibleItems.isEmpty()) return null
    val averageItemHeight = visibleItems.sumOf { it.size }.toFloat() / visibleItems.size
    val totalContentHeight = averageItemHeight * totalItems
    if (totalContentHeight <= viewportHeightPx) return null
    val firstItem = visibleItems.first()
    val scrolledPast = (firstItem.index * averageItemHeight) - firstItem.offset
    val thumbHeightRatio = (viewportHeightPx / totalContentHeight).coerceIn(0.05f, 1f)
    val thumbHeightPx = (viewportHeightPx * thumbHeightRatio).coerceAtLeast(minThumbHeightPx)
    val maxThumbOffset = (viewportHeightPx - thumbHeightPx).coerceAtLeast(0f)
    val scrollRange = (totalContentHeight - viewportHeightPx).coerceAtLeast(1f)
    val scrollProgress = (scrolledPast / scrollRange).coerceIn(0f, 1f)
    val thumbOffsetY = maxThumbOffset * scrollProgress
    return ThumbMetrics(thumbOffsetY, thumbHeightPx, maxThumbOffset, averageItemHeight, totalItems)
}

private fun moveThumbTo(
    state: LazyListState,
    scope: CoroutineScope,
    metrics: ThumbMetrics,
    rawTop: Float
) {
    if (metrics.maxOffset <= 0f || metrics.averageItemHeight <= 0f || metrics.totalItems <= 0) return
    val clampedTop = rawTop.coerceIn(0f, metrics.maxOffset)
    val progress = clampedTop / metrics.maxOffset
    val scrollRange = metrics.averageItemHeight * metrics.totalItems
    val targetOffsetPx = progress * scrollRange
    val targetIndex = (targetOffsetPx / metrics.averageItemHeight).toInt()
        .coerceIn(0, metrics.totalItems - 1)
    val itemOffset = (targetOffsetPx - targetIndex * metrics.averageItemHeight)
        .toInt()
        .coerceAtLeast(0)
    scope.launch {
        state.scrollToItem(targetIndex, itemOffset)
    }
}
