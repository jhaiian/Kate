package com.jhaiian.kate.playback

import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import com.jhaiian.kate.data.SystemControls
import com.jhaiian.kate.data.FavoritesStore
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.compose.runtime.snapshotFlow
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaLibraryService
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.widget.WidgetBridge

@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    private lateinit var mediaSession: MediaLibraryService.MediaLibrarySession
    private lateinit var crossfader: Crossfader
    private lateinit var recorder: ListeningRecorder
    private lateinit var widgetBridge: WidgetBridge
    private lateinit var focusKeeper: AudioFocusKeeper
    private lateinit var headsetAutoPlay: HeadsetAutoPlay
    private lateinit var longTrackMemory: LongTrackMemory
    private lateinit var routeWatcher: OutputRouteWatcher
    private lateinit var volumeWatcher: VolumeWatcher
    private lateinit var eqAutoSwitcher: EqAutoSwitcher
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controlsPlayer: Player? = null
    private var buttonsKey = ""
    private val controlsListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshButtons()
        }
    }
    private val settingsListener: () -> Unit = {
        BitPerfect.refresh(this)
        crossfader.applySettings()
        focusKeeper.refresh()
    }

    override fun onCreate() {
        super.onCreate()
        AudioSettings.ensureLoaded(this)
        EqualizerSettings.ensureLoaded(this)
        AudioEffects.ensureLoaded(this)
        SystemControls.ensureLoaded(this)
        FavoritesStore.ensureLoaded(this)
        widgetBridge = WidgetBridge(this)
        crossfader = Crossfader(this) { player ->
            mediaSession.player = SessionPlayer(player, crossfader)
            widgetBridge.attach(player)
            focusKeeper.attach(player)
            longTrackMemory.attach(player)
            eqAutoSwitcher.attach(player)
            attachControls(player)
        }
        focusKeeper = AudioFocusKeeper(this, crossfader)
        longTrackMemory = LongTrackMemory(this)
        eqAutoSwitcher = EqAutoSwitcher(this)
        mediaSession = MediaLibraryService.MediaLibrarySession.Builder(
            this,
            SessionPlayer(crossfader.player, crossfader),
            KateLibraryCallback(this)
        ).build()
        crossfader.start()
        focusKeeper.attach(crossfader.player)
        longTrackMemory.attach(crossfader.player)
        eqAutoSwitcher.attach(crossfader.player)
        attachControls(crossfader.player)
        serviceScope.launch {
            snapshotFlow { Triple(FavoritesStore.ids, SystemControls.notificationFirst, SystemControls.notificationSecond) }
                .collect { refreshButtons() }
        }
        recorder = ListeningRecorder(this) { crossfader.player }
        recorder.start()
        widgetBridge.attach(crossfader.player)
        headsetAutoPlay = HeadsetAutoPlay(this) {
            val player = crossfader.player
            if (player.mediaItemCount > 0 && !player.playWhenReady) crossfader.play()
        }
        headsetAutoPlay.start()
        routeWatcher = OutputRouteWatcher(this)
        routeWatcher.start()
        volumeWatcher = VolumeWatcher(this, crossfader)
        volumeWatcher.start()
        AudioSettings.addListener(settingsListener)
        settingsListener()
    }

    private fun attachControls(player: Player) {
        if (controlsPlayer === player) return
        controlsPlayer?.removeListener(controlsListener)
        controlsPlayer = player
        player.addListener(controlsListener)
        if (::mediaSession.isInitialized) refreshButtons()
    }

    private fun refreshButtons() {
        if (!::mediaSession.isInitialized) return
        val buttons = SessionControls.buttons(this, mediaSession.player)
        val key = buttons.joinToString { "${it.sessionCommand?.customAction}:${it.icon}" }
        if (key == buttonsKey) return
        buttonsKey = key
        mediaSession.setMediaButtonPreferences(buttons)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibraryService.MediaLibrarySession = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val currentPlayer = mediaSession.player
        if (!currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        AudioSettings.removeListener(settingsListener)
        headsetAutoPlay.stop()
        routeWatcher.stop()
        BitPerfect.release(this)
        volumeWatcher.stop()
        focusKeeper.release()
        longTrackMemory.release()
        eqAutoSwitcher.release()
        serviceScope.cancel()
        controlsPlayer?.removeListener(controlsListener)
        controlsPlayer = null
        recorder.stop()
        widgetBridge.release()
        mediaSession.release()
        crossfader.release()
        super.onDestroy()
    }
}
