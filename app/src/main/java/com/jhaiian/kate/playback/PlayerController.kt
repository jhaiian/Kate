package com.jhaiian.kate.playback

import androidx.compose.runtime.mutableFloatStateOf
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.PlayCounts
import com.jhaiian.kate.data.PlaybackStateStore
import com.jhaiian.kate.data.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SkipDirection { NEXT, PREVIOUS }

object PlayerController {
    private const val END_OF_TRACK_WINDOW_MS = 1_500L
    private const val END_OF_TRACK_LEAD_MS = 80L
    private const val SAVE_POSITION_EVERY_MS = 5_000L
    private const val CROSSFADE_UI_WINDOW_MS = 2_000L
    private const val STALE_TRACK_CHANGE_MS = 1_000L
    internal const val EXTRA_ARTIST = "kate.artist"
    internal const val EXTRA_ALBUM = "kate.album"
    internal const val EXTRA_ALBUM_ARTIST = "kate.album_artist"
    internal const val EXTRA_GENRE = "kate.genre"
    internal const val EXTRA_DURATION = "kate.duration"
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickJob: Job? = null
    private val pendingActions = mutableListOf<(MediaController) -> Unit>()
    private var appContext: Context? = null
    var openPlayerRequest by mutableIntStateOf(0)
        private set
    private var restoreStarted = false
    private var restoreJob: Job? = null
    private var lastPositionSaveAt = 0L

