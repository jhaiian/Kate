package com.jhaiian.kate.ui.player

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.playback.PlayerController

object PlayerActions {
    const val NONE = "none"
    const val SHUFFLE = "shuffle"
    const val REPEAT = "repeat"
    const val FAVORITE = "favorite"
    const val LYRICS = "lyrics"
    const val SLEEP_TIMER = "sleep_timer"
    const val EQUALIZER = "equalizer"
    const val AUDIO = "audio"
    const val ADD_TO_PLAYLIST = "add_to_playlist"
    const val QUEUE = "queue"
    const val MENU = "menu"
    const val REWIND = "rewind"
    const val FORWARD = "forward"
    const val PREVIOUS = "previous"
    const val NEXT = "next"
    const val PLAY_PAUSE = "play_pause"
    const val VISUALIZER = "visualizer"

    val SideSlots = listOf(NONE, SHUFFLE, REPEAT, FAVORITE, LYRICS, SLEEP_TIMER, EQUALIZER, AUDIO, ADD_TO_PLAYLIST, QUEUE, MENU, REWIND, FORWARD, VISUALIZER)
    val InnerSlots = listOf(PREVIOUS, NEXT, REWIND, FORWARD)
    val QuickRow = listOf(LYRICS, SLEEP_TIMER, EQUALIZER, AUDIO, ADD_TO_PLAYLIST, QUEUE, FAVORITE, SHUFFLE, REPEAT, MENU, REWIND, FORWARD, VISUALIZER)
    val Gestures = listOf(NONE, PLAY_PAUSE, FAVORITE, NEXT, PREVIOUS, REWIND, FORWARD, SHUFFLE, REPEAT, LYRICS, QUEUE, SLEEP_TIMER, EQUALIZER, AUDIO, ADD_TO_PLAYLIST, MENU, VISUALIZER)
    val DefaultQuickRow = listOf(LYRICS, SLEEP_TIMER, EQUALIZER, AUDIO, ADD_TO_PLAYLIST)
    const val MAX_QUICK = 6

    @StringRes
    fun label(action: String): Int = when (action) {
        SHUFFLE -> R.string.player_shuffle
        REPEAT -> R.string.player_repeat
        FAVORITE -> R.string.player_add_to_favorites
        LYRICS -> R.string.player_lyrics
        SLEEP_TIMER -> R.string.player_menu_sleep_timer
        EQUALIZER -> R.string.eq_title
        AUDIO -> R.string.audio_quick_title
        ADD_TO_PLAYLIST -> R.string.playlist_add_to
        QUEUE -> R.string.queue_title
        MENU -> R.string.library_menu
        REWIND -> R.string.action_rewind
        FORWARD -> R.string.action_forward
        PREVIOUS -> R.string.player_previous
        NEXT -> R.string.player_next
        PLAY_PAUSE -> R.string.action_play_pause
        VISUALIZER -> R.string.visualizer_mode_action
        else -> R.string.action_none
    }

    fun icon(action: String, trackId: String? = null): ImageVector = when (action) {
        SHUFFLE -> Icons.Filled.Shuffle
        REPEAT -> if (PlayerController.repeatMode == 1) Icons.Filled.RepeatOne else Icons.Filled.Repeat
        FAVORITE -> if (trackId != null && FavoritesStore.isFavorite(trackId)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
        LYRICS -> Icons.Filled.FormatQuote
        SLEEP_TIMER -> Icons.Filled.Bedtime
        EQUALIZER -> Icons.Filled.GraphicEq
        AUDIO -> Icons.Filled.Speed
        ADD_TO_PLAYLIST -> Icons.AutoMirrored.Filled.PlaylistAdd
        QUEUE -> Icons.AutoMirrored.Filled.QueueMusic
        MENU -> Icons.Filled.MoreVert
        REWIND -> Icons.Filled.FastRewind
        FORWARD -> Icons.Filled.FastForward
        PREVIOUS -> Icons.Filled.SkipPrevious
        NEXT -> Icons.Filled.SkipNext
        PLAY_PAUSE -> if (PlayerController.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
        VISUALIZER -> Icons.Filled.BarChart
        else -> Icons.Filled.Block
    }

    fun seekBy(deltaMs: Long) {
        val duration = PlayerController.durationMs
        val target = PlayerController.estimatedPositionMs() + deltaMs
        PlayerController.seekTo(if (duration > 0L) target.coerceIn(0L, duration) else target.coerceAtLeast(0L))
    }

    fun run(action: String, handler: PlayerQuickActions, context: Context, trackId: String, seekStepMs: Long): Boolean {
        when (action) {
            SHUFFLE -> PlayerController.toggleShuffle()
            REPEAT -> PlayerController.cycleRepeat()
            FAVORITE -> FavoritesStore.toggle(context, trackId)
            LYRICS -> handler.onLyrics()
            SLEEP_TIMER -> handler.onSleepTimer()
            EQUALIZER -> handler.onEqualizer()
            AUDIO -> handler.onAudio()
            ADD_TO_PLAYLIST -> handler.onAddToPlaylist()
            QUEUE -> handler.onQueue()
            MENU -> handler.onMenu()
            REWIND -> seekBy(-seekStepMs)
            FORWARD -> seekBy(seekStepMs)
            PREVIOUS -> PlayerController.skipPrevious()
            NEXT -> PlayerController.skipNext()
            PLAY_PAUSE -> PlayerController.togglePlayPause()
            VISUALIZER -> {
                val modeLabel = context.getString(
                    when (VisualizerFocus.cycle(context)) {
                        VisualizerFocus.FADE -> R.string.visualizer_focus_fade
                        VisualizerFocus.FULL -> R.string.visualizer_focus_full
                        else -> R.string.visualizer_focus_off
                    }
                )
                android.widget.Toast.makeText(context, context.getString(R.string.visualizer_mode_toast, modeLabel), android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> return false
        }
        return true
    }
}

val LocalPlayerActions = staticCompositionLocalOf<PlayerQuickActions?> { null }

@androidx.compose.runtime.Composable
internal fun trackInfoLine(track: com.jhaiian.kate.data.Track, fields: Set<String>): String {
    if (fields.isEmpty()) return ""
    val context = androidx.compose.ui.platform.LocalContext.current
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_PLAYS in fields) {
        androidx.compose.runtime.LaunchedEffect(Unit) { com.jhaiian.kate.data.PlayCounts.refresh(context) }
    }
    val parts = mutableListOf<String>()
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_TRACK in fields) {
        track.trackNumber?.let { parts += androidx.compose.ui.res.stringResource(R.string.info_track_number, it) }
    }
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_YEAR in fields) track.year?.let { parts += it.toString() }
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_GENRE in fields && track.genre.isNotBlank()) parts += track.genre
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_FORMAT in fields && track.format.isNotBlank()) parts += track.format.uppercase()
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_QUALITY in fields) {
        val depth = track.bitDepth?.let { androidx.compose.ui.res.stringResource(R.string.song_info_bit_depth_value, it) }
        val rate = track.sampleRate?.let { androidx.compose.ui.res.stringResource(R.string.song_info_sample_rate_value, it / 1000f) }
        listOfNotNull(depth, rate).joinToString(" / ").takeIf { it.isNotEmpty() }?.let { parts += it }
    }
    if (com.jhaiian.kate.ui.theme.PlayerStylePreferences.INFO_PLAYS in fields) {
        val count = com.jhaiian.kate.data.PlayCounts.of(track.id)?.count ?: 0
        parts += androidx.compose.ui.res.pluralStringResource(R.plurals.info_plays, count, count)
    }
    return parts.joinToString(" · ")
}
