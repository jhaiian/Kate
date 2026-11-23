package com.jhaiian.kate

import com.jhaiian.kate.ui.theme.AppLanguage
import android.content.Context
import android.Manifest
import android.content.Intent
import android.app.ActivityOptions
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.jhaiian.kate.crash.CrashHandler
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.ArtworkColors
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.SetupState
import com.jhaiian.kate.playback.FineVolume
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.KateApp
import com.jhaiian.kate.ui.components.ImmersiveMode
import com.jhaiian.kate.ui.theme.AnimationSpeed
import com.jhaiian.kate.ui.theme.ClintComposeTheme
import com.jhaiian.kate.ui.theme.KateTextScope
import com.jhaiian.kate.ui.theme.MotionPreferences
import com.jhaiian.kate.ui.theme.UiVisibility
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.StyleStore
import com.jhaiian.kate.ui.theme.FontStore
import com.jhaiian.kate.data.LyricsOffsetStore
import com.jhaiian.kate.ui.player.VisualizerFocus
import com.jhaiian.kate.update.UpdateChecker

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_LIBRARY_CATEGORY = "com.jhaiian.kate.extra.LIBRARY_CATEGORY"
        const val EXTRA_OPEN_PLAYER = "com.jhaiian.kate.extra.OPEN_PLAYER"
    }

    private var hideStatusBarState = false
    private var hideNavigationBarState = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onStart() {
        UiVisibility.update(true)
        super.onStart()
    }

    override fun onStop() {
        UiVisibility.update(false)
        super.onStop()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.install(this)
        if (savedInstanceState == null && !SetupState.isDone(this)) {
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, SetupActivity::class.java), options.toBundle())
            finish()
            return
        }
        LibraryStore.restoreCategory(this)
        LibraryStore.ensureLoaded(this)
        FavoritesStore.ensureLoaded(this)
        PlaylistStore.ensureLoaded(this)
        AudioSettings.ensureLoaded(this)
        EqualizerSettings.ensureLoaded(this)
        AudioEffects.ensureLoaded(this)
        ListeningHistory.ensureLoaded(this)
        StyleStore.ensureLoaded(this)
        FontStore.ensureLoaded(this)
        LyricsOffsetStore.ensureLoaded(this)
        VisualizerFocus.ensureLoaded(this)
        PlayerController.restore(this)
        ArtworkColors.start(this)
        ArtistImageStore.start(this)
        if (savedInstanceState == null) {
            openShortcutCategory(intent, switchTab = false)
            openPlayerFromIntent(intent)
            UpdateChecker.checkOnLaunch(this)
        }
        val launchStartRoute = if (savedInstanceState == null && intent?.hasExtra(EXTRA_LIBRARY_CATEGORY) != true) {
            LookAndFeelSettings.load(this).effectiveStartScreen
        } else {
            null
        }
        enableEdgeToEdge()
        AnimationSpeed.apply(this, MotionPreferences.getAnimationSpeed(this))
        val composeView = ComposeView(this)
        composeView.setParentCompositionContext(composeView.createLifecycleAwareWindowRecomposer(AnimationSpeed, lifecycle))
        composeView.setContent {
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !SetupState.notificationsAsked(context) &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val windowSizeClass = calculateWindowSizeClass(this)
            val navController = rememberNavController()
            var lookAndFeel by remember { mutableStateOf(LookAndFeelSettings.load(context)) }

            val immersive = ImmersiveMode.active
            LaunchedEffect(lookAndFeel.hideStatusBar, lookAndFeel.hideNavigationBar, immersive) {
                hideStatusBarState = lookAndFeel.hideStatusBar || immersive
                hideNavigationBarState = lookAndFeel.hideNavigationBar || immersive
                applySystemBars(hideStatusBarState, hideNavigationBarState)
            }

            key(lookAndFeel.accent, lookAndFeel.intensity) {
                ClintComposeTheme(
                    theme = lookAndFeel.theme,
                    appArtworkColors = lookAndFeel.appArtworkColors,
                    tuningOverride = lookAndFeel.themeTuning,
                    overridesOverride = lookAndFeel.colorOverrides
                ) {
                    KateTextScope(lookAndFeel.fontStyle, lookAndFeel.textSize) {
                        KateApp(
                            windowWidthSizeClass = windowSizeClass.widthSizeClass,
                            navController = navController,
                            lookAndFeel = lookAndFeel,
                            onLookAndFeelChange = { updated ->
                                updated.save(context)
                                if (updated.animationSpeed != lookAndFeel.animationSpeed) {
                                    AnimationSpeed.apply(context, updated.animationSpeed)
                                }
                                lookAndFeel = updated
                            },
                            startRoute = launchStartRoute
                        )
                    }
                }
            }
        }
        setContentView(composeView)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val up = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> true
            KeyEvent.KEYCODE_VOLUME_DOWN -> false
            else -> return super.onKeyDown(keyCode, event)
        }
        if (FineVolume.handleKey(this, up)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openShortcutCategory(intent, switchTab = true)
        openPlayerFromIntent(intent)
    }

    private fun openPlayerFromIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER, false) == true) PlayerController.requestOpenPlayer()
    }

    private fun openShortcutCategory(intent: Intent?, switchTab: Boolean) {
        val category = LibraryCategory.fromKey(intent?.getStringExtra(EXTRA_LIBRARY_CATEGORY)) ?: return
        LibraryStore.openCategory(this, category)
        if (switchTab) LibraryStore.requestLibraryTab()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applySystemBars(hideStatusBarState, hideNavigationBarState)
        }
    }

    private fun applySystemBars(hideStatusBar: Boolean, hideNavigationBar: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hideStatusBar) controller.hide(WindowInsetsCompat.Type.statusBars()) else controller.show(WindowInsetsCompat.Type.statusBars())
        if (hideNavigationBar) controller.hide(WindowInsetsCompat.Type.navigationBars()) else controller.show(WindowInsetsCompat.Type.navigationBars())
    }
}