    var queue by mutableStateOf<List<Track>>(emptyList())
        private set
    private var currentTrackState by mutableStateOf<Track?>(null)
    private var trackChangedAt = 0L
    var currentTrack: Track?
        get() = currentTrackState
        private set(value) {
            if (value?.id != currentTrackState?.id) trackChangedAt = SystemClock.elapsedRealtime()
            currentTrackState = value
        }
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0L)
        private set
    private var positionSampledAt = 0L
    var durationMs by mutableStateOf(0L)
        private set
    var shuffleEnabled by mutableStateOf(false)
        private set
    var repeatMode by mutableStateOf(0)
        private set
    var lastSkipDirection by mutableStateOf(SkipDirection.NEXT)
        private set
    private var crossfadeStartedAt = 0L
    private var crossfadeDurationMs = 0L
    private var crossfadeTargetId: String? = null
    private var crossfadeTargetAt = 0L
    private var crossfadeTargetReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
    var sleepTimerEndsAt by mutableStateOf<Long?>(null)
        private set
    var sleepAtEndOfTrack by mutableStateOf(false)
        private set
    @Volatile
    internal var instantPauseAt = 0L
        private set
    private var sleepJob: Job? = null
    private var reloadingId: String? = null
    private var endOfTrackJob: Job? = null

    fun ensureConnected(context: Context) {
        if (controller != null || controllerFuture != null) return
        val appContext = context.applicationContext
        this.appContext = appContext
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener({
            val c = future.get()
            controller = c
            isPlaying = c.isPlaying
            playbackSpeed = c.playbackParameters.speed
            c.repeatMode = uiToPlayerRepeat(repeatMode)
            updatePosition(c.currentPosition)
            c.duration.takeIf { it > 0 && it != C.TIME_UNSET }?.let { durationMs = it }
            attachListener(c)
            manageTicker()
            val actions = pendingActions.toList()
            pendingActions.clear()
            actions.forEach { it(c) }
            syncQueueFromController()
        }, MoreExecutors.directExecutor())
    }

    /**
     * Replaces the active queue with a fresh snapshot of [tracks] (in the exact order given)
     * and starts playback at [startTrack]. This is the single place that creates a new queue:
     * Next/Previous simply walk this snapshot afterwards and it is never silently rebuilt.
     */
    fun restore(context: Context) {
        if (restoreStarted) return
        restoreStarted = true
        val appContext = context.applicationContext
        this.appContext = appContext
        ShufflePreferences.ensureLoaded(appContext)
        KateRadio.ensureLoaded(appContext)
        PlayCounts.refresh(appContext)
        restoreJob = scope.launch {
            val saved = withContext(Dispatchers.IO) { PlaybackStateStore.load(appContext) }
            shuffleEnabled = saved.shuffle
            repeatMode = saved.repeatMode
            if (queue.isNotEmpty() || saved.trackIds.isEmpty()) return@launch
            LibraryStore.ensureLoaded(appContext)
            val byId = LibraryStore.tracks.associateBy { it.id }
            val tracks = saved.trackIds.mapNotNull { byId[it] }
            if (tracks.isEmpty()) return@launch
            val savedIndex = tracks.indexOfFirst { it.id == saved.currentId }
            val startIndex = if (savedIndex >= 0) savedIndex else saved.index.coerceIn(0, tracks.lastIndex)
            val startTrack = tracks[startIndex]
            val startPosition = if (savedIndex >= 0) {
                saved.positionMs.coerceIn(0L, (startTrack.durationMs - 1_000L).coerceAtLeast(0L))
            } else {
                0L
            }
            queue = tracks
            currentTrack = startTrack
            durationMs = startTrack.durationMs
            updatePosition(startPosition)
            runOrQueue(appContext) { c ->
                if (c.mediaItemCount > 0) return@runOrQueue
                c.shuffleModeEnabled = false
                c.repeatMode = uiToPlayerRepeat(repeatMode)
                c.setMediaItems(tracks.map { it.toMediaItem(appContext) }, startIndex, startPosition)
                c.playWhenReady = false
                updatePosition(startPosition)
            }
        }
    }

    fun requestOpenPlayer() {
        openPlayerRequest += 1
    }

    fun withController(context: Context, action: (MediaController) -> Unit) {
        val appContext = context.applicationContext
        this.appContext = appContext
        restore(appContext)
        val pending = restoreJob
        if (pending == null || pending.isCompleted) {
            runOrQueue(appContext, action)
        } else {
            scope.launch {
                pending.join()
                runOrQueue(appContext, action)
            }
        }
    }

    fun togglePlayPause(context: Context) {
        withController(context) { c ->
            if (c.isPlaying) {
                c.pause()
            } else {
                if (c.playbackState == Player.STATE_IDLE) c.prepare()
                c.play()
            }
        }
    }

    private fun setQueueAndPlay(context: Context, tracks: List<Track>, startTrack: Track, fromRadio: Boolean = false) {
        val appContext = context.applicationContext
        this.appContext = appContext
        if (!fromRadio) KateRadio.onQueueReplaced(appContext)
        queue = tracks
        currentTrack = startTrack
        durationMs = startTrack.durationMs
        updatePosition(0L)
        val startIndex = tracks.indexOfFirst { it.id == startTrack.id }.coerceAtLeast(0)
        PlaybackStateStore.saveQueue(appContext, tracks.map { it.id })
        PlaybackStateStore.savePosition(appContext, startTrack.id, startIndex, 0L)
        runOrQueue(appContext) { c ->
            val items = tracks.map { it.toMediaItem(appContext) }
            // The queue snapshot itself defines the play order, so ExoPlayer's own shuffle
            // pass is disabled to avoid a second, uncontrolled reordering on top of it.
            c.shuffleModeEnabled = false
            c.setMediaItems(items, startIndex, 0L)
            c.prepare()
            c.playWhenReady = true
        }
    }

    /**
     * Manually selecting a song creates a new queue from [tracks] (current sort order), starting
     * at [track]. When Shuffle is toggled on (the shuffle icon in the full player), the rest of
     * the queue is randomized instead, with the tapped track kept first.
     */
    fun playTrack(context: Context, tracks: List<Track>, track: Track) {
        if (shuffleEnabled) {
            val mode = shuffleMode(context)
            setQueueAndPlay(context, SmartShuffle.arrangeFrom(tracks, track, mode, PlayCounts.stats), track)
        } else {
            setQueueAndPlay(context, tracks, track)
        }
    }

    /**
     * "Play" button: creates a new queue from [tracks] in their current sort order.
     * Resumes from the currently selected/playing song when it is part of this list,
     * otherwise starts from the beginning.
     */
    fun playAll(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val resumeTrack = currentTrack?.let { current -> tracks.firstOrNull { it.id == current.id } }
        setQueueAndPlay(context, tracks, resumeTrack ?: tracks.first())
    }

    /**
     * "Shuffle" button: creates a new queue containing all of [tracks] in a randomized order.
     * That randomized order is stored as the queue itself, so Next/Previous replay that exact
     * sequence instead of picking a new random song each time.
     */
    fun playShuffled(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val shuffled = SmartShuffle.arrange(tracks, shuffleMode(context), PlayCounts.stats)
        setQueueAndPlay(context, shuffled, shuffled.first())
    }

    private fun shuffleMode(context: Context): ShuffleMode {
        ShufflePreferences.ensureLoaded(context)
        val mode = ShufflePreferences.mode
        if (mode.usesPlays) PlayCounts.refresh(context)
        return mode
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun skipNext() {
        lastSkipDirection = SkipDirection.NEXT
        crossfadeStartedAt = 0L
        controller?.seekToNext()
    }

    fun skipPrevious() {
        lastSkipDirection = SkipDirection.PREVIOUS
        crossfadeStartedAt = 0L
        controller?.seekToPrevious()
    }

    fun currentQueueIndex(): Int {
        val c = controller
        if (c != null && c.mediaItemCount == queue.size) {
            val index = c.currentMediaItemIndex
            if (index in queue.indices) return index
        }
        return queue.indexOfFirst { it.id == currentTrack?.id }
    }

    fun playQueueItem(index: Int) {
        if (index !in queue.indices) return
        val current = currentQueueIndex()
        if (index == current) return
        lastSkipDirection = if (index > current) SkipDirection.NEXT else SkipDirection.PREVIOUS
        crossfadeStartedAt = 0L
        controller?.let { c ->
            c.seekTo(index, 0L)
            if (!c.isPlaying) c.play()
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        if (from == to || from !in queue.indices || to !in queue.indices) return
        val reordered = queue.toMutableList()
        reordered.add(to, reordered.removeAt(from))
        queue = reordered
        controller?.moveMediaItem(from, to)
        saveQueueState()
    }

    fun removeQueueItem(index: Int) {
        if (index !in queue.indices || index == currentQueueIndex()) return
        queue = queue.toMutableList().also { it.removeAt(index) }
        controller?.removeMediaItem(index)
        saveQueueState()
    }

    internal fun startRadioQueue(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        setQueueAndPlay(context, tracks, tracks.first(), fromRadio = true)
    }

    internal fun appendRadioTracks(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty() || queue.isEmpty()) return
        insertIntoQueue(context, queue.size, tracks)
    }

    internal fun replaceUpcoming(context: Context, tracks: List<Track>) {
        val current = currentQueueIndex()
        if (current < 0) return
        val appContext = context.applicationContext
        this.appContext = appContext
        queue = queue.take(current + 1) + tracks
        runOrQueue(appContext) { c ->
            val total = c.mediaItemCount
            if (total > current + 1) c.removeMediaItems(current + 1, total)
            c.addMediaItems(current + 1, tracks.map { it.toMediaItem(appContext) })
        }
        saveQueueState()
    }

    fun canReshuffleUpcoming(): Boolean {
        val current = currentQueueIndex()
        return current >= 0 && queue.size - current - 1 >= 2
    }

    fun reshuffleUpcoming(context: Context): Boolean {
        if (!canReshuffleUpcoming()) return false
        val appContext = context.applicationContext
        this.appContext = appContext
        val current = currentQueueIndex()
        val head = queue.take(current + 1)
        val upcoming = queue.drop(current + 1)
        val arranged = SmartShuffle.arrange(upcoming, shuffleMode(appContext), PlayCounts.stats)
        queue = head + arranged
        runOrQueue(appContext) { c ->
            val total = c.mediaItemCount
            if (total > current + 1) c.removeMediaItems(current + 1, total)
            c.addMediaItems(current + 1, arranged.map { it.toMediaItem(appContext) })
        }
        saveQueueState()
        return true
    }

    fun playNext(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        if (queue.isEmpty() || currentTrack == null) {
            setQueueAndPlay(context, tracks, tracks.first())
            return
        }
        insertIntoQueue(context, currentQueueIndex() + 1, tracks)
    }

    fun addToQueue(context: Context, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        if (queue.isEmpty() || currentTrack == null) {
            setQueueAndPlay(context, tracks, tracks.first())
            return
        }
        insertIntoQueue(context, queue.size, tracks)
    }

    private fun insertIntoQueue(context: Context, index: Int, tracks: List<Track>) {
        val appContext = context.applicationContext
        this.appContext = appContext
        val at = index.coerceIn(0, queue.size)
        queue = queue.toMutableList().also { it.addAll(at, tracks) }
        runOrQueue(appContext) { c -> c.addMediaItems(at, tracks.map { it.toMediaItem(appContext) }) }
        saveQueueState()
    }

    private fun saveQueueState() {
        val context = appContext ?: return
        PlaybackStateStore.saveQueue(context, queue.map { it.id })
        val track = currentTrack ?: return
        PlaybackStateStore.savePosition(context, track.id, currentQueueIndex().coerceAtLeast(0), controller?.currentPosition ?: positionMs)
    }

    fun dismiss() {
        cancelSleepTimer()
        KateRadio.stop(appContext)
        controller?.let { c ->
            c.stop()
            c.clearMediaItems()
        }
        queue = emptyList()
        currentTrack = null
        isPlaying = false
        durationMs = 0L
        updatePosition(0L)
        manageTicker()
        appContext?.let { context ->
            PlaybackStateStore.saveQueue(context, emptyList())
            PlaybackStateStore.savePosition(context, null, 0, 0L)
            PlaybackStateStore.setPlayerExpanded(context, false)
        }
    }

    internal fun onCrossfadeStarted(durationMs: Long, mediaId: String?, manual: Boolean) {
        lastSkipDirection = SkipDirection.NEXT
        crossfadeDurationMs = durationMs
        crossfadeStartedAt = SystemClock.elapsedRealtime()
        crossfadeTargetId = mediaId
        crossfadeTargetAt = crossfadeStartedAt
        crossfadeTargetReason = if (manual) Player.MEDIA_ITEM_TRANSITION_REASON_SEEK else Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
    }

    private fun transitionReason(mediaItem: MediaItem?, reason: Int): Int {
        val target = crossfadeTargetId ?: return reason
        crossfadeTargetId = null
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT || mediaItem?.mediaId != target) return reason
        if (SystemClock.elapsedRealtime() - crossfadeTargetAt > CROSSFADE_UI_WINDOW_MS) return reason
        return crossfadeTargetReason
    }

    internal fun trackChangeIsStale(): Boolean = SystemClock.elapsedRealtime() - trackChangedAt > STALE_TRACK_CHANGE_MS

    internal fun crossfadeTransitionMs(): Int {
        val started = crossfadeStartedAt
        if (!AudioSettings.crossfadeAnimation || started == 0L || SystemClock.elapsedRealtime() - started > CROSSFADE_UI_WINDOW_MS) return 0
        return crossfadeDurationMs.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    fun toggleShuffle() {
        shuffleEnabled = !shuffleEnabled
        saveModes()
    }

    fun cycleRepeat() {
        repeatMode = (repeatMode + 1) % 3
        controller?.repeatMode = uiToPlayerRepeat(repeatMode)
        saveModes()
    }

    private fun saveModes() {
        appContext?.let { PlaybackStateStore.saveModes(it, shuffleEnabled, repeatMode) }
    }

    private fun savePosition() {
        val context = appContext ?: return
        val c = controller ?: return
        val track = currentTrack ?: return
        lastPositionSaveAt = SystemClock.elapsedRealtime()
        PlaybackStateStore.savePosition(context, track.id, c.currentMediaItemIndex, c.currentPosition)
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val durationMillis = minutes * 60_000L
        sleepTimerEndsAt = SystemClock.elapsedRealtime() + durationMillis
        sleepJob = scope.launch {
            delay(durationMillis)
            controller?.pause()
            cancelSleepTimer()
        }
    }

    fun sleepAfterCurrentTrack() {
        cancelSleepTimer()
        sleepAtEndOfTrack = true
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        endOfTrackJob?.cancel()
        endOfTrackJob = null
        sleepTimerEndsAt = null
        sleepAtEndOfTrack = false
    }

    fun sleepRemainingMs(): Long? = sleepTimerEndsAt?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0L) }

    private fun scheduleEndOfTrackStop(c: MediaController) {
        if (!sleepAtEndOfTrack || endOfTrackJob != null) return
        val duration = c.duration.takeIf { it > 0 && it != C.TIME_UNSET } ?: return
        val remaining = duration - c.currentPosition
        if (remaining <= 0L || remaining > END_OF_TRACK_WINDOW_MS) return
        endOfTrackJob = scope.launch {
            delay((remaining - END_OF_TRACK_LEAD_MS).coerceAtLeast(0L))
            if (sleepAtEndOfTrack) {
                instantPauseAt = SystemClock.elapsedRealtime()
                controller?.pause()
                cancelSleepTimer()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val c = controller ?: return
        val target = positionMs.coerceAtLeast(0L)
        c.seekTo(target)
        updatePosition(target)
        savePosition()
    }

    fun seekToFraction(fraction: Float) {
        val c = controller ?: return
        val dur = c.duration.takeIf { it > 0 && it != C.TIME_UNSET } ?: durationMs
        if (dur <= 0) return
        val target = (dur * fraction.coerceIn(0f, 1f)).toLong()
        c.seekTo(target)
        updatePosition(target)
        savePosition()
    }

    var playbackSpeed by mutableFloatStateOf(1f)
        private set

    fun setSpeed(speed: Float) {
        val safe = speed.coerceIn(0.25f, 2f)
        val c = controller ?: return
        updatePosition(estimatedPositionMs())
        c.setPlaybackSpeed(safe)
        playbackSpeed = safe
    }

    fun estimatedPositionMs(): Long {
        val base = positionMs
        if (!isPlaying) return base
        val estimate = base + ((SystemClock.elapsedRealtime() - positionSampledAt) * playbackSpeed).toLong()
        val duration = durationMs
        return if (duration > 0) estimate.coerceAtMost(duration) else estimate
    }

    private fun updatePosition(value: Long) {
        positionSampledAt = SystemClock.elapsedRealtime()
        positionMs = value
    }

    fun refreshTracks(context: Context, updated: List<Track>) {
        if (updated.isEmpty()) return
        val byId = updated.associateBy { it.id }
        if (queue.any { it.id in byId }) queue = queue.map { byId[it.id] ?: it }
        currentTrack?.let { current -> byId[current.id]?.let { currentTrack = it } }
        val appContext = context.applicationContext
        val c = controller ?: return
        val playingIndex = c.currentMediaItemIndex
        for (index in 0 until c.mediaItemCount) {
            val fresh = byId[c.getMediaItemAt(index).mediaId] ?: continue
            if (index == playingIndex) {
                val position = c.currentPosition
                val item = fresh.toMediaItem(appContext).buildUpon()
                    .setCustomCacheKey(fresh.id + "#" + SystemClock.elapsedRealtime())
                    .build()
                reloadingId = fresh.id
                runCatching {
                    c.replaceMediaItem(index, item)
                    c.seekTo(index, position)
                }.onFailure { reloadingId = null }
            } else {
                runCatching { c.replaceMediaItem(index, fresh.toMediaItem(appContext)) }
            }
        }
    }

    fun reloadAfterWrite(context: Context, trackId: String) {
        val track = queue.firstOrNull { it.id == trackId } ?: return
        refreshTracks(context, listOf(track))
    }

    private fun runOrQueue(context: Context, action: (MediaController) -> Unit) {
        ensureConnected(context)
        val c = controller
        if (c != null) action(c) else pendingActions.add(action)
    }

    private fun attachListener(c: MediaController) {
        c.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                controller?.let { updatePosition(it.currentPosition) }
                isPlaying = playing
                manageTicker()
                if (!playing) savePosition()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, rawReason: Int) {
                val reason = transitionReason(mediaItem, rawReason)
                val reloaded = reloadingId
                if (reloaded != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem?.mediaId == reloaded) {
                    reloadingId = null
                    currentTrack = queue.firstOrNull { it.id == reloaded } ?: currentTrack
                    return
                }
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    lastSkipDirection = SkipDirection.NEXT
                }
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                    val previousIndex = queue.indexOfFirst { it.id == currentTrack?.id }
                    val nextIndex = queue.indexOfFirst { it.id == mediaItem?.mediaId }
                    if (previousIndex >= 0 && nextIndex >= 0) {
                        lastSkipDirection = if (nextIndex >= previousIndex) SkipDirection.NEXT else SkipDirection.PREVIOUS
                    }
                }
                if (sleepAtEndOfTrack) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                        instantPauseAt = SystemClock.elapsedRealtime()
                        controller?.pause()
                        cancelSleepTimer()
                    } else {
                        endOfTrackJob?.cancel()
                        endOfTrackJob = null
                    }
                }
                currentTrack = queue.firstOrNull { it.id == mediaItem?.mediaId } ?: currentTrack
                updatePosition(controller?.currentPosition ?: 0L)
                durationMs = currentTrack?.durationMs ?: 0L
                savePosition()
                appContext?.let { KateRadio.onTrackChanged(it) }

                // Mode 2 ("play it twice, then move on") is a one-shot for whichever song was
                // playing when it was turned on — it does not carry over to later songs. The
                // item is armed with REPEAT_MODE_ONE so it loops back once; the moment that
                // loop happens (reason == REPEAT) native repeat is turned off so the second
                // play-through advances to the next song normally. Once that advance actually
                // lands on a different item, the mode resets itself to Off (icon included).
                if (repeatMode == 2) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                        controller?.repeatMode = Player.REPEAT_MODE_OFF
                    } else {
                        repeatMode = 0
                        controller?.repeatMode = Player.REPEAT_MODE_OFF
                        saveModes()
                    }
                }
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) syncQueueFromController()
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                updatePosition(controller?.currentPosition ?: positionMs)
                playbackSpeed = playbackParameters.speed
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    controller?.duration?.takeIf { it > 0 && it != C.TIME_UNSET }?.let { durationMs = it }
                }
            }
        })
    }

    private fun syncQueueFromController() {
        val c = controller ?: return
        val count = c.mediaItemCount
        if (count == 0) return
        if (count == queue.size && (0 until count).all { c.getMediaItemAt(it).mediaId == queue[it].id }) return
        val known = HashMap<String, Track>()
        queue.forEach { known[it.id] = it }
        LibraryStore.tracks.forEach { known.putIfAbsent(it.id, it) }
        val rebuilt = (0 until count).map { known[c.getMediaItemAt(it).mediaId] ?: return }
        queue = rebuilt
        rebuilt.getOrNull(c.currentMediaItemIndex)?.let { track ->
            currentTrack = track
            durationMs = track.durationMs
        }
        saveQueueState()
    }

    private fun manageTicker() {
        tickJob?.cancel()
        val c = controller ?: return
        if (c.isPlaying) {
            tickJob = scope.launch {
                while (isActive) {
                    controller?.let {
                        updatePosition(it.currentPosition)
                        it.duration.takeIf { d -> d > 0 && d != C.TIME_UNSET }?.let { d -> durationMs = d }
                        scheduleEndOfTrackStop(it)
                        if (SystemClock.elapsedRealtime() - lastPositionSaveAt >= SAVE_POSITION_EVERY_MS) savePosition()
                    }
                    delay(500)
                }
            }
        } else {
            updatePosition(c.currentPosition)
        }
    }

    /**
     * Repeat has two modes, neither of which loops the whole queue:
     * 1 = loop the current song forever.
     * 2 = play the current song one extra time, then continue to the next song as normal.
     */
    private fun uiToPlayerRepeat(ui: Int): Int = when (ui) {
        1, 2 -> Player.REPEAT_MODE_ONE
        else -> Player.REPEAT_MODE_OFF
    }
}

internal fun Track.toMediaItem(context: Context): MediaItem {
    val metadataBuilder = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist.ifBlank { context.getString(R.string.player_unknown_artist) })
        .setAlbumTitle(album.ifBlank { context.getString(R.string.player_unknown_album) })
        .setExtras(
            Bundle().apply {
                putString(PlayerController.EXTRA_ARTIST, artist)
                putString(PlayerController.EXTRA_ALBUM, album)
                putString(PlayerController.EXTRA_ALBUM_ARTIST, albumArtist)
                putString(PlayerController.EXTRA_GENRE, genre)
                putLong(PlayerController.EXTRA_DURATION, durationMs)
            }
        )
    LibraryTrackCache.artFileUriOrNull(context, artKey)?.let { metadataBuilder.setArtworkUri(it) }
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(metadataBuilder.build())
        .build()
}
