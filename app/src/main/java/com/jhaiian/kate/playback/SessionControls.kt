package com.jhaiian.kate.playback

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.SystemControls

@OptIn(UnstableApi::class)
internal object SessionControls {
    fun command(action: String): SessionCommand = SessionCommand(SystemControls.COMMAND_PREFIX + action, Bundle.EMPTY)

    fun actionOf(command: SessionCommand): String? =
        command.customAction.takeIf { it.startsWith(SystemControls.COMMAND_PREFIX) }?.removePrefix(SystemControls.COMMAND_PREFIX)

    fun buttons(context: Context, player: Player): List<CommandButton> =
        listOf(SystemControls.notificationFirst, SystemControls.notificationSecond)
            .filter { it != SystemControls.NONE }
            .distinct()
            .map { action ->
                CommandButton.Builder(icon(action, player))
                    .setDisplayName(context.getString(label(action)))
                    .setSessionCommand(command(action))
                    .build()
            }

    private fun icon(action: String, player: Player): Int = when (action) {
        SystemControls.FAVORITE -> {
            val id = player.currentMediaItem?.mediaId
            if (id != null && FavoritesStore.isFavorite(id)) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED
        }
        SystemControls.REWIND -> CommandButton.ICON_SKIP_BACK_10
        SystemControls.FORWARD -> CommandButton.ICON_SKIP_FORWARD_10
        SystemControls.STOP -> CommandButton.ICON_STOP
        else -> CommandButton.ICON_UNDEFINED
    }

    private fun label(action: String): Int = when (action) {
        SystemControls.FAVORITE -> R.string.player_add_to_favorites
        SystemControls.REWIND -> R.string.player_rewind_10
        SystemControls.FORWARD -> R.string.player_forward_10
        SystemControls.STOP -> R.string.control_stop
        SystemControls.NEXT -> R.string.player_next
        SystemControls.PREVIOUS -> R.string.player_previous
        SystemControls.PLAY_PAUSE -> R.string.action_play_pause
        else -> R.string.action_none
    }

    fun perform(context: Context, player: Player, action: String) {
        when (action) {
            SystemControls.PLAY_PAUSE -> if (player.playWhenReady) player.pause() else player.play()
            SystemControls.NEXT -> player.seekToNext()
            SystemControls.PREVIOUS -> player.seekToPrevious()
            SystemControls.REWIND -> player.seekTo((player.currentPosition - SystemControls.SEEK_STEP_MS).coerceAtLeast(0L))
            SystemControls.FORWARD -> {
                val duration = player.duration
                val target = player.currentPosition + SystemControls.SEEK_STEP_MS
                player.seekTo(if (duration != C.TIME_UNSET && duration > 0L) target.coerceAtMost(duration) else target)
            }
            SystemControls.FAVORITE -> player.currentMediaItem?.mediaId?.let { FavoritesStore.toggle(context, it) }
            SystemControls.STOP -> {
                player.pause()
                player.stop()
            }
        }
    }
}
