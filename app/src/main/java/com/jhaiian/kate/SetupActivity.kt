package com.jhaiian.kate

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.core.view.WindowCompat
import com.jhaiian.kate.crash.CrashHandler
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.SetupState
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.rememberKateHaptics
import com.jhaiian.kate.ui.setup.SetupScreen
import com.jhaiian.kate.ui.theme.AnimationSpeed
import com.jhaiian.kate.ui.theme.AppLanguage
import com.jhaiian.kate.ui.theme.ClintComposeTheme
import com.jhaiian.kate.ui.theme.KateTextScope
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.MotionPreferences
import com.jhaiian.kate.ui.theme.UiVisibility

class SetupActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onStart() {
        UiVisibility.update(true)
        super.onStart()
    }

    override fun onStop() {
        if (!isFinishing) UiVisibility.update(false)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.install(this)
        LibraryStore.ensureLoaded(this)
        enableEdgeToEdge()
        AnimationSpeed.apply(this, MotionPreferences.getAnimationSpeed(this))
        val composeView = ComposeView(this)
        composeView.setParentCompositionContext(composeView.createLifecycleAwareWindowRecomposer(AnimationSpeed, lifecycle))
        composeView.setContent {
            val context = LocalContext.current
            var look by remember { mutableStateOf(LookAndFeelSettings.load(context)) }
            val current = look
            ClintComposeTheme(
                theme = current.theme,
                appArtworkColors = current.appArtworkColors,
                tuningOverride = current.themeTuning,
                overridesOverride = current.colorOverrides,
                accentOverride = current.accent
            ) {
                SetupSystemBars(window)
                KateTextScope(current.fontStyle, current.textSize) {
                    CompositionLocalProvider(
                        LocalLookAndFeel provides current,
                        LocalKateHaptics provides rememberKateHaptics(current.haptics)
                    ) {
                        SetupScreen(
                            look = current,
                            onLookChange = { updated ->
                                updated.save(context)
                                look = updated
                            },
                            onFinish = { completeSetup() }
                        )
                    }
                }
            }
        }
        setContentView(composeView)
    }

    private fun completeSetup() {
        if (isFinishing) return
        SetupState.markDone(this)
        if (LibraryStore.folders.any { it.enabled }) LibraryStore.startScan(this, restart = true)
        val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, MainActivity::class.java), options.toBundle())
        finish()
    }
}

@Composable
private fun SetupSystemBars(window: Window) {
    val colors = LocalClintColors.current
    val view = LocalView.current
    val light = colors.isLight
    SideEffect {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = light
        controller.isAppearanceLightNavigationBars = light
    }
}
