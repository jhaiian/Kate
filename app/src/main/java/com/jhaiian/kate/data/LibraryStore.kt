package com.jhaiian.kate.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.jhaiian.kate.playback.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LibraryScanState {
    data object Idle : LibraryScanState
    data object Scanning : LibraryScanState
    data object Empty : LibraryScanState
    data object Loaded : LibraryScanState
}

object LibraryStore {
    val folders = mutableStateListOf<MusicFolder>()
    val tracks = mutableStateListOf<Track>()
    var scanState by mutableStateOf<LibraryScanState>(LibraryScanState.Idle)
        private set
    var sortOrder by mutableStateOf(LibrarySortOrder.Default)
        private set
    var sortDirection by mutableStateOf(SortDirection.Default)
        private set
    var viewModes by mutableStateOf<Map<LibraryCategory, LibraryViewMode>>(emptyMap())
        private set
    var scanProgress by mutableStateOf<ScanProgress?>(null)
        private set
    var index by mutableStateOf(LibraryIndex.Empty)
        private set
    var category by mutableStateOf<LibraryCategory?>(null)
        private set
    var libraryTabRequest by mutableIntStateOf(0)
        private set

    private var loaded = false
    private var autoScanTriggered = false
    private var queueObserverStarted = false
    private var indexObserverStarted = false
    private var categoryRestored = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scanJob: Job? = null
    private const val PROGRESS_SAMPLE_MILLIS = 120L
    private const val STREAM_EVERY_SAMPLES = 3

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val appContext = context.applicationContext
        TagSettings.ensureLoaded(appContext)
        ScanFilters.ensureLoaded(appContext)
        sortOrder = LibraryPreferences.getSortOrder(appContext)
        sortDirection = LibraryPreferences.getSortDirection(appContext)
        category?.let { LibraryPreferences.getCategorySort(appContext, it) }?.let { (order, direction) ->
            sortOrder = order
            sortDirection = direction
        }
        viewModes = LibraryCategory.entries.associateWith { LibraryPreferences.getViewMode(appContext, it) }
        folders.addAll(LibraryPreferences.getFolders(appContext))
        val cached = LibraryTrackCache.loadTracks(appContext)
        if (cached.isNotEmpty()) {
            tracks.addAll(sortTracks(ScanFilters.apply(cached), sortOrder, sortDirection))
            scanState = LibraryScanState.Loaded
        } else if (folders.any { it.enabled }) {
            scanState = LibraryScanState.Scanning
        }
        startQueueObserver()
        startIndexObserver()
    }

    fun restoreCategory(context: Context) {
        if (categoryRestored) return
        categoryRestored = true
        category = LibraryPreferences.getCategory(context.applicationContext)
    }

    fun requestLibraryTab() {
        libraryTabRequest++
    }

    fun openCategory(context: Context, target: LibraryCategory?) {
        if (category == target) return
        category = target
        val appContext = context.applicationContext
        LibraryPreferences.setCategory(appContext, target)
        val saved = target?.let { LibraryPreferences.getCategorySort(appContext, it) }
        val order = saved?.first ?: LibraryPreferences.getSortOrder(appContext)
        val direction = saved?.second ?: LibraryPreferences.getSortDirection(appContext)
        if (order != sortOrder || direction != sortDirection) {
            sortOrder = order
            sortDirection = direction
            replaceTracks(sortTracks(tracks, order, direction))
        }
    }

    fun refilter(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            val cached = withContext(Dispatchers.IO) { LibraryTrackCache.loadTracks(appContext) }
            if (cached.isEmpty()) return@launch
            val sorted = withContext(Dispatchers.Default) { sortTracks(ScanFilters.apply(cached), sortOrder, sortDirection) }
            replaceTracks(sorted)
        }
    }

    private fun startIndexObserver() {
        if (indexObserverStarted) return
        indexObserverStarted = true
        scope.launch {
            snapshotFlow { tracks.toList() to TagSettings.config }.collectLatest { (snapshot, config) ->
                index = withContext(Dispatchers.Default) { LibraryIndex.build(snapshot, config) }
            }
        }
    }

    /**
     * Keeps the library view in sync with the active playback queue while the "Queue" sort
     * order is selected: whenever Play, Shuffle, or manually picking a song replaces the queue,
     * the visible list is re-sorted to match the new queue order. Any other sort order is left
     * untouched, since only the "Queue" option depends on this state.
     */
    private fun startQueueObserver() {
        if (queueObserverStarted) return
        queueObserverStarted = true
        scope.launch {
            snapshotFlow { PlayerController.queue }.collect {
                if (sortOrder == LibrarySortOrder.QUEUE && tracks.isNotEmpty()) {
                    val resorted = sortTracks(tracks, sortOrder, sortDirection)
                    tracks.clear()
                    tracks.addAll(resorted)
                }
            }
        }
    }

    fun setSortOrder(context: Context, order: LibrarySortOrder) {
        if (sortOrder == order) return
        sortOrder = order
        LibraryPreferences.setSortOrder(context.applicationContext, order)
        category?.let { LibraryPreferences.setCategorySort(context.applicationContext, it, sortOrder, sortDirection) }
        val sorted = sortTracks(tracks, sortOrder, sortDirection)
        tracks.clear()
        tracks.addAll(sorted)
    }

    fun setSortDirection(context: Context, direction: SortDirection) {
        if (sortDirection == direction) return
        sortDirection = direction
        LibraryPreferences.setSortDirection(context.applicationContext, direction)
        category?.let { LibraryPreferences.setCategorySort(context.applicationContext, it, sortOrder, sortDirection) }
        val sorted = sortTracks(tracks, sortOrder, sortDirection)
        tracks.clear()
        tracks.addAll(sorted)
    }

    fun viewModeFor(category: LibraryCategory): LibraryViewMode =
        viewModes[category] ?: if (category == LibraryCategory.ALBUMS) LibraryViewMode.GRID else LibraryViewMode.Default

    fun setViewMode(context: Context, category: LibraryCategory, mode: LibraryViewMode) {
        if (viewModeFor(category) == mode) return
        viewModes = viewModes + (category to mode)
        LibraryPreferences.setViewMode(context.applicationContext, category, mode)
    }

    fun scanIfNeeded(context: Context) {
        if (autoScanTriggered) return
        if (folders.none { it.enabled }) return
        autoScanTriggered = true
        startScan(context)
    }

    fun startScan(context: Context, restart: Boolean = false) {
        val appContext = context.applicationContext
        val running = scanJob
        if (running != null && running.isActive) {
            if (!restart) return
            running.cancel()
        }
        scanJob = scope.launch {
            running?.join()
            runScan(appContext)
        }
    }

    fun addFolder(context: Context, uri: Uri, displayName: String, locationLabel: String) {
        if (folders.any { it.uri == uri }) return
        folders.add(MusicFolder(uri, displayName, locationLabel, enabled = true))
        persist(context)
    }

    fun setFolderEnabled(context: Context, uri: Uri, enabled: Boolean) {
        val index = folders.indexOfFirst { it.uri == uri }
        if (index == -1) return
        folders[index] = folders[index].copy(enabled = enabled)
        persist(context)
    }

    fun removeFolder(context: Context, uri: Uri) {
        folders.removeAll { it.uri == uri }
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        persist(context)
    }

    private fun persist(context: Context) {
        LibraryPreferences.setFolders(context.applicationContext, folders.toList())
    }

    fun applyEdits(context: Context, updated: List<Track>) {
        if (updated.isEmpty()) return
        val byId = updated.associateBy { it.id }
        replaceTracks(sortTracks(tracks.map { byId[it.id] ?: it }, sortOrder, sortDirection))
        val appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            val index = LibraryTrackCache.loadIndex(appContext)
            if (index.isEmpty()) return@launch
            val entries = index.values.map { entry ->
                val track = byId[entry.uri] ?: return@map entry
                entry.copy(
                    lastModified = 0L,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    albumArtist = track.albumArtist,
                    genre = track.genre,
                    trackNumber = track.trackNumber,
                    discNumber = track.discNumber,
                    year = track.year,
                    hasArt = track.hasArt,
                    artKey = track.artKey
                )
            }
            LibraryTrackCache.save(appContext, entries)
        }
    }

    private fun replaceTracks(sorted: List<Track>) {
        tracks.clear()
        tracks.addAll(sorted)
    }

    private suspend fun runScan(appContext: Context) {
        val enabled = folders.filter { it.enabled }
        if (enabled.isEmpty()) {
            tracks.clear()
            withContext(Dispatchers.IO) { LibraryTrackCache.clear(appContext) }
            scanState = LibraryScanState.Idle
            return
        }
        val tracker = MusicScanner.Tracker()
        val streaming = tracks.isEmpty()
        tracker.collectTracks = streaming
        if (streaming) scanState = LibraryScanState.Scanning
        scanProgress = tracker.progress()
        try {
            coroutineScope {
                val sampler = launch {
                    val streamed = ArrayList<Track>()
                    var samples = 0
                    while (isActive) {
                        delay(PROGRESS_SAMPLE_MILLIS)
                        scanProgress = tracker.progress()
                        samples++
                        if (streaming && samples % STREAM_EVERY_SAMPLES == 0) {
                            val fresh = tracker.drainTracks()
                            if (fresh.isNotEmpty()) {
                                streamed.addAll(fresh)
                                val snapshot = streamed.toList()
                                val sorted = withContext(Dispatchers.Default) { sortTracks(ScanFilters.apply(snapshot), sortOrder, sortDirection) }
                                replaceTracks(sorted)
                                scanState = LibraryScanState.Loaded
                            }
                        }
                    }
                }
                val cacheIndex = withContext(Dispatchers.IO) { LibraryTrackCache.loadIndex(appContext) }
                val result = MusicScanner.scan(appContext, enabled, cacheIndex, tracker)
                sampler.cancel()
                val count = result.tracks.size
                scanProgress = ScanProgress(ScanPhase.SAVING, count, count, count)
                val sorted = withContext(Dispatchers.Default) { sortTracks(ScanFilters.apply(result.tracks), sortOrder, sortDirection) }
                replaceTracks(sorted)
                withContext(Dispatchers.IO) { LibraryTrackCache.save(appContext, result.cacheEntries) }
                scanState = if (tracks.isEmpty()) LibraryScanState.Empty else LibraryScanState.Loaded
            }
        } finally {
            scanProgress = null
            if (scanState == LibraryScanState.Scanning) {
                scanState = if (tracks.isEmpty()) LibraryScanState.Idle else LibraryScanState.Loaded
            }
        }
    }
}
