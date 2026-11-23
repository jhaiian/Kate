package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.theme.LibraryLayoutPreferences
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AlbumGroup
import com.jhaiian.kate.data.ArtistGroup
import com.jhaiian.kate.data.ArtistImage
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.data.TrackGroup
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.ArtistArt
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalNavAnimatedScope
import com.jhaiian.kate.ui.components.sharedBoundsIn
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

internal val LibraryCategory.labelRes: Int
    get() = when (this) {
        LibraryCategory.SONGS -> R.string.library_category_songs
        LibraryCategory.ALBUMS -> R.string.library_category_albums
        LibraryCategory.ARTISTS -> R.string.library_category_artists
        LibraryCategory.ALBUM_ARTISTS -> R.string.library_category_album_artists
        LibraryCategory.GENRES -> R.string.library_category_genres
        LibraryCategory.FOLDERS -> R.string.library_category_folders
        LibraryCategory.YEARS -> R.string.library_category_years
        LibraryCategory.RECENTLY_ADDED -> R.string.library_category_recently_added
        LibraryCategory.FAVORITES -> R.string.library_category_favorites
        LibraryCategory.PLAYLISTS -> R.string.library_category_playlists
    }

internal val LibraryCategory.icon: ImageVector
    get() = when (this) {
        LibraryCategory.SONGS -> Icons.Filled.MusicNote
        LibraryCategory.ALBUMS -> Icons.Filled.Album
        LibraryCategory.ARTISTS -> Icons.Filled.Mic
        LibraryCategory.ALBUM_ARTISTS -> Icons.Filled.Person
        LibraryCategory.GENRES -> Icons.Filled.Category
        LibraryCategory.FOLDERS -> Icons.Filled.Folder
        LibraryCategory.YEARS -> Icons.Filled.DateRange
        LibraryCategory.RECENTLY_ADDED -> Icons.Filled.LibraryAdd
        LibraryCategory.FAVORITES -> Icons.Filled.Favorite
        LibraryCategory.PLAYLISTS -> Icons.AutoMirrored.Filled.QueueMusic
    }

internal fun categoryIconKey(category: LibraryCategory) = "library_category_icon_${category.key}"

internal fun categoryLabelKey(category: LibraryCategory) = "library_category_label_${category.key}"

internal fun albumArtKey(albumKey: String) = "library_album_art_$albumKey"

internal fun artistArtKey(artistKey: String, albumArtist: Boolean) = "library_artist_art_${albumArtist}_$artistKey"

internal fun groupArtKey(category: LibraryCategory, groupKey: String) = "library_group_art_${category.key}_$groupKey"

internal fun playlistArtKey(playlistId: String) = "library_playlist_art_$playlistId"

@Composable
internal fun categoryCountText(category: LibraryCategory, count: Int): String = when (category) {
    LibraryCategory.SONGS, LibraryCategory.RECENTLY_ADDED, LibraryCategory.FAVORITES -> pluralStringResource(R.plurals.library_song_count, count, count)
    LibraryCategory.ALBUMS -> pluralStringResource(R.plurals.library_album_count, count, count)
    LibraryCategory.ARTISTS, LibraryCategory.ALBUM_ARTISTS -> pluralStringResource(R.plurals.library_artist_count, count, count)
    LibraryCategory.GENRES -> pluralStringResource(R.plurals.library_genre_count, count, count)
    LibraryCategory.FOLDERS -> pluralStringResource(R.plurals.library_folder_count, count, count)
    LibraryCategory.YEARS -> pluralStringResource(R.plurals.library_year_count, count, count)
    LibraryCategory.PLAYLISTS -> pluralStringResource(R.plurals.library_playlist_count, count, count)
}

@Composable
internal fun albumTitle(album: AlbumGroup): String = album.title.ifBlank { stringResource(R.string.player_unknown_album) }

@Composable
internal fun albumArtistLabel(album: AlbumGroup): String {
    val artist = album.artist ?: return stringResource(R.string.library_various_artists)
    return artist.ifBlank { stringResource(R.string.player_unknown_artist) }
}

@Composable
internal fun artistName(artist: ArtistGroup): String = artist.name.ifBlank { stringResource(R.string.player_unknown_artist) }

@Composable
internal fun trackArtist(track: Track): String = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) }

@Composable
internal fun groupName(category: LibraryCategory, group: TrackGroup): String = when {
    group.name.isNotBlank() -> group.name
    category == LibraryCategory.YEARS -> stringResource(R.string.library_unknown_year)
    else -> stringResource(R.string.library_unknown_genre)
}

internal fun joinMeta(vararg parts: String): String = parts.filter { it.isNotBlank() }.joinToString(" · ")

