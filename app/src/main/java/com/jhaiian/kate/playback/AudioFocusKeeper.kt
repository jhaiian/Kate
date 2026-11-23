package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import com.jhaiian.kate.data.AudioSettings

internal class AudioFocusKeeper(context: Context, private val crossfader: Crossfader) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setWillPauseWhenDucked(true)
        .setAcceptsDelayedFocusGain(false)
        .setOnAudioFocusChangeListener({ onFocusChange(it) }, handler)
        .build()
    private var player: Player? = null
    private var holding = false
    private var suspended = false
    private var pausedByLoss = false
    private var ducked = false

    private val playerListener = object : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            update(userAction = reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            update(userAction = false)
        }
    }

    fun attach(next: Player) {
        if (player === next) return
        player?.removeListener(playerListener)
        player = next
        next.addListener(playerListener)
        update(userAction = false)
    }

    fun refresh() {
        if (ducked) crossfader.setDucked(true)
        update(userAction = false)
    }

    fun release() {
        player?.removeListener(playerListener)
        player = null
        pausedByLoss = false
        abandon()
    }

    private fun update(userAction: Boolean) {
        val current = player ?: return
        if (AudioSettings.focusMode == AudioSettings.FOCUS_IGNORE) {
            pausedByLoss = false
            abandon()
            return
        }
        val state = current.playbackState
        val wants = current.playWhenReady && state != Player.STATE_IDLE && state != Player.STATE_ENDED
        if (!wants) {
            if (!pausedByLoss) abandon()
            return
        }
        if (holding && !(suspended && userAction)) return
        pausedByLoss = false
        val result = audioManager.requestAudioFocus(request)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            holding = true
            suspended = false
            setDucked(false)
        } else {
            holding = false
            current.pause()
        }
    }

    private fun onFocusChange(change: Int) {
        if (!holding) return
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                suspended = false
                setDucked(false)
                if (pausedByLoss) {
                    pausedByLoss = false
                    if (AudioSettings.resumeAfterInterruption) crossfader.play() else abandon()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausedByLoss = false
                abandon()
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                suspended = true
                if (AudioSettings.focusMode == AudioSettings.FOCUS_DUCK && audioManager.mode == AudioManager.MODE_NORMAL) {
                    setDucked(true)
                } else {
                    pauseForLoss()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                suspended = true
                setDucked(true)
            }
        }
    }

    private fun pauseForLoss() {
        val current = player ?: return
        if (!current.playWhenReady) return
        pausedByLoss = true
        current.pause()
    }

    private fun setDucked(value: Boolean) {
        if (ducked == value) return
        ducked = value
        crossfader.setDucked(value)
    }

    private fun abandon() {
        if (holding) audioManager.abandonAudioFocusRequest(request)
        holding = false
        suspended = false
        setDucked(false)
    }
}
