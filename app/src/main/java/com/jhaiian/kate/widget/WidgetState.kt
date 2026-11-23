package com.jhaiian.kate.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Player
import com.jhaiian.kate.playback.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class WidgetQueueItem(val index: Int, val title: String, val artist: String)

data class WidgetSnapshot(
    val trackId: String?,
    val title: String,
    val artist: String,
    val album: String,
    val artPath: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val sampledAt: Long,
    val shuffle: Boolean,
    val repeatMode: Int,
    val upNext: List<WidgetQueueItem>
) {
    val hasTrack: Boolean get() = trackId != null

    fun progress(): Float {
        if (durationMs <= 0L) return 0f
        val now = if (isPlaying) positionMs + (SystemClock.elapsedRealtime() - sampledAt) else positionMs
        return (now.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun withoutTiming(): WidgetSnapshot = copy(positionMs = 0L, sampledAt = 0L)

    companion object {
        val EMPTY = WidgetSnapshot(null, "", "", "", null, false, 0L, 0L, 0L, false, 0, emptyList())
    }
}

object WidgetState {
    private const val PREFS_NAME = "widget_state"
    private const val KEY_TRACK_ID = "track_id"
    private const val KEY_TITLE = "title"
    private const val KEY_ARTIST = "artist"
    private const val KEY_ALBUM = "album"
    private const val KEY_ART = "art"
    private const val KEY_POSITION = "position"
    private const val KEY_DURATION = "duration"
    private const val UPDATE_DELAY_MS = 120L

    var snapshot by mutableStateOf(WidgetSnapshot.EMPTY)
        private set

    private var loaded = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pending: Job? = null

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        val trackId = prefs.getString(KEY_TRACK_ID, null) ?: return
        snapshot = WidgetSnapshot(
            trackId = trackId,
            title = prefs.getString(KEY_TITLE, null).orEmpty(),
            artist = prefs.getString(KEY_ARTIST, null).orEmpty(),
            album = prefs.getString(KEY_ALBUM, null).orEmpty(),
            artPath = prefs.getString(KEY_ART, null),
            isPlaying = false,
            positionMs = prefs.getLong(KEY_POSITION, 0L),
            durationMs = prefs.getLong(KEY_DURATION, 0L),
            sampledAt = SystemClock.elapsedRealtime(),
            shuffle = PlayerController.shuffleEnabled,
            repeatMode = PlayerController.repeatMode,
            upNext = emptyList()
        )
    }

    fun publish(context: Context, next: WidgetSnapshot) {
        ensureLoaded(context)
        val previous = snapshot
        snapshot = next
        if (previous.withoutTiming() != next.withoutTiming() || !next.isPlaying) persist(context, next)
        refresh(context)
    }

    fun updateModes(context: Context) {
        ensureLoaded(context)
        snapshot = snapshot.copy(shuffle = PlayerController.shuffleEnabled, repeatMode = PlayerController.repeatMode)
        refresh(context)
    }

    fun refresh(context: Context) {
        val appContext = context.applicationContext
        pending?.cancel()
        pending = scope.launch {
            delay(UPDATE_DELAY_MS)
            KateWidgets.updateAll(appContext)
        }
    }

    private fun persist(context: Context, value: WidgetSnapshot) {
        val editor = prefs(context).edit()
        if (value.trackId == null) {
            editor.clear()
        } else {
            editor.putString(KEY_TRACK_ID, value.trackId)
                .putString(KEY_TITLE, value.title)
                .putString(KEY_ARTIST, value.artist)
                .putString(KEY_ALBUM, value.album)
                .putString(KEY_ART, value.artPath)
                .putLong(KEY_POSITION, value.positionMs)
                .putLong(KEY_DURATION, value.durationMs)
        }
        editor.apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

internal class WidgetBridge(context: Context) {
    private companion object {
        const val TICK_MS = 5_000L
        const val MAX_UP_NEXT = 12
    }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var player: Player? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish()
        }
    }

    private val tick = Runnable { publish() }

    fun attach(next: Player) {
        if (player === next) return
        player?.removeListener(listener)
        player = next
        next.addListener(listener)
        publish()
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        val last = player
        last?.removeListener(listener)
        player = null
        if (last != null) publishFrom(last, forcePaused = true)
    }

    private fun publish() {
        val current = player ?: return
        publishFrom(current, forcePaused = false)
    }

    private fun publishFrom(source: Player, forcePaused: Boolean) {
        handler.removeCallbacks(tick)
        val item = source.currentMediaItem
        if (item == null) {
            WidgetState.publish(
                appContext,
                if (source.mediaItemCount == 0) WidgetSnapshot.EMPTY else WidgetState.snapshot.copy(isPlaying = false)
            )
            return
        }
        val playing = !forcePaused && source.isPlaying
        val metadata = item.mediaMetadata
        val duration = source.duration.takeIf { it > 0 && it != C.TIME_UNSET } ?: 0L
        val startIndex = source.currentMediaItemIndex
        val end = minOf(source.mediaItemCount, startIndex + 1 + MAX_UP_NEXT)
        val upNext = (startIndex + 1 until end).map { index ->
            val next = source.getMediaItemAt(index).mediaMetadata
            WidgetQueueItem(index, next.title?.toString().orEmpty(), next.artist?.toString().orEmpty())
        }
        WidgetState.publish(
            appContext,
            WidgetSnapshot(
                trackId = item.mediaId,
                title = metadata.title?.toString().orEmpty(),
                artist = metadata.artist?.toString().orEmpty(),
                album = metadata.albumTitle?.toString().orEmpty(),
                artPath = metadata.artworkUri?.path,
                isPlaying = playing,
                positionMs = source.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                sampledAt = SystemClock.elapsedRealtime(),
                shuffle = PlayerController.shuffleEnabled,
                repeatMode = PlayerController.repeatMode,
                upNext = upNext
            )
        )
        if (playing) handler.postDelayed(tick, TICK_MS)
    }
}
