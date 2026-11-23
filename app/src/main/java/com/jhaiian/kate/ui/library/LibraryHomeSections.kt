package com.jhaiian.kate.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.player.rememberPlaybackPositionMs
import com.jhaiian.kate.ui.settings.SectionHeader
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val CarouselLimit = 20
private val CarouselCardSize = 132.dp
private val CarouselArtCorner = 16.dp
private const val ContinueProgressSpanPx = 400f

@Composable
internal fun rememberRecentlyPlayed(): List<Track> {
    val context = LocalContext.current
    val version = ListeningHistory.version
    val librarySize = LibraryStore.tracks.size
    val tracks by produceState(initialValue = emptyList<Track>(), version, librarySize) {
        val byId = LibraryStore.tracks.associateBy { it.id }
        value = withContext(Dispatchers.Default) {
            ListeningHistory.plays(context, 0L, System.currentTimeMillis())
                .sortedByDescending { it.startedAt }
                .asSequence()
                .mapNotNull { byId[it.trackId] }
                .distinctBy { it.id }
                .take(CarouselLimit)
                .toList()
        }
    }
    return tracks
}

@Composable
internal fun rememberMostPlayed(): List<Track> {
    val context = LocalContext.current
    val version = ListeningHistory.version
    val librarySize = LibraryStore.tracks.size
    val tracks by produceState(initialValue = emptyList<Track>(), version, librarySize) {
        val byId = LibraryStore.tracks.associateBy { it.id }
        value = withContext(Dispatchers.Default) {
            ListeningHistory.plays(context, 0L, System.currentTimeMillis())
                .groupingBy { it.trackId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .mapNotNull { byId[it.key] }
                .take(CarouselLimit)
        }
    }
    return tracks
}

@Composable
internal fun ContinueListeningCard(onOpenPlayer: () -> Unit) {
    val track = PlayerController.currentTrack ?: return
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val corners = LocalLookAndFeel.current.libraryCorners
    val position = rememberPlaybackPositionMs { ContinueProgressSpanPx }
    val durationMs = PlayerController.durationMs.takeIf { it > 0 } ?: track.durationMs
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        SectionHeader(text = stringResource(R.string.home_continue_listening), tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .clickable(onClick = onOpenPlayer)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                ArtThumb(
                    track = track,
                    icon = Icons.Filled.MusicNote,
                    shape = RoundedCornerShape(LibraryStylePreferences.scaledCorner(14.dp, corners)),
                    iconSize = 28.dp,
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = trackArtist(track),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                KatePlayButton(
                    isPlaying = PlayerController.isPlaying,
                    baseSize = 48.dp,
                    followSize = false,
                    allowGlow = false,
                    onClick = {
                        haptics.click()
                        PlayerController.togglePlayPause()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.primary.copy(alpha = 0.16f))
            ) {
                val fraction = if (durationMs > 0) (position.value.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(colors.primary)
                )
            }
        }
    }
}

@Composable
internal fun TrackCarousel(
    title: String,
    tracks: List<Track>,
    onTrackClick: (List<Track>, Track) -> Unit
) {
    if (tracks.isEmpty()) return
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val corners = LocalLookAndFeel.current.libraryCorners
    val currentId = PlayerController.currentTrack?.id
    CarouselFrame(title = title) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                CarouselCard(
                    title = track.title,
                    subtitle = trackArtist(track),
                    highlighted = track.id == currentId,
                    art = {
                        ArtThumb(
                            track = track,
                            icon = Icons.Filled.MusicNote,
                            shape = RoundedCornerShape(LibraryStylePreferences.scaledCorner(CarouselArtCorner, corners)),
                            iconSize = 40.dp,
                            modifier = Modifier.size(CarouselCardSize)
                        )
                    },
                    titleColor = if (track.id == currentId) colors.primary else colors.onSurface,
                    onClick = {
                        haptics.click()
                        onTrackClick(tracks, track)
                    }
                )
            }
        }
    }
}

@Composable
internal fun PlaylistCarousel(onPlaylistClick: (String) -> Unit) {
    val playlists = PlaylistStore.playlists
    if (playlists.isEmpty()) return
    val colors = LocalClintColors.current
    val corners = LocalLookAndFeel.current.libraryCorners
    val byId = LibraryStore.tracks.associateBy { it.id }
    CarouselFrame(title = stringResource(R.string.home_your_playlists)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                val tracks = playlist.trackIds.mapNotNull { byId[it] }
                CarouselCard(
                    title = playlist.name,
                    subtitle = pluralStringResource(R.plurals.library_song_count, tracks.size, tracks.size),
                    highlighted = false,
                    art = {
                        ArtThumb(
                            track = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull(),
                            icon = LibraryCategory.PLAYLISTS.icon,
                            shape = RoundedCornerShape(LibraryStylePreferences.scaledCorner(CarouselArtCorner, corners)),
                            iconSize = 40.dp,
                            modifier = Modifier.size(CarouselCardSize)
                        )
                    },
                    titleColor = colors.onSurface,
                    onClick = { onPlaylistClick(playlist.id) }
                )
            }
        }
    }
}

@Composable
private fun CarouselFrame(title: String, content: @Composable () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        SectionHeader(text = title, tint = colors.primary)
        content()
    }
}

@Composable
private fun CarouselCard(
    title: String,
    subtitle: String,
    highlighted: Boolean,
    art: @Composable () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .width(CarouselCardSize)
            .clip(RoundedCornerShape(CarouselArtCorner))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = if (highlighted) {
                Modifier.selectionHighlight(true, colors.primary, CarouselArtCorner)
            } else {
                Modifier
            }
        ) {
            art()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 8.dp)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
        )
    }
}
