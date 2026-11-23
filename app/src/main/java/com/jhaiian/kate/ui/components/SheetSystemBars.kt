package com.jhaiian.kate.ui.components

import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

@Composable
fun MatchAppSystemBars() {
    val view = LocalView.current
    val look = LocalLookAndFeel.current
    val immersive = ImmersiveMode.active
    val hideStatusBar = look.hideStatusBar || immersive
    val hideNavigationBar = look.hideNavigationBar || immersive
    DisposableEffect(view, hideStatusBar, hideNavigationBar) {
        val window = view.findOwnerWindow()
        if (window == null) {
            onDispose {}
        } else {
            val apply = { window.applySystemBars(hideStatusBar, hideNavigationBar) }
            apply()
            val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
                if (hasFocus) apply()
            }
            val observer = view.viewTreeObserver
            if (observer.isAlive) observer.addOnWindowFocusChangeListener(focusListener)
            onDispose {
                val current = view.viewTreeObserver
                if (current.isAlive) current.removeOnWindowFocusChangeListener(focusListener)
            }
        }
    }
}

private fun View.findOwnerWindow(): Window? {
    var current: Any? = this
    while (current != null) {
        if (current is DialogWindowProvider) return current.window
        current = (current as? View)?.parent
    }
    return null
}

private fun Window.applySystemBars(hideStatusBar: Boolean, hideNavigationBar: Boolean) {
    val controller = WindowInsetsControllerCompat(this, decorView)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    if (hideStatusBar) {
        controller.hide(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.show(WindowInsetsCompat.Type.statusBars())
    }
    if (hideNavigationBar) {
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    } else {
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }
}