@Composable
internal fun ArtThumb(
    track: Track?,
    icon: ImageVector,
    shape: Shape,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    artistImage: ArtistImage? = null,
    allowHardware: Boolean = true
) {
    val colors = LocalClintColors.current
    if (artistImage != null) {
        ArtistArt(
            image = artistImage,
            fallback = track,
            allowHardware = allowHardware,
            modifier = modifier
                .clip(shape)
                .background(colors.surfaceVariant)
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(iconSize))
        }
    } else if (track == null) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(iconSize))
        }
    } else {
        AlbumArt(
            track = track,
            allowHardware = allowHardware,
            modifier = modifier
                .clip(shape)
                .background(colors.surfaceVariant)
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
internal fun CategoryRow(
    category: LibraryCategory,
    count: Int,
    contentScope: AnimatedVisibilityScope?,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(
                imageVector = category.icon,
                contentDescription = null,
                tint = kateTileTint(),
                modifier = Modifier
                    .sharedBoundsIn(categoryIconKey(category), contentScope)
                    .size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                text = stringResource(category.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.sharedBoundsIn(categoryLabelKey(category), contentScope)
            )
            Text(
                text = categoryCountText(category, count),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1
            )
        }
        KateRowArrow()
    }
}

@Composable
internal fun AlbumCard(
    album: AlbumGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val corners = LocalLookAndFeel.current.libraryCorners
    val artShape = RoundedCornerShape(LibraryStylePreferences.scaledCorner(16.dp, corners))
    val currentId = PlayerController.currentTrack?.id
    val playing = currentId != null && album.tracks.any { it.id == currentId }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(LibraryStylePreferences.scaledCorner(18.dp, corners)))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        ArtThumb(
            track = album.artTrack,
            icon = Icons.Filled.Album,
            shape = artShape,
            iconSize = 40.dp,
            modifier = Modifier
                .sharedBoundsIn(albumArtKey(album.key), LocalNavAnimatedScope.current)
                .fillMaxWidth()
                .aspectRatio(1f)
                .let { if (playing) it.border(BorderStroke(2.dp, colors.primary), artShape) else it }
        )
        Text(
            text = albumTitle(album),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (playing) colors.primary else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp, end = 2.dp)
        )
        Text(
            text = joinMeta(albumArtistLabel(album), album.year?.toString().orEmpty()),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
        )
    }
}

private fun joinListSubtitle(vararg parts: String): String = parts.filter { it.isNotBlank() }.joinToString(" • ")

@Composable
internal fun rememberSongTiles(tracks: List<Track>): List<LibraryTile> {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    val style = LocalLookAndFeel.current
    val showBadges = style.libraryShowBadges
    val showDuration = style.libraryShowDuration
    val titleSource = style.songTitleSource
    val fields = style.songSubtitleFields
    val trackNumbers = style.songShowTrackNumber
    return remember(tracks, configuration, showBadges, showDuration, titleSource, fields, trackNumbers) {
        val unknownArtist = resources.getString(R.string.player_unknown_artist)
        val unknownAlbum = resources.getString(R.string.player_unknown_album)
        tracks.map { track ->
            val artist = track.artist.ifBlank { unknownArtist }
            val baseTitle = if (titleSource == LibraryLayoutPreferences.TITLE_FILENAME) track.filename.ifBlank { track.title } else track.title
            val subtitleParts = fields.map { field ->
                when (field) {
                    LibraryLayoutPreferences.FIELD_ARTIST -> artist
                    LibraryLayoutPreferences.FIELD_ALBUM -> track.album.ifBlank { unknownAlbum }
                    LibraryLayoutPreferences.FIELD_ALBUM_ARTIST -> track.albumArtist
                    LibraryLayoutPreferences.FIELD_YEAR -> track.year?.toString().orEmpty()
                    LibraryLayoutPreferences.FIELD_GENRE -> track.genre
                    LibraryLayoutPreferences.FIELD_FORMAT -> track.format
                    LibraryLayoutPreferences.FIELD_FOLDER -> track.path.substringBeforeLast('/', "").substringAfterLast('/').substringAfterLast(':')
                    else -> ""
                }
            }
            LibraryTile(
                id = track.id,
                title = if (trackNumbers && track.trackNumber != null) "${track.trackNumber}. $baseTitle" else baseTitle,
                gridSubtitle = subtitleParts.firstOrNull { it.isNotBlank() } ?: artist,
                listSubtitle = joinListSubtitle(*subtitleParts.toTypedArray()),
                art = track,
                artistImage = null,
                icon = Icons.Filled.MusicNote,
                meta = if (showDuration) formatDuration(track.durationMs) else null,
                chips = if (showBadges) {
                    listOfNotNull(track.format.takeIf { it.isNotBlank() }, track.bitDepth?.let { "$it-bit" })
                } else {
                    emptyList()
                },
                trackIds = setOf(track.id),
                sharedKey = null
            )
        }
    }
}

@Composable
internal fun rememberAlbumTiles(albums: List<AlbumGroup>): List<LibraryTile> {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    return remember(albums, configuration) {
        val unknownAlbum = resources.getString(R.string.player_unknown_album)
        val unknownArtist = resources.getString(R.string.player_unknown_artist)
        val various = resources.getString(R.string.library_various_artists)
        albums.map { album ->
            val artist = album.artist?.ifBlank { unknownArtist } ?: various
            LibraryTile(
                id = album.key,
                title = album.title.ifBlank { unknownAlbum },
                gridSubtitle = artist,
                listSubtitle = joinListSubtitle(artist, album.year?.toString().orEmpty()),
                art = album.artTrack,
                artistImage = null,
                icon = Icons.Filled.Album,
                meta = resources.getQuantityString(R.plurals.library_song_count, album.tracks.size, album.tracks.size),
                chips = emptyList(),
                trackIds = album.tracks.mapTo(HashSet<String>()) { it.id },
                sharedKey = albumArtKey(album.key)
            )
        }
    }
}

