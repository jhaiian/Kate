package com.jhaiian.kate.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlin.math.abs
import kotlin.math.sign

private class ReorderEntry(val key: String, val track: Track)

private val ReorderEdgeZone = 56.dp
private val ReorderAutoScrollStep = 12.dp
private val ReorderRemoveVelocity = 900.dp
private const val ReorderRemoveFraction = 0.35f
private const val ReorderRemoveMillis = 160
private const val ReorderKeyPrefix = "reorder_track_"

private fun reorderEntries(tracks: List<Track>): List<ReorderEntry> {
    val seen = HashMap<String, Int>()
    return tracks.map { track ->
        val occurrence = seen.getOrDefault(track.id, 0)
        seen[track.id] = occurrence + 1
        ReorderEntry("$ReorderKeyPrefix${track.id}#$occurrence", track)
    }
}

@Composable
fun ReorderableTrackList(
    tracks: List<Track>,
    currentIndex: Int,
    currentLabel: String?,
    onPlay: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    canRemove: (Int) -> Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(top = 4.dp, bottom = 24.dp),
    header: LazyListScope.() -> Unit = {},
    footer: LazyListScope.() -> Unit = {}
) {
    val haptics = LocalKateHaptics.current
    val density = LocalDensity.current
    val entries = remember(tracks) { reorderEntries(tracks) }
    val currentKey = entries.getOrNull(currentIndex)?.key
    var order by remember(entries) { mutableStateOf(entries) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragDelta by remember { mutableFloatStateOf(0f) }
    val edgePx = with(density) { ReorderEdgeZone.toPx() }
    val autoScrollPx = with(density) { ReorderAutoScrollStep.toPx() }

    fun dragBy(amount: Float) {
        val key = draggingKey ?: return
        dragDelta += amount
        val layout = listState.layoutInfo
        val dragged = layout.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        val top = dragged.offset + dragDelta
        val bottom = top + dragged.size
        val scrolled = when {
            bottom > layout.viewportEndOffset - edgePx && amount > 0f -> listState.dispatchRawDelta(autoScrollPx)
            top < layout.viewportStartOffset + edgePx && amount < 0f -> listState.dispatchRawDelta(-autoScrollPx)
            else -> 0f
        }
        dragDelta += scrolled
        val center = dragged.offset + dragDelta + dragged.size / 2f
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { other ->
            val otherKey = other.key
            otherKey != key && otherKey is String && otherKey.startsWith(ReorderKeyPrefix) &&
                center >= other.offset && center <= other.offset + other.size
        } ?: return
        val from = order.indexOfFirst { it.key == key }
        val to = order.indexOfFirst { it.key == target.key }
        if (from < 0 || to < 0) return
        order = order.toMutableList().also { it.add(to, it.removeAt(from)) }
        dragDelta += dragged.offset - target.offset
        haptics.tick()
    }

    fun endDrag() {
        val key = draggingKey ?: return
        val to = order.indexOfFirst { it.key == key }
        val from = dragStartIndex
        draggingKey = null
        dragDelta = 0f
        dragStartIndex = -1
        if (from >= 0 && to >= 0 && from != to) {
            haptics.confirm()
            onMove(from, to)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header()
        itemsIndexed(order, key = { _, entry -> entry.key }) { _, entry ->
            val dragging = entry.key == draggingKey
            val sourceIndex = entries.indexOfFirst { it.key == entry.key }
            ReorderableTrackRow(
                track = entry.track,
                isCurrent = entry.key == currentKey,
                currentLabel = currentLabel,
                dragging = dragging,
                swipeEnabled = draggingKey == null && sourceIndex >= 0 && canRemove(sourceIndex),
                onClick = {
                    if (sourceIndex >= 0) onPlay(sourceIndex)
                },
                onRemove = {
                    if (sourceIndex >= 0) onRemove(sourceIndex)
                },
                handleModifier = Modifier.pointerInput(entry.key, entries) {
                    detectDragGestures(
                        onDragStart = {
                            draggingKey = entry.key
                            dragStartIndex = order.indexOfFirst { it.key == entry.key }
                            dragDelta = 0f
                            haptics.longPress()
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            dragBy(amount.y)
                        },
                        onDragEnd = { endDrag() },
                        onDragCancel = { endDrag() }
                    )
                },
                modifier = Modifier
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (dragging) dragDelta else 0f
                        shadowElevation = if (dragging) 12.dp.toPx() else 0f
                    }
                    .then(if (dragging) Modifier else Modifier.animateItem())
            )
        }
        footer()
    }
}

@Composable
private fun ReorderableTrackRow(
    track: Track,
    isCurrent: Boolean,
    currentLabel: String?,
    dragging: Boolean,
    swipeEnabled: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    handleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val density = LocalDensity.current
    var rowWidth by remember { mutableIntStateOf(1) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val removeVelocityPx = with(density) { ReorderRemoveVelocity.toPx() }
    val swipeState = rememberDraggableState { delta -> offsetX += delta }
    Box(
        modifier = modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .onSizeChanged { rowWidth = it.width.coerceAtLeast(1) }
    ) {
        if (offsetX != 0f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (offsetX > 0f) Arrangement.Start else Arrangement.End,
                modifier = Modifier
                    .matchParentSize()
                    .background(colors.colorErrorContainer)
                    .padding(horizontal = 20.dp)
            ) {
                KateIcon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    tint = colors.colorOnErrorContainer
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX }
                .background(
                    when {
                        dragging -> colors.cardBackground
                        isCurrent -> colors.primary.copy(alpha = 0.14f)
                        else -> colors.background
                    }
                )
                .draggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled,
                    onDragStopped = { velocity ->
                        val remove = abs(offsetX) > rowWidth * ReorderRemoveFraction ||
                            (abs(velocity) > removeVelocityPx && sign(velocity) == sign(offsetX))
                        if (remove) {
                            haptics.confirm()
                            animate(
                                initialValue = offsetX,
                                targetValue = sign(offsetX) * rowWidth,
                                animationSpec = tween(durationMillis = ReorderRemoveMillis)
                            ) { value, _ -> offsetX = value }
                            onRemove()
                            offsetX = 0f
                        } else {
                            animate(
                                initialValue = offsetX,
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                            ) { value, _ -> offsetX = value }
                        }
                    }
                )
                .clickable(onClick = onClick)
                .padding(start = 10.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AlbumArt(track = track, modifier = Modifier.fillMaxSize()) {
                    KateIcon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) colors.primary else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isCurrent && currentLabel != null) {
                        currentLabel
                    } else {
                        track.artist.ifBlank { stringResource(R.string.player_unknown_artist) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) colors.primary else colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isCurrent) {
                KateIcon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = handleModifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                KateIcon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = stringResource(R.string.queue_reorder),
                    tint = colors.secondaryText
                )
            }
        }
    }
}
