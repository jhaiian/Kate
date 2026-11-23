package com.jhaiian.kate.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.jhaiian.kate.MainActivity
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.playback.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal val QueueIndexKey = ActionParameters.Key<Int>("queue_index")
internal val QuickPlayKey = ActionParameters.Key<String>("quick_play")

internal const val QUICK_SHUFFLE_ALL = "shuffle_all"
internal const val QUICK_FAVORITES = "favorites"
internal const val QUICK_RECENT = "recent"
internal const val QUICK_RESUME = "resume"
private const val RECENT_LIMIT = 100

internal fun openPlayerIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse("kate://widget/player")
        putExtra(MainActivity.EXTRA_OPEN_PLAYER, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

internal fun openAppIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
    }

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) { PlayerController.togglePlayPause(context) }
    }
}

class NextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) { PlayerController.withController(context) { it.seekToNext() } }
    }
}

class PreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) { PlayerController.withController(context) { it.seekToPrevious() } }
    }
}

class ShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) {
            PlayerController.withController(context) {
                PlayerController.toggleShuffle()
                WidgetState.updateModes(context)
            }
        }
    }
}

class RepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) {
            PlayerController.withController(context) {
                PlayerController.cycleRepeat()
                WidgetState.updateModes(context)
            }
        }
    }
}

class FavoriteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        withContext(Dispatchers.Main) {
            WidgetState.ensureLoaded(context)
            val trackId = WidgetState.snapshot.trackId ?: return@withContext
            FavoritesStore.ensureLoaded(context)
            FavoritesStore.toggle(context, trackId)
            WidgetState.refresh(context)
        }
    }
}

class PlayQueueItemAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val index = parameters[QueueIndexKey] ?: return
        withContext(Dispatchers.Main) {
            PlayerController.withController(context) { controller ->
                if (index in PlayerController.queue.indices) {
                    PlayerController.playQueueItem(index)
                } else if (index < controller.mediaItemCount) {
                    controller.seekTo(index, 0L)
                    controller.play()
                }
            }
        }
    }
}

class QuickPlayAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val kind = parameters[QuickPlayKey] ?: return
        if (kind == QUICK_RESUME) {
            withContext(Dispatchers.Main) {
                PlayerController.withController(context) { controller ->
                    if (!controller.isPlaying) {
                        if (controller.playbackState == androidx.media3.common.Player.STATE_IDLE) controller.prepare()
                        controller.play()
                    }
                }
            }
            return
        }
        val tracks = withContext(Dispatchers.IO) { runCatching { LibraryTrackCache.loadTracks(context) }.getOrDefault(emptyList()) }
        if (tracks.isEmpty()) {
            runCatching { context.startActivity(openAppIntent(context)) }
            return
        }
        withContext(Dispatchers.Main) {
            when (kind) {
                QUICK_SHUFFLE_ALL -> PlayerController.playShuffled(context, tracks)
                QUICK_FAVORITES -> {
                    FavoritesStore.ensureLoaded(context)
                    val favorites = FavoritesStore.favoriteTracks(tracks)
                    if (favorites.isEmpty()) {
                        runCatching { context.startActivity(openAppIntent(context)) }
                    } else {
                        PlayerController.playAll(context, favorites)
                    }
                }
                QUICK_RECENT -> PlayerController.playAll(context, tracks.sortedByDescending { it.dateAddedMs }.take(RECENT_LIMIT))
            }
        }
    }
}
