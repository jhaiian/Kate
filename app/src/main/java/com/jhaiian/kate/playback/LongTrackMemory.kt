package com.jhaiian.kate.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.TrackPositionStore

internal class LongTrackMemory(context: Context) {
    private companion object {
        const val SAMPLE_MS = 5_000L
        const val MIN_PROGRESS_MS = 30_000L
        const val END_MARGIN_MS = 30_000L
        const val START_WINDOW_MS = 5_000L
    }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var player: Player? = null
    private var trackedId: String? = null
    private var trackedPosition = 0L
    private var trackedDuration = 0L

    private val sampler = object : Runnable {
        override fun run() {
            sample()
            handler.postDelayed(this, SAMPLE_MS)
        }
    }

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem?.mediaId == trackedId && reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) return
            commit(finished = reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT)
            begin()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            sample()
            if (!isPlaying) commit(finished = false)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) commit(finished = true)
        }
    }

    fun attach(next: Player) {
        if (player === next) return
        player?.let {
            sample()
            it.removeListener(listener)
        }
        commit(finished = false)
        player = next
        next.addListener(listener)
        begin()
        handler.removeCallbacks(sampler)
        handler.postDelayed(sampler, SAMPLE_MS)
    }

    fun release() {
        handler.removeCallbacks(sampler)
        sample()
        commit(finished = false)
        player?.removeListener(listener)
        player = null
    }

    private fun minimumMs(): Long = AudioSettings.longTrackMinutes * 60_000L

    private fun sample() {
        val current = player ?: return
        val id = current.currentMediaItem?.mediaId ?: return
        if (id != trackedId) return
        trackedPosition = current.currentPosition
        current.duration.takeIf { it != C.TIME_UNSET && it > 0L }?.let { trackedDuration = it }
    }

    private fun commit(finished: Boolean) {
        val id = trackedId ?: return
        if (finished) {
            TrackPositionStore.remove(appContext, id)
            return
        }
        val minimum = minimumMs()
        if (minimum <= 0L || trackedDuration < minimum) return
        if (trackedPosition < MIN_PROGRESS_MS || trackedPosition > trackedDuration - END_MARGIN_MS) {
            TrackPositionStore.remove(appContext, id)
        } else {
            TrackPositionStore.save(appContext, id, trackedPosition)
        }
    }

    private fun begin() {
        val current = player ?: return
        val item = current.currentMediaItem
        trackedId = item?.mediaId
        trackedPosition = current.currentPosition
        trackedDuration = durationOf(current, item)
        val id = trackedId ?: return
        val minimum = minimumMs()
        if (minimum <= 0L || trackedDuration < minimum) return
        if (current.currentPosition > START_WINDOW_MS) return
        val saved = TrackPositionStore.get(appContext, id) ?: return
        if (saved >= trackedDuration - END_MARGIN_MS) return
        current.seekTo(saved)
        trackedPosition = saved
    }

    private fun durationOf(current: Player, item: MediaItem?): Long {
        val stored = item?.mediaMetadata?.extras?.getLong(PlayerController.EXTRA_DURATION, 0L) ?: 0L
        if (stored > 0L) return stored
        return current.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
    }
}
