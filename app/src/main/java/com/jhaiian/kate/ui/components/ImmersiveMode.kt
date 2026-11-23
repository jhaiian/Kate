package com.jhaiian.kate.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView

object ImmersiveMode {
    private var requests by mutableIntStateOf(0)

    val active: Boolean get() = requests > 0

    internal fun acquire() {
        requests += 1
    }

    internal fun release() {
        requests = (requests - 1).coerceAtLeast(0)
    }
}

@Composable
fun ImmersiveEffect(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (enabled) ImmersiveMode.acquire()
        onDispose {
            if (enabled) ImmersiveMode.release()
        }
    }
}

@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        if (enabled) view.keepScreenOn = true
        onDispose {
            if (enabled) view.keepScreenOn = false
        }
    }
}
