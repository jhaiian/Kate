package com.jhaiian.kate.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlayRecord

internal class ListeningRecorder(
    private val context: Context,
    private val playerProvider: () -> Player
) {
    private companion object {
        const val TICK_MS = 1_000L
        const val MAX_TICK_MS = 3_000L
        const val SAVE_EVERY_MS = 15_000L
        const val COUNT_AFTER_MS = 30_000L
        const val MIN_COUNT_MS = 5_000L
        const val WRAP_WINDOW_MS = 5_000L
    }

    private class ActivePlay(
        val id: Long,
        val mediaId: String,
        val index: Int,
        val startedAt: Long,
        val item: MediaItem
    ) {
        var durationMs = 0L
        var listenedMs = 0L
        var savedMs = -1L
        var lastPosition = 0L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var current: ActivePlay? = null
    private var lastTick = 0L
    private var lastId = 0L

    private val tick = object : Runnable {
        override fun run() {
            step()
            handler.postDelayed(this, TICK_MS)
        }
    }

    fun start() {
        ListeningHistory.ensureLoaded(context)
        lastTick = SystemClock.elapsedRealtime()
        handler.post(tick)
    }

    fun stop() {
        handler.removeCallbacks(tick)
        current?.let { finish(it) }
        current = null
    }

    private fun step() {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastTick).coerceIn(0L, MAX_TICK_MS)
        lastTick = now
        val player = playerProvider()
        val item = player.currentMediaItem
        if (item == null) {
            current?.let { finish(it) }
            current = null
            return
        }
        val index = player.currentMediaItemIndex
        val position = player.currentPosition
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0L }
        val active = current
        val wrapped = active != null &&
            duration != null &&
            active.lastPosition >= duration - WRAP_WINDOW_MS &&
            position < WRAP_WINDOW_MS
        val play = if (active == null || active.mediaId != item.mediaId || active.index != index || wrapped) {
            active?.let { finish(it) }
            ActivePlay(nextId(), item.mediaId, index, System.currentTimeMillis(), item).also { current = it }
        } else {
            active
        }
        if (duration != null) play.durationMs = duration
        if (player.isPlaying) play.listenedMs += delta
        play.lastPosition = position
        if (!ListeningHistory.enabled || play.listenedMs < threshold(play)) return
        if (play.savedMs < 0L || play.listenedMs - play.savedMs >= SAVE_EVERY_MS) save(play)
    }

    private fun finish(play: ActivePlay) {
        if (!ListeningHistory.enabled) return
        if (play.savedMs >= 0L && play.listenedMs > play.savedMs) save(play)
    }

    private fun threshold(play: ActivePlay): Long {
        val duration = play.durationMs
        if (duration <= 0L) return COUNT_AFTER_MS
        return minOf(COUNT_AFTER_MS, maxOf(duration / 2, MIN_COUNT_MS))
    }

    private fun save(play: ActivePlay) {
        val isNew = play.savedMs < 0L
        play.savedMs = play.listenedMs
        ListeningHistory.save(context, play.id, record(play), isNew)
    }

    private fun record(play: ActivePlay): PlayRecord {
        val metadata = play.item.mediaMetadata
        val extras = metadata.extras
        return PlayRecord(
            trackId = play.mediaId,
            title = metadata.title?.toString().orEmpty(),
            artist = extras?.getString(PlayerController.EXTRA_ARTIST) ?: metadata.artist?.toString().orEmpty(),
            album = extras?.getString(PlayerController.EXTRA_ALBUM) ?: metadata.albumTitle?.toString().orEmpty(),
            albumArtist = extras?.getString(PlayerController.EXTRA_ALBUM_ARTIST).orEmpty(),
            genre = extras?.getString(PlayerController.EXTRA_GENRE).orEmpty(),
            durationMs = play.durationMs,
            startedAt = play.startedAt,
            listenedMs = play.listenedMs
        )
    }

    private fun nextId(): Long {
        val candidate = System.currentTimeMillis() * 10
        lastId = if (candidate > lastId) candidate else lastId + 1
        return lastId
    }
}
