package com.jhaiian.kate.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.jhaiian.kate.data.AudioSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal class Crossfader(
    private val context: Context,
    private val onPlayerSwitched: (ExoPlayer) -> Unit
) {
    private companion object {
        const val CHECK_INTERVAL_MS = 150L
        const val RAMP_INTERVAL_MS = 25L
        const val GAIN_INTERVAL_MS = 16L
        const val PREPARE_LEAD_MS = 8_000L
        const val MIN_FADE_MS = 500L
        const val MAX_SKIP_FADE_MS = 3_000L
        const val SKIP_WAIT_MS = 1_200L
        const val SKIP_POLL_MS = 30L
        const val DUCK_RAMP_MS = 250L
        const val INSTANT_PAUSE_WINDOW_MS = 1_000L
        const val REWIND_AFTER_PAUSE_MS = 60_000L
        const val MIN_SPEED = 0.1f
    }

    private class GainRamp(initial: Float) {
        var value = initial
            private set
        var target = initial
            private set
        private var from = initial
        private var startedAt = 0L
        private var duration = 0L

        fun set(level: Float) {
            value = level
            from = level
            target = level
            duration = 0L
        }

        fun start(level: Float, durationMs: Long, now: Long) {
            from = value
            target = level
            startedAt = now
            duration = durationMs
            if (durationMs <= 0L) value = level
        }

        fun step(now: Long): Boolean {
            if (value == target) return false
            val fraction = if (duration <= 0L) 1f else ((now - startedAt).toFloat() / duration).coerceIn(0f, 1f)
            value = if (fraction >= 1f) target else from + (target - from) * fraction
            return value != target
        }
    }

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()
    private val handler = Handler(Looper.getMainLooper())
    private val tapGates = HashMap<ExoPlayer, TapGate>()

    private var builtPipeline = AudioSettings.pipelineParams

    var player: ExoPlayer = buildPlayer(tapOpen = true)
        private set
    private var spare: ExoPlayer? = null
    private var outgoing: ExoPlayer? = null
    private var preparedKey: String? = null
    private var fadeStartedAt = 0L
    private var fadeDurationMs = 0L
    private var fadeFraction = 0f
    private var fadeCurve = AudioSettings.CURVE_SMOOTH
    private var pausedDuringFade = false
    private var switching = false
    private var mixIn = 1f
    private var mixOut = 0f
    private val userGain = GainRamp(1f)
    private val duckGain = GainRamp(1f)
    private var pauseAfterFade = false
    private var fadeInPending = false
    private var skipKey: String? = null
    private var skipRequestedAt = 0L
    private var pausedAt = 0L
    private var appliedSpeed = Float.NaN
    private var sleepGain = 1f
    private var appliedKeepPitch = true

    private val checkRunnable = object : Runnable {
        override fun run() {
            check()
            if (player.isPlaying) handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    private val fadeRunnable = object : Runnable {
        override fun run() {
            if (outgoing == null) return
            fadeFraction = ((SystemClock.elapsedRealtime() - fadeStartedAt).toFloat() / fadeDurationMs).coerceIn(0f, 1f)
            mixIn = curveIn(fadeFraction)
            mixOut = curveOut(fadeFraction)
            applyVolumes()
            if (fadeFraction >= 1f) finishFade() else handler.postDelayed(this, RAMP_INTERVAL_MS)
        }
    }

    private val gainRunnable = object : Runnable {
        override fun run() {
            val now = SystemClock.elapsedRealtime()
            val userMoving = userGain.step(now)
            val duckMoving = duckGain.step(now)
            applyVolumes()
            if (!userMoving && pauseAfterFade && userGain.value == 0f) {
                pauseAfterFade = false
                player.pause()
                userGain.set(1f)
                applyVolumes()
            }
            if (userMoving || duckMoving) handler.postDelayed(this, GAIN_INTERVAL_MS)
        }
    }

    private val skipRunnable = Runnable { trySkipFade() }

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            cancelSkip()
            if (outgoing != null) finishFade() else releaseSpare()
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK && outgoing != null && !switching) finishFade()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED && outgoing == null) releaseSpare()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && outgoing != null) finishFade()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            pausedAt = if (playWhenReady) 0L else SystemClock.elapsedRealtime()
            syncOutgoing()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying && fadeInPending) startFadeIn()
            if (isPlaying) scheduleCheck()
            syncOutgoing()
        }
    }

    private val outgoingListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) finishFade()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            finishFade()
        }

        override fun onPlayerError(error: PlaybackException) {
            finishFade()
        }
    }

    fun start() {
        player.addListener(listener)
        applySettings()
        scheduleCheck()
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        player.removeListener(listener)
        outgoing?.release()
        outgoing = null
        spare?.release()
        spare = null
        preparedKey = null
        skipKey = null
        player.release()
        tapGates.clear()
    }

    fun applySettings() {
        if (AudioSettings.pipelineParams != builtPipeline) {
            rebuildPlayer()
            return
        }
        player.skipSilenceEnabled = AudioSettings.skipSilence && !AudioSettings.bitPerfectEngaged
        player.setHandleAudioBecomingNoisy(AudioSettings.pauseOnDisconnect)
        applyOffload(player)
        applySpeed()
        applyVolumes()
        val prepared = spare
        if (prepared != null && preparedKey != null &&
            (prepared.playbackParameters != player.playbackParameters || prepared.skipSilenceEnabled != player.skipSilenceEnabled)
        ) {
            releaseSpare()
        }
    }

    private fun rebuildPlayer() {
        if (outgoing != null) finishFade()
        cancelSkip()
        val old = player
        val items = List(old.mediaItemCount) { old.getMediaItemAt(it) }
        val index = old.currentMediaItemIndex
        val position = old.currentPosition
        val playWhenReady = old.playWhenReady && !pauseAfterFade
        pauseAfterFade = false
        fadeInPending = false
        userGain.set(1f)
        releaseSpare()
        spare?.let { releasePlayer(it) }
        spare = null
        preparedKey = null
        builtPipeline = AudioSettings.pipelineParams
        val fresh = buildPlayer(tapOpen = true)
        fresh.repeatMode = old.repeatMode
        fresh.shuffleModeEnabled = old.shuffleModeEnabled
        fresh.playbackParameters = old.playbackParameters
        fresh.trackSelectionParameters = old.trackSelectionParameters
        old.removeListener(listener)
        old.stop()
        player = fresh
        fresh.addListener(listener)
        if (items.isNotEmpty()) {
            fresh.setMediaItems(items, index.coerceIn(0, items.lastIndex), position.coerceAtLeast(0L))
            fresh.prepare()
        }
        fresh.playWhenReady = playWhenReady
        appliedSpeed = Float.NaN
        fresh.skipSilenceEnabled = AudioSettings.skipSilence && !AudioSettings.bitPerfectEngaged
        fresh.setHandleAudioBecomingNoisy(AudioSettings.pauseOnDisconnect)
        applyOffload(fresh)
        applySpeed()
        applyVolumes()
        onPlayerSwitched(fresh)
        releasePlayer(old)
    }

    private fun applySpeed() {
        val speed = AudioSettings.playbackSpeed
        val keepPitch = AudioSettings.keepPitch
        if (speed == appliedSpeed && keepPitch == appliedKeepPitch) return
        appliedSpeed = speed
        appliedKeepPitch = keepPitch
        player.playbackParameters = PlaybackParameters(speed, if (keepPitch) 1f else speed)
    }

    fun pauseWithFade(): Boolean {
        val fadeMs = if (AudioSettings.bitPerfectEngaged) 0 else AudioSettings.playPauseFadeMs
        fadeInPending = false
        val instant = SystemClock.elapsedRealtime() - PlayerController.instantPauseAt < INSTANT_PAUSE_WINDOW_MS
        if (fadeMs <= 0 || instant || !player.isPlaying) {
            pauseAfterFade = false
            userGain.set(1f)
            applyVolumes()
            return false
        }
        pauseAfterFade = true
        userGain.start(0f, (fadeMs * userGain.value).toLong(), SystemClock.elapsedRealtime())
        runGainRamp()
        return true
    }

    fun playWithFade(play: () -> Unit) {
        val fadeMs = if (AudioSettings.bitPerfectEngaged) 0 else AudioSettings.playPauseFadeMs
        if (!player.playWhenReady && !pauseAfterFade) rewindIfNeeded()
        if (pauseAfterFade) {
            pauseAfterFade = false
            userGain.start(1f, (fadeMs * (1f - userGain.value)).toLong(), SystemClock.elapsedRealtime())
            runGainRamp()
            play()
            return
        }
        if (player.playWhenReady) {
            play()
            return
        }
        if (fadeMs <= 0) {
            fadeInPending = false
            userGain.set(1f)
            applyVolumes()
            play()
            return
        }
        userGain.set(0f)
        applyVolumes()
        fadeInPending = true
        play()
        if (player.isPlaying && fadeInPending) startFadeIn()
    }

    fun play() {
        playWithFade {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.play()
        }
    }

    fun setDucked(ducked: Boolean) {
        val level = if (ducked) AudioSettings.duckPercent / 100f else 1f
        if (duckGain.target == level) return
        duckGain.start(level, DUCK_RAMP_MS, SystemClock.elapsedRealtime())
        runGainRamp()
    }

    fun skipWithCrossfade(): Boolean {
        if (skipKey != null) {
            cancelSkip()
            return false
        }
        if (!AudioSettings.crossfadeOnSkip || AudioSettings.crossfadeMs <= 0L || AudioSettings.bitPerfectEngaged) return false
        if (outgoing != null || pauseAfterFade || !player.isPlaying) return false
        if (player.repeatMode != Player.REPEAT_MODE_OFF || player.shuffleModeEnabled) return false
        val nextIndex = player.nextMediaItemIndex
        if (nextIndex == C.INDEX_UNSET) return false
        if (skipFadeMs() < MIN_FADE_MS) return false
        val key = preparationKey(nextIndex)
        prepareSpare(nextIndex, key)
        skipKey = key
        skipRequestedAt = SystemClock.elapsedRealtime()
        trySkipFade()
        return true
    }

    private fun trySkipFade() {
        val key = skipKey ?: return
        if (preparedKey != key || !player.isPlaying) {
            hardSkip()
            return
        }
        if (isSpareReady(key)) {
            skipKey = null
            val fadeMs = minOf(skipFadeMs(), halfOf(spare?.duration ?: C.TIME_UNSET))
            if (fadeMs < MIN_FADE_MS) {
                releaseSpare()
                player.seekToNext()
            } else {
                beginFade(fadeMs, manual = true)
            }
            return
        }
        if (SystemClock.elapsedRealtime() - skipRequestedAt > SKIP_WAIT_MS) {
            hardSkip()
            return
        }
        handler.postDelayed(skipRunnable, SKIP_POLL_MS)
    }

    private fun hardSkip() {
        cancelSkip()
        player.seekToNext()
    }

    private fun cancelSkip() {
        skipKey = null
        handler.removeCallbacks(skipRunnable)
    }

    private fun skipFadeMs(): Long {
        val duration = player.duration
        val remaining = if (duration == C.TIME_UNSET || duration <= 0L) Long.MAX_VALUE else toWallMs(duration - player.currentPosition)
        return minOf(AudioSettings.crossfadeMs, MAX_SKIP_FADE_MS, remaining)
    }

    private fun toWallMs(mediaMs: Long): Long =
        (mediaMs / player.playbackParameters.speed.coerceAtLeast(MIN_SPEED).toDouble()).toLong()

    private fun halfOf(durationMs: Long): Long =
        if (durationMs == C.TIME_UNSET || durationMs <= 0L) Long.MAX_VALUE else toWallMs(durationMs) / 2

    private fun storedDuration(item: MediaItem): Long {
        val stored = item.mediaMetadata.extras?.getLong(PlayerController.EXTRA_DURATION, 0L) ?: 0L
        return if (stored > 0L) stored else C.TIME_UNSET
    }

    private fun rewindIfNeeded() {
        val since = pausedAt
        pausedAt = 0L
        val rewindMs = AudioSettings.rewindSeconds * 1_000L
        if (rewindMs <= 0L || since == 0L) return
        if (SystemClock.elapsedRealtime() - since < REWIND_AFTER_PAUSE_MS) return
        val position = player.currentPosition
        if (position <= 0L) return
        player.seekTo((position - rewindMs).coerceAtLeast(0L))
    }

    private fun startFadeIn() {
        fadeInPending = false
        userGain.start(1f, AudioSettings.playPauseFadeMs.toLong(), SystemClock.elapsedRealtime())
        runGainRamp()
    }

    private fun runGainRamp() {
        handler.removeCallbacks(gainRunnable)
        handler.post(gainRunnable)
    }

    private fun scheduleCheck() {
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
    }

    private fun syncOutgoing() {
        val leaving = outgoing ?: return
        val running = player.playWhenReady && (player.isPlaying || player.playbackState == Player.STATE_BUFFERING)
        if (running && pausedDuringFade) {
            pausedDuringFade = false
            fadeStartedAt = SystemClock.elapsedRealtime() - (fadeDurationMs * fadeFraction).toLong()
            leaving.play()
            handler.removeCallbacks(fadeRunnable)
            handler.post(fadeRunnable)
        } else if (!running && !pausedDuringFade) {
            pausedDuringFade = true
            leaving.pause()
            handler.removeCallbacks(fadeRunnable)
        }
    }

    private fun applyVolumes() {
        val base = if (AudioSettings.bitPerfectEngaged) {
            1f
        } else {
            userGain.value * duckGain.value * sleepGain * AudioSettings.fineVolumeGain * AudioSettings.volumeLimit / 100f
        }
        player.volume = mixIn * base
        outgoing?.volume = mixOut * base
    }

    private fun curveIn(fraction: Float): Float = when (fadeCurve) {
        AudioSettings.CURVE_LINEAR -> fraction
        AudioSettings.CURVE_S -> sin(smoothStep(fraction) * PI / 2).toFloat()
        else -> sin(fraction * PI / 2).toFloat()
    }

    private fun curveOut(fraction: Float): Float = when (fadeCurve) {
        AudioSettings.CURVE_LINEAR -> 1f - fraction
        AudioSettings.CURVE_S -> cos(smoothStep(fraction) * PI / 2).toFloat()
        else -> cos(fraction * PI / 2).toFloat()
    }

    private fun smoothStep(fraction: Float): Double {
        val t = fraction.toDouble()
        return t * t * (3.0 - 2.0 * t)
    }

    @OptIn(UnstableApi::class)
    private fun applyOffload(target: ExoPlayer) {
        val mode = if (AudioSettings.offloadActive) {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
        } else {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
        }
        val params = target.trackSelectionParameters
        if (params.audioOffloadPreferences.audioOffloadMode == mode) return
        val preferences = TrackSelectionParameters.AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(mode)
            .setIsGaplessSupportRequired(true)
            .setIsSpeedChangeSupportRequired(true)
            .build()
        target.trackSelectionParameters = params.buildUpon().setAudioOffloadPreferences(preferences).build()
    }

    @OptIn(UnstableApi::class)
    private fun buildPlayer(tapOpen: Boolean): ExoPlayer {
        val gate = TapGate(tapOpen)
        return ExoPlayer.Builder(context, KateRenderersFactory(context, gate))
            .setAudioAttributes(audioAttributes, false)
            .setHandleAudioBecomingNoisy(false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .also { tapGates[it] = gate }
    }

    private fun releasePlayer(target: ExoPlayer) {
        tapGates.remove(target)
        target.release()
    }

    private fun setTap(target: ExoPlayer, open: Boolean) {
        tapGates[target]?.open = open
    }

    private fun updateSleepFade() {
        val fadeMs = AudioSettings.sleepFadeSeconds * 1_000L
        val endsAt = PlayerController.sleepTimerEndsAt
        val remaining = when {
            fadeMs <= 0L -> Long.MAX_VALUE
            endsAt != null -> endsAt - SystemClock.elapsedRealtime()
            PlayerController.sleepAtEndOfTrack && player.duration != C.TIME_UNSET && player.duration > 0L ->
                ((player.duration - player.currentPosition) / player.playbackParameters.speed.coerceAtLeast(0.1f)).toLong()
            else -> Long.MAX_VALUE
        }
        val target = if (remaining >= fadeMs) {
            1f
        } else {
            val fraction = remaining.coerceAtLeast(0L).toFloat() / fadeMs
            fraction * fraction
        }
        if (target != sleepGain) {
            sleepGain = target
            applyVolumes()
        }
    }

    private fun check() {
        updateSleepFade()
        applyOffload(player)
        if (outgoing != null || skipKey != null) return
        val fadeSetting = AudioSettings.crossfadeMs
        if (fadeSetting <= 0L ||
            AudioSettings.bitPerfectEngaged ||
            player.repeatMode != Player.REPEAT_MODE_OFF ||
            player.shuffleModeEnabled ||
            PlayerController.sleepAtEndOfTrack
        ) {
            releaseSpare()
            return
        }
        val nextIndex = player.nextMediaItemIndex
        if (nextIndex == C.INDEX_UNSET || !player.isPlaying) return
        val next = player.getMediaItemAt(nextIndex)
        if (AudioSettings.gaplessAlbums && sameAlbum(player.currentMediaItem, next)) {
            releaseSpare()
            return
        }
        val duration = player.duration
        if (duration == C.TIME_UNSET || duration <= 0L) return
        val key = preparationKey(nextIndex)
        val nextDuration = if (isSpareReady(key)) spare?.duration ?: C.TIME_UNSET else storedDuration(next)
        val fadeMs = minOf(fadeSetting, halfOf(duration), halfOf(nextDuration))
        if (fadeMs < MIN_FADE_MS) return
        val remaining = toWallMs(duration - player.currentPosition)
        if (remaining > fadeMs + PREPARE_LEAD_MS) return
        prepareSpare(nextIndex, key)
        if (remaining in MIN_FADE_MS..fadeMs && isSpareReady(key)) beginFade(remaining, manual = false)
    }

    private fun sameAlbum(current: MediaItem?, next: MediaItem): Boolean {
        val currentExtras = current?.mediaMetadata?.extras ?: return false
        val nextExtras = next.mediaMetadata.extras ?: return false
        val album = currentExtras.getString(PlayerController.EXTRA_ALBUM).orEmpty().trim()
        if (album.isEmpty() || !album.equals(nextExtras.getString(PlayerController.EXTRA_ALBUM).orEmpty().trim(), ignoreCase = true)) {
            return false
        }
        val currentOwner = currentExtras.getString(PlayerController.EXTRA_ALBUM_ARTIST).orEmpty().trim()
        val nextOwner = nextExtras.getString(PlayerController.EXTRA_ALBUM_ARTIST).orEmpty().trim()
        return currentOwner.isEmpty() || nextOwner.isEmpty() || currentOwner.equals(nextOwner, ignoreCase = true)
    }

    private fun preparationKey(nextIndex: Int): String =
        "$nextIndex:${player.mediaItemCount}:${player.getMediaItemAt(nextIndex).mediaId}"

    private fun prepareSpare(nextIndex: Int, key: String) {
        if (preparedKey == key) return
        val next = spare ?: buildPlayer(tapOpen = false).also { spare = it }
        setTap(next, false)
        next.volume = 0f
        next.playWhenReady = false
        next.repeatMode = Player.REPEAT_MODE_OFF
        next.shuffleModeEnabled = false
        next.playbackParameters = player.playbackParameters
        next.skipSilenceEnabled = player.skipSilenceEnabled
        next.trackSelectionParameters = player.trackSelectionParameters
        next.setMediaItems(List(player.mediaItemCount) { player.getMediaItemAt(it) }, nextIndex, 0L)
        next.prepare()
        preparedKey = key
    }

    private fun isSpareReady(key: String): Boolean =
        preparedKey == key && spare?.playbackState == Player.STATE_READY

    private fun beginFade(durationMs: Long, manual: Boolean) {
        val incoming = spare ?: return
        val leaving = player
        spare = null
        preparedKey = null
        outgoing = leaving
        pausedDuringFade = false
        fadeCurve = AudioSettings.crossfadeCurve
        fadeDurationMs = durationMs
        fadeFraction = 0f
        fadeStartedAt = SystemClock.elapsedRealtime()

        leaving.removeListener(listener)
        leaving.setHandleAudioBecomingNoisy(false)
        leaving.addListener(outgoingListener)
        setTap(leaving, false)

        incoming.playbackParameters = leaving.playbackParameters
        incoming.skipSilenceEnabled = leaving.skipSilenceEnabled
        incoming.setHandleAudioBecomingNoisy(AudioSettings.pauseOnDisconnect)
        setTap(incoming, true)
        incoming.addListener(listener)
        player = incoming
        mixIn = 0f
        mixOut = 1f
        applyVolumes()
        incoming.play()

        PlayerController.onCrossfadeStarted(durationMs, incoming.currentMediaItem?.mediaId, manual)
        switching = true
        try {
            onPlayerSwitched(incoming)
        } finally {
            switching = false
        }
        handler.post(fadeRunnable)
    }

    private fun finishFade() {
        handler.removeCallbacks(fadeRunnable)
        val leaving = outgoing ?: return
        outgoing = null
        pausedDuringFade = false
        leaving.removeListener(outgoingListener)
        leaving.pause()
        leaving.volume = 0f
        leaving.clearMediaItems()
        mixIn = 1f
        mixOut = 0f
        applyVolumes()
        spare?.let { releasePlayer(it) }
        spare = leaving
        preparedKey = null
    }

    private fun releaseSpare() {
        val next = spare ?: return
        if (preparedKey == null) return
        next.pause()
        next.volume = 0f
        next.clearMediaItems()
        preparedKey = null
    }
}
