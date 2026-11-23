package com.jhaiian.kate.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.media3.common.Player
import com.jhaiian.kate.data.SystemControls

internal class HeadsetButtons(context: Context) {
    private companion object {
        const val PRESS_WINDOW_MS = 450L
    }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var presses = 0
    private var longHandled = false
    private var player: Player? = null

    private val resolve = Runnable {
        val count = presses
        presses = 0
        val action = when (count) {
            0 -> SystemControls.NONE
            1 -> SystemControls.headsetSingle
            2 -> SystemControls.headsetDouble
            else -> SystemControls.headsetTriple
        }
        player?.let { SessionControls.perform(appContext, it, action) }
    }

    fun handle(target: Player, event: KeyEvent): Boolean {
        SystemControls.ensureLoaded(appContext)
        if (!SystemControls.headsetCustomized) return false
        if (event.keyCode != KeyEvent.KEYCODE_HEADSETHOOK && event.keyCode != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) return false
        player = target
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    longHandled = false
                } else if (!longHandled && SystemControls.headsetLong != SystemControls.NONE) {
                    longHandled = true
                    handler.removeCallbacks(resolve)
                    presses = 0
                    SessionControls.perform(appContext, target, SystemControls.headsetLong)
                }
            }
            KeyEvent.ACTION_UP -> {
                if (longHandled) {
                    longHandled = false
                } else {
                    presses++
                    handler.removeCallbacks(resolve)
                    handler.postDelayed(resolve, PRESS_WINDOW_MS)
                }
            }
        }
        return true
    }

    fun release() {
        handler.removeCallbacks(resolve)
        player = null
    }
}
