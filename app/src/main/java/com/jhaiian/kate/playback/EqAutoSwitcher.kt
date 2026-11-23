package com.jhaiian.kate.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.jhaiian.kate.data.EqAutoPresets
import com.jhaiian.kate.data.LibraryStore

internal class EqAutoSwitcher(context: Context) {
    private val appContext = context.applicationContext
    private var player: Player? = null

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            update(mediaItem)
        }
    }

    init {
        EqAutoPresets.ensureLoaded(appContext)
    }

    fun attach(next: Player) {
        if (player === next) return
        player?.removeListener(listener)
        player = next
        next.addListener(listener)
        update(next.currentMediaItem)
    }

    fun release() {
        player?.removeListener(listener)
        player = null
    }

    private fun update(mediaItem: MediaItem?) {
        if (EqAutoPresets.rules.isEmpty()) return
        val id = mediaItem?.mediaId
        LibraryStore.ensureLoaded(appContext)
        val track = id?.let { target -> LibraryStore.tracks.firstOrNull { it.id == target } }
        EqAutoPresets.onTrackChanged(appContext, track)
    }
}
