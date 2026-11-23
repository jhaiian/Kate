package com.jhaiian.kate.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.AudioEffects

internal class VolumeWatcher(context: Context, private val crossfader: Crossfader) {
    private companion object {
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
    }

    private val appContext = context.applicationContext
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var lastVolume = -1
    private var pausedByMute = false
    private var registered = false

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            check()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            check()
        }
    }

    fun start() {
        if (registered) return
        registered = true
        lastVolume = currentVolume()
        publishFraction(lastVolume)
        appContext.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        ContextCompat.registerReceiver(appContext, receiver, IntentFilter(VOLUME_CHANGED_ACTION), ContextCompat.RECEIVER_EXPORTED)
    }

    fun stop() {
        if (!registered) return
        registered = false
        appContext.contentResolver.unregisterContentObserver(observer)
        runCatching { appContext.unregisterReceiver(receiver) }
        pausedByMute = false
    }

    private fun publishFraction(volume: Int) {
        val max = runCatching { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }.getOrDefault(0)
        if (volume >= 0 && max > 0) AudioEffects.setVolumeFraction(volume.toFloat() / max)
    }

    private fun currentVolume(): Int = runCatching { audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) }.getOrDefault(-1)

    private fun check() {
        val volume = currentVolume()
        if (volume < 0 || volume == lastVolume) return
        val previous = lastVolume
        lastVolume = volume
        publishFraction(volume)
        FineVolume.onSystemVolume(volume)
        val player = crossfader.player
        if (volume == 0 && previous > 0) {
            if (AudioSettings.pauseWhenMuted && player.playWhenReady) {
                pausedByMute = true
                player.pause()
            }
        } else if (volume > 0 && pausedByMute) {
            pausedByMute = false
            if (AudioSettings.pauseWhenMuted && !player.playWhenReady) crossfader.play()
        }
    }
}
