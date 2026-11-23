package com.jhaiian.kate.playback

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
internal class SessionPlayer(
    player: Player,
    private val crossfader: Crossfader
) : ForwardingPlayer(player) {

    override fun play() {
        crossfader.playWithFade { super.play() }
    }

    override fun pause() {
        if (!crossfader.pauseWithFade()) super.pause()
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) {
            crossfader.playWithFade { super.setPlayWhenReady(true) }
        } else if (!crossfader.pauseWithFade()) {
            super.setPlayWhenReady(false)
        }
    }

    override fun seekToNext() {
        if (!crossfader.skipWithCrossfade()) super.seekToNext()
    }

    override fun seekToNextMediaItem() {
        if (!crossfader.skipWithCrossfade()) super.seekToNextMediaItem()
    }
}
