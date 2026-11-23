package com.jhaiian.kate.ui.player

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jhaiian.kate.ui.components.isItemInView
import com.jhaiian.kate.ui.components.JumpToPlayingButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.ReorderableTrackList
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
internal fun QueuePanel(
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
                .background(
                    Brush.verticalGradient(
                        listOf(colors.primary.copy(alpha = 0.16f), Color.Transparent, colors.primary.copy(alpha = 0.08f))
                    )
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QueueTopBar(onClose = onClose)
                QueueList(modifier = Modifier.weight(1f).widthIn(max = 720.dp).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun QueueTopBar(onClose: () -> Unit) {
    val colors = LocalClintColors.current
    val count = PlayerController.queue.size
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
    ) {
        KateIconButton(onClick = onClose) {
            KateIcon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.queue_close),
                tint = buttonTint(colors.onSurface),
                modifier = Modifier.size(28.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                text = stringResource(R.string.queue_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(R.plurals.library_song_count, count, count),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QueueList(modifier: Modifier = Modifier) {
    val haptics = LocalKateHaptics.current
    val queue = PlayerController.queue
    val currentIndex = PlayerController.currentQueueIndex()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentIndex - 1).coerceAtLeast(0))
    val scope = rememberCoroutineScope()
    val jumpVisible by remember(currentIndex) {
        derivedStateOf { currentIndex >= 0 && !listState.isItemInView(currentIndex) }
    }
    Box(modifier = modifier) {
        ReorderableTrackList(
            tracks = queue,
            currentIndex = currentIndex,
            currentLabel = stringResource(R.string.queue_now_playing),
            onPlay = { index ->
                if (index != currentIndex) {
                    haptics.click()
                    PlayerController.playQueueItem(index)
                }
            },
            onMove = { from, to -> PlayerController.moveQueueItem(from, to) },
            onRemove = { index -> PlayerController.removeQueueItem(index) },
            canRemove = { index -> index != currentIndex },
            modifier = Modifier.fillMaxSize(),
            listState = listState
        )
        JumpToPlayingButton(
            visible = jumpVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            onClick = { scope.launch { listState.animateScrollToItem(maxOf(0, currentIndex - 1)) } }
        )
    }
}
