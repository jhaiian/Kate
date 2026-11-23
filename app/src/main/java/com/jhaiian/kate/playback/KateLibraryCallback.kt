package com.jhaiian.kate.playback

import com.jhaiian.kate.data.SystemControls
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionCommand
import androidx.core.content.IntentCompat
import android.view.KeyEvent
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryIndex
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.PlaybackStateStore
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.Track
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
internal class KateLibraryCallback(context: Context) : MediaLibrarySession.Callback {
    private companion object {
        const val ROOT = "kate_root"
        const val SHUFFLE_ALL = "kate_shuffle_all"
        const val SONGS = "kate_songs"
        const val ALBUMS = "kate_albums"
        const val ARTISTS = "kate_artists"
        const val PLAYLISTS = "kate_playlists"
        const val FAVORITES = "kate_favorites"
        const val ALBUM_PREFIX = "kate_album:"
        const val ARTIST_PREFIX = "kate_artist:"
        const val PLAYLIST_PREFIX = "kate_playlist:"
        const val TRACK_PREFIX = "kate_track:"
        const val SEPARATOR = '\u001F'
        const val SEARCH_LIMIT = 100
    }

    private val appContext = context.applicationContext
    private val headsetButtons = HeadsetButtons(context)

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
        val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
        SystemControls.CommandActions.forEach { commands.add(SessionControls.command(it)) }
        return MediaSession.ConnectionResult.AcceptedResultBuilder()
            .setAvailableSessionCommands(commands.build())
            .setAvailablePlayerCommands(MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        val action = SessionControls.actionOf(customCommand)
            ?: return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
        SessionControls.perform(appContext, session.player, action)
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onMediaButtonEvent(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, intent: Intent): Boolean {
        val event = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java) ?: return false
        return headsetButtons.handle(session.player, event)
    }
    private val executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())
    private var builtIndex: LibraryIndex? = null
    private var builtFrom: List<Track>? = null

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val extras = Bundle().apply {
            putInt(MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM)
            putInt(MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM)
        }
        val root = folder(ROOT, appContext.getString(R.string.app_name), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
        return Futures.immediateFuture(LibraryResult.ofItem(root, LibraryParams.Builder().setExtras(extras).build()))
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        ensureLoaded()
        val item = itemFor(mediaId) ?: return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        return Futures.immediateFuture(LibraryResult.ofItem(item, null))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        ensureLoaded()
        val children = childrenOf(parentId)
            ?: return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        return Futures.immediateFuture(LibraryResult.ofItemList(paged(children, page, pageSize), params))
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        ensureLoaded()
        if (query.isNotBlank()) session.notifySearchResultChanged(browser, query, search(query).size, params)
        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        ensureLoaded()
        val parent = "search" + SEPARATOR + query
        val items = search(query).map { trackItem(parent, it) }
        return Futures.immediateFuture(LibraryResult.ofItemList(paged(items, page, pageSize), params))
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        if (mediaItems.all { it.localConfiguration != null }) return Futures.immediateFuture(mediaItems)
        ensureLoaded()
        val resolved = mediaItems.flatMap { item ->
            if (item.localConfiguration != null) {
                listOf(item)
            } else {
                resolve(item)?.let { (tracks, _) -> tracks.map { it.toMediaItem(appContext) } }.orEmpty()
            }
        }
        if (resolved.isEmpty()) return Futures.immediateFailedFuture(UnsupportedOperationException())
        return Futures.immediateFuture(resolved.toMutableList())
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        if (mediaItems.isNotEmpty() && mediaItems.all { it.localConfiguration != null }) {
            return Futures.immediateFuture(MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs))
        }
        ensureLoaded()
        val request = mediaItems.getOrNull(startIndex.coerceAtLeast(0)) ?: mediaItems.firstOrNull()
        val resolved = request?.let { resolve(it) }
            ?: return Futures.immediateFailedFuture(UnsupportedOperationException())
        val (tracks, start) = resolved
        if (tracks.isEmpty()) return Futures.immediateFailedFuture(UnsupportedOperationException())
        val index = start.coerceIn(0, tracks.lastIndex)
        PlaybackStateStore.saveQueue(appContext, tracks.map { it.id })
        PlaybackStateStore.savePosition(appContext, tracks[index].id, index, 0L)
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(tracks.map { it.toMediaItem(appContext) }, index, 0L)
        )
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = executor.submit<MediaSession.MediaItemsWithStartPosition> {
        val saved = PlaybackStateStore.load(appContext)
        val byId = LibraryTrackCache.loadTracks(appContext).associateBy { it.id }
        val tracks = saved.trackIds.mapNotNull { byId[it] }
        check(tracks.isNotEmpty()) { "Nothing to resume" }
        val savedIndex = tracks.indexOfFirst { it.id == saved.currentId }
        val startIndex = if (savedIndex >= 0) savedIndex else saved.index.coerceIn(0, tracks.lastIndex)
        val startPosition = if (savedIndex >= 0) saved.positionMs else 0L
        MediaSession.MediaItemsWithStartPosition(
            tracks.map { it.toMediaItem(appContext) },
            startIndex,
            startPosition
        )
    }

    private fun ensureLoaded() {
        LibraryStore.ensureLoaded(appContext)
        PlaylistStore.ensureLoaded(appContext)
        FavoritesStore.ensureLoaded(appContext)
    }

    private fun allTracks(): List<Track> = LibraryStore.tracks.toList()

    private fun index(): LibraryIndex {
        val live = LibraryStore.index
        if (live.albums.isNotEmpty() || LibraryStore.tracks.isEmpty()) return live
        val tracks = allTracks()
        val cached = builtIndex
        if (cached != null && builtFrom == tracks) return cached
        return LibraryIndex.build(tracks).also {
            builtIndex = it
            builtFrom = tracks
        }
    }

    private fun tracksOf(parentId: String): List<Track>? = when {
        parentId == SONGS || parentId == SHUFFLE_ALL -> allTracks()
        parentId == FAVORITES -> {
            val ids = FavoritesStore.ids
            allTracks().filter { it.id in ids }
        }
        parentId.startsWith(ALBUM_PREFIX) -> index().album(parentId.removePrefix(ALBUM_PREFIX))?.tracks
        parentId.startsWith(ARTIST_PREFIX) -> index().artist(parentId.removePrefix(ARTIST_PREFIX), albumArtist = false)?.tracks
        parentId.startsWith(PLAYLIST_PREFIX) -> {
            val playlist = PlaylistStore.playlist(parentId.removePrefix(PLAYLIST_PREFIX))
            if (playlist == null) {
                null
            } else {
                val byId = allTracks().associateBy { it.id }
                playlist.trackIds.mapNotNull { byId[it] }
            }
        }
        parentId.startsWith("search" + SEPARATOR) -> search(parentId.substringAfter(SEPARATOR))
        else -> null
    }

    private fun childrenOf(parentId: String): List<MediaItem>? {
        if (parentId == ROOT) {
            SystemControls.ensureLoaded(appContext)
            val sections = listOf(
                SystemControls.AUTO_SHUFFLE to { playableAction(SHUFFLE_ALL, appContext.getString(R.string.widget_quick_shuffle)) },
                SystemControls.AUTO_SONGS to { folder(SONGS, appContext.getString(R.string.library_category_songs), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED, gridChildren = false) },
                SystemControls.AUTO_ALBUMS to { folder(ALBUMS, appContext.getString(R.string.library_category_albums), MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS, gridChildren = true) },
                SystemControls.AUTO_ARTISTS to { folder(ARTISTS, appContext.getString(R.string.library_category_artists), MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS, gridChildren = true) },
                SystemControls.AUTO_PLAYLISTS to { folder(PLAYLISTS, appContext.getString(R.string.library_category_playlists), MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS, gridChildren = false) },
                SystemControls.AUTO_FAVORITES to { folder(FAVORITES, appContext.getString(R.string.library_category_favorites), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED, gridChildren = false) }
            )
            val visible = sections.filter { SystemControls.autoVisible(it.first) }.ifEmpty { sections }
            return visible.map { it.second() }
        }
        if (parentId == ALBUMS) {
            return index().albums.map { album ->
                folder(
                    ALBUM_PREFIX + album.key,
                    album.title.ifBlank { appContext.getString(R.string.player_unknown_album) },
                    MediaMetadata.MEDIA_TYPE_ALBUM,
                    subtitle = album.artist,
                    artwork = artworkUri(album.artTrack)
                )
            }
        }
        if (parentId == ARTISTS) {
            return index().artists.map { artist ->
                folder(
                    ARTIST_PREFIX + artist.key,
                    artist.name.ifBlank { appContext.getString(R.string.player_unknown_artist) },
                    MediaMetadata.MEDIA_TYPE_ARTIST,
                    artwork = artworkUri(artist.artTrack)
                )
            }
        }
        if (parentId == PLAYLISTS) {
            return PlaylistStore.playlists.map { playlist ->
                folder(PLAYLIST_PREFIX + playlist.id, playlist.name, MediaMetadata.MEDIA_TYPE_PLAYLIST)
            }
        }
        val tracks = tracksOf(parentId) ?: return null
        return tracks.map { trackItem(parentId, it) }
    }

    private fun itemFor(mediaId: String): MediaItem? {
        if (mediaId == ROOT) return folder(ROOT, appContext.getString(R.string.app_name), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
        if (mediaId.startsWith(TRACK_PREFIX)) {
            val (parent, trackId) = splitTrackId(mediaId) ?: return null
            val track = allTracks().firstOrNull { it.id == trackId } ?: return null
            return trackItem(parent, track)
        }
        return childrenOf(ROOT)?.firstOrNull { it.mediaId == mediaId }
            ?: childrenOf(ALBUMS)?.firstOrNull { it.mediaId == mediaId }
            ?: childrenOf(ARTISTS)?.firstOrNull { it.mediaId == mediaId }
            ?: childrenOf(PLAYLISTS)?.firstOrNull { it.mediaId == mediaId }
    }

    private fun resolve(item: MediaItem): Pair<List<Track>, Int>? {
        val mediaId = item.mediaId
        val query = item.requestMetadata.searchQuery
        if (mediaId.isEmpty() && query != null) {
            val results = if (query.isBlank()) allTracks().shuffled() else search(query)
            return results to 0
        }
        if (mediaId == SHUFFLE_ALL) return allTracks().shuffled() to 0
        if (mediaId.startsWith(TRACK_PREFIX)) {
            val (parent, trackId) = splitTrackId(mediaId) ?: return null
            val tracks = tracksOf(parent) ?: allTracks()
            val start = tracks.indexOfFirst { it.id == trackId }
            if (start >= 0) return tracks to start
            val single = allTracks().firstOrNull { it.id == trackId } ?: return null
            return listOf(single) to 0
        }
        val tracks = tracksOf(mediaId) ?: return null
        return tracks to 0
    }

    private fun search(query: String): List<Track> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return emptyList()
        val tracks = allTracks()
        val byTitle = tracks.filter { it.title.lowercase().contains(needle) }
        val byOther = tracks.filter {
            it !in byTitle && (it.artist.lowercase().contains(needle) || it.album.lowercase().contains(needle))
        }
        return (byTitle + byOther).take(SEARCH_LIMIT)
    }

    private fun splitTrackId(mediaId: String): Pair<String, String>? {
        val body = mediaId.removePrefix(TRACK_PREFIX)
        val cut = body.lastIndexOf(SEPARATOR)
        if (cut < 0) return null
        return body.substring(0, cut) to body.substring(cut + 1)
    }

    private fun paged(items: List<MediaItem>, page: Int, pageSize: Int): List<MediaItem> {
        if (pageSize <= 0 || pageSize == Int.MAX_VALUE) return items
        val from = page.toLong() * pageSize
        if (from >= items.size) return emptyList()
        val to = minOf(items.size.toLong(), from + pageSize)
        return items.subList(from.toInt(), to.toInt())
    }

    private fun folder(
        mediaId: String,
        title: String,
        mediaType: Int,
        gridChildren: Boolean? = null,
        subtitle: String? = null,
        artwork: Uri? = null
    ): MediaItem {
        val extras = gridChildren?.let { grid ->
            Bundle().apply {
                putInt(
                    MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                    if (grid) MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM else MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                )
            }
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtist(subtitle)
            .setArtworkUri(artwork)
            .setIsBrowsable(true)
            .setIsPlayable(mediaType != MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS && mediaType != MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS &&
                mediaType != MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS && mediaId != ROOT)
            .setMediaType(mediaType)
            .setExtras(extras)
            .build()
        return MediaItem.Builder().setMediaId(mediaId).setMediaMetadata(metadata).build()
    }

    private fun playableAction(mediaId: String, title: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
            .build()
        return MediaItem.Builder().setMediaId(mediaId).setMediaMetadata(metadata).build()
    }

    private fun trackItem(parentId: String, track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist.ifBlank { appContext.getString(R.string.player_unknown_artist) })
            .setAlbumTitle(track.album.ifBlank { appContext.getString(R.string.player_unknown_album) })
            .setSubtitle(track.artist.ifBlank { appContext.getString(R.string.player_unknown_artist) })
            .setArtworkUri(artworkUri(track))
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()
        return MediaItem.Builder()
            .setMediaId(TRACK_PREFIX + parentId + SEPARATOR + track.id)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun artworkUri(track: Track?): Uri? {
        val key = track?.artKey ?: return null
        if (!LibraryTrackCache.artFile(appContext, key).exists()) return null
        return ArtworkProvider.uriFor(appContext, key)
    }
}
