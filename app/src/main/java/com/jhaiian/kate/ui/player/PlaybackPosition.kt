package com.jhaiian.kate.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.jhaiian.kate.playback.PlayerController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private data class PlaybackSnapshot(val playing: Boolean, val positionMs: Long, val durationMs: Long)

@Composable
fun rememberPlaybackPositionMs(pixelSpan: () -> Float): State<Long> {
    val position = remember { mutableLongStateOf(PlayerController.estimatedPositionMs()) }
    val currentPixelSpan by rememberUpdatedState(pixelSpan)
    LaunchedEffect(Unit) {
        snapshotFlow {
            PlaybackSnapshot(PlayerController.isPlaying, PlayerController.positionMs, PlayerController.durationMs)
        }.collectLatest { snapshot ->
            position.longValue = PlayerController.estimatedPositionMs()
            if (snapshot.playing) {
                while (true) {
                    val span = currentPixelSpan().coerceAtLeast(1f)
                    val step = if (snapshot.durationMs > 0) {
                        (snapshot.durationMs / span).toLong().coerceIn(16L, 500L)
                    } else {
                        500L
                    }
                    delay(step)
                    position.longValue = PlayerController.estimatedPositionMs()
                }
            }
        }
    }
    return position
}
