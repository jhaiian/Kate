package com.jhaiian.kate.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

class KateHaptics internal constructor(private val view: View?, private val level: String) {

    fun tick() = perform(
        light = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> HapticFeedbackConstants.TEXT_HANDLE_MOVE
            else -> HapticFeedbackConstants.CLOCK_TICK
        },
        strong = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
    )

    fun click() = perform(
        light = HapticFeedbackConstants.KEYBOARD_TAP,
        strong = HapticFeedbackConstants.VIRTUAL_KEY
    )

    fun toggle(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val constant = if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
            perform(light = constant, strong = constant)
        } else {
            perform(light = HapticFeedbackConstants.CLOCK_TICK, strong = HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    fun confirm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            perform(light = HapticFeedbackConstants.CONFIRM, strong = HapticFeedbackConstants.CONFIRM)
        } else {
            perform(light = HapticFeedbackConstants.VIRTUAL_KEY, strong = HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun reject() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            perform(light = HapticFeedbackConstants.REJECT, strong = HapticFeedbackConstants.REJECT)
        } else {
            perform(light = HapticFeedbackConstants.LONG_PRESS, strong = HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun longPress() = perform(
        light = HapticFeedbackConstants.CONTEXT_CLICK,
        strong = HapticFeedbackConstants.LONG_PRESS
    )

    private fun perform(light: Int, strong: Int) {
        val target = view ?: return
        val constant = when (level) {
            HAPTICS_LIGHT -> light
            HAPTICS_STRONG -> strong
            else -> return
        }
        target.performHapticFeedback(constant)
    }

    companion object {
        const val HAPTICS_OFF = "off"
        const val HAPTICS_LIGHT = "light"
        const val HAPTICS_STRONG = "strong"
    }
}

val LocalKateHaptics = staticCompositionLocalOf { KateHaptics(null, KateHaptics.HAPTICS_OFF) }

@Composable
fun rememberKateHaptics(level: String): KateHaptics {
    val view = LocalView.current
    return remember(view, level) { KateHaptics(view, level) }
}