@Composable
internal fun rememberArtistTiles(artists: List<ArtistGroup>, albumArtist: Boolean, icon: ImageVector): List<LibraryTile> {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    val artistImages = ArtistImageStore.images
    return remember(artists, albumArtist, configuration, artistImages) {
        val unknownArtist = resources.getString(R.string.player_unknown_artist)
        artists.map { artist ->
            val albums = resources.getQuantityString(R.plurals.library_album_count, artist.albums.size, artist.albums.size)
            val songs = resources.getQuantityString(R.plurals.library_song_count, artist.tracks.size, artist.tracks.size)
            LibraryTile(
                id = artist.key,
                title = artist.name.ifBlank { unknownArtist },
                gridSubtitle = albums,
                listSubtitle = joinListSubtitle(albums, songs),
                art = artist.artTrack,
                artistImage = artistImages[artist.key],
                icon = icon,
                meta = null,
                chips = emptyList(),
                trackIds = artist.tracks.mapTo(HashSet<String>()) { it.id },
                sharedKey = artistArtKey(artist.key, albumArtist)
            )
        }
    }
}

@Composable
internal fun rememberGroupTiles(category: LibraryCategory, groups: List<TrackGroup>): List<LibraryTile> {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    return remember(category, groups, configuration) {
        val unknown = resources.getString(
            if (category == LibraryCategory.YEARS) R.string.library_unknown_year else R.string.library_unknown_genre
        )
        groups.map { group ->
            val songs = resources.getQuantityString(R.plurals.library_song_count, group.tracks.size, group.tracks.size)
            LibraryTile(
                id = group.key,
                title = group.name.ifBlank { unknown },
                gridSubtitle = songs,
                listSubtitle = if (category == LibraryCategory.FOLDERS) joinListSubtitle(group.detail, songs) else songs,
                art = if (category == LibraryCategory.GENRES) group.artTrack else null,
                artistImage = null,
                icon = category.icon,
                meta = null,
                chips = emptyList(),
                trackIds = group.tracks.mapTo(HashSet<String>()) { it.id },
                sharedKey = groupArtKey(category, group.key)
            )
        }
    }
}

@Composable
internal fun rememberPlaylistTiles(): List<LibraryTile> {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    val tiles by remember(configuration) {
        derivedStateOf {
            val byId = LibraryStore.tracks.associateBy { it.id }
            PlaylistStore.playlists.map { playlist ->
                val tracks = playlist.trackIds.mapNotNull { byId[it] }
                val songs = resources.getQuantityString(R.plurals.library_song_count, tracks.size, tracks.size)
                LibraryTile(
                    id = playlist.id,
                    title = playlist.name,
                    gridSubtitle = songs,
                    listSubtitle = songs,
                    art = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull(),
                    artistImage = null,
                    icon = LibraryCategory.PLAYLISTS.icon,
                    meta = null,
                    chips = emptyList(),
                    trackIds = tracks.mapTo(HashSet<String>()) { it.id },
                    sharedKey = playlistArtKey(playlist.id)
                )
            }
        }
    }
    return tiles
}

@Composable
internal fun CategorySwitcherDialog(
    visible: Boolean,
    current: LibraryCategory?,
    onDismiss: () -> Unit,
    onSelect: (LibraryCategory) -> Unit
) {
    val colors = LocalClintColors.current
    val index = LibraryStore.index
    val songCount = LibraryStore.tracks.size
    val favoriteCount = FavoritesStore.favoriteTracks(LibraryStore.tracks).size
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(28.dp))
                .padding(bottom = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 10.dp, top = 16.dp, bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .kateIconTile(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KateIcon(
                        imageVector = Icons.Outlined.LibraryMusic,
                        contentDescription = null,
                        tint = kateTileTint(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.library_switch_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                KateIconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            val look = LocalLookAndFeel.current
            val visible = (orderedCategories(look, LibraryCategory.Browse) + orderedCategories(look, LibraryCategory.Collections))
                .ifEmpty { listOf(LibraryCategory.SONGS) }
                .let { list -> if (current != null && current !in list) list + current else list }
            LazyColumn(contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                items(visible, key = { it.key }) { option ->
                    SwitcherOptionRow(
                        category = option,
                        count = index.count(option, songCount, favoriteCount),
                        selected = option == current,
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitcherOptionRow(
    category: LibraryCategory,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val rowBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "switcher_row_background"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurface,
        animationSpec = tween(durationMillis = 220),
        label = "switcher_row_content"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        KateIcon(
            imageVector = category.icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.secondaryText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText
        )
    }
}
