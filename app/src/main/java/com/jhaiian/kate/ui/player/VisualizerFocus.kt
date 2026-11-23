package com.jhaiian.kate.ui.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.preference.PreferenceManager

object VisualizerFocus {
    const val OFF = "off"
    const val FADE = "fade"
    const val FULL = "full"
    val Modes = listOf(OFF, FADE, FULL)
    const val IDLE_DELAY_MS = 4_000L
    private const val KEY_MODE = "visualizer_focus"
    private const val FADED_ALPHA = 0.3f

    private var loaded = false

    var mode by mutableStateOf(OFF)
        private set
    var idle by mutableStateOf(false)
    var sheetOpen by mutableStateOf(false)
    var touches by mutableIntStateOf(0)
        private set

    val targetUiAlpha: Float
        get() = when {
            !idle -> 1f
            mode == FULL -> 0f
            else -> FADED_ALPHA
        }

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        mode = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_MODE, OFF)?.takeIf { it in Modes } ?: OFF
    }

    fun setMode(context: Context, value: String) {
        if (value !in Modes) return
        mode = value
        idle = false
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_MODE, value).apply()
    }

    fun cycle(context: Context): String {
        ensureLoaded(context)
        val next = Modes[(Modes.indexOf(mode) + 1) % Modes.size]
        setMode(context, next)
        return next
    }

    fun touch() {
        touches++
        idle = false
    }
}

fun Modifier.visualizerFocusTouch(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type == PointerEventType.Press) VisualizerFocus.touch()
        }
    }
}
