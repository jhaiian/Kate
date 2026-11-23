package com.jhaiian.kate.ui.player

import com.jhaiian.kate.ui.tags.TagEditorRequest
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Edit
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import com.jhaiian.kate.playback.ShuffleMode
import com.jhaiian.kate.playback.ShufflePreferences
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.RadioRequest
import com.jhaiian.kate.playback.KateRadio
import com.jhaiian.kate.playback.RadioSeed
import com.jhaiian.kate.playback.RadioSeedType
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.Radio
import com.jhaiian.kate.ui.components.ShuffleModeOptionRow
import com.jhaiian.kate.ui.components.shuffleModeDescription
import com.jhaiian.kate.ui.components.shuffleModeIcon
import com.jhaiian.kate.ui.components.shuffleModeTitle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistNames
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.settings.equalizerStatus
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import kotlinx.coroutines.delay

private val SleepTimerMinutes = listOf(15, 30, 45, 60, 90)

@Composable
internal fun PlayerOptionsMenu(
    visible: Boolean,
    track: Track,
    onDismiss: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onShowInfo: () -> Unit,
    onShowLyrics: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onShowEqualizer: () -> Unit,
    onShowAudio: () -> Unit,
    onShowShuffleModes: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    val colors = LocalClintColors.current
    val index = LibraryStore.index
    val albumKey = remember(track.id, index) {
        index.albums.firstOrNull { album -> album.tracks.any { it.id == track.id } }?.key
    }
    val artists = remember(track.id, index) {
        ArtistNames.split(track.artist)
            .map { ArtistNames.key(it) to it }
            .filter { (key, _) -> index.artist(key, albumArtist = false) != null }
    }
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            RowDivider(color = colors.surfaceVariant)
            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                if (albumKey != null) {
                    PlayerMenuRow(
                        icon = Icons.Filled.Album,
                        title = stringResource(R.string.player_menu_go_to_album),
                        subtitle = track.album.ifBlank { stringResource(R.string.player_unknown_album) },
                        onClick = { onOpenAlbum(albumKey) }
                    )
                }
                artists.forEach { (key, name) ->
                    PlayerMenuRow(
                        icon = Icons.Filled.Mic,
                        title = stringResource(R.string.player_menu_go_to_artist),
                        subtitle = name,
                        onClick = { onOpenArtist(key) }
                    )
                }
                PlayerMenuRow(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    title = stringResource(R.string.playlist_add_to),
                    subtitle = null,
                    onClick = onAddToPlaylist
                )
                val radio = KateRadio.session
                if (radio != null) {
                    PlayerMenuRow(
                        icon = Icons.Filled.Radio,
                        title = stringResource(R.string.radio_title),
                        subtitle = stringResource(R.string.radio_based_on, radio.label),
                        onClick = {
                            onDismiss()
                            RadioRequest.manage()
                        }
                    )
                }
                PlayerMenuRow(
                    icon = Icons.Outlined.Radio,
                    title = stringResource(R.string.radio_start_from_song),
                    subtitle = null,
                    onClick = {
                        onDismiss()
                        RadioRequest.open(RadioSeed(RadioSeedType.SONG, track.id, track.title))
                    }
                )
                PlayerMenuRow(
                    icon = Icons.Filled.FormatQuote,
                    title = stringResource(R.string.player_lyrics),
                    subtitle = null,
                    onClick = onShowLyrics
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Edit,
                    title = stringResource(R.string.sync_title),
                    subtitle = stringResource(R.string.sync_menu_subtitle),
                    onClick = {
                        onDismiss()
                        LyricsMakerRequest.open(track, null)
                    }
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Sell,
                    title = stringResource(R.string.tag_editor_title),
                    subtitle = stringResource(R.string.tag_editor_menu_subtitle),
                    onClick = {
                        onDismiss()
                        TagEditorRequest.open(listOf(track))
                    }
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.player_menu_song_info),
                    subtitle = null,
                    onClick = onShowInfo
                )
                PlayerMenuRow(
                    icon = Icons.Filled.GraphicEq,
                    title = stringResource(R.string.eq_title),
                    subtitle = equalizerStatus(),
                    onClick = onShowEqualizer
                )
                PlayerMenuRow(
                    icon = Icons.Filled.BarChart,
                    title = stringResource(R.string.visualizer_title),
                    subtitle = stringResource(visualizerStyleLabel(LocalLookAndFeel.current.visualizerStyle)),
                    onClick = {
                        onDismiss()
                        VisualizerFocus.sheetOpen = true
                    }
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Speed,
                    title = stringResource(R.string.audio_quick_title),
                    subtitle = audioMenuSummary(),
                    onClick = onShowAudio
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Shuffle,
                    title = stringResource(R.string.shuffle_mode_title),
                    subtitle = stringResource(shuffleModeTitle(ShufflePreferences.mode)),
                    onClick = onShowShuffleModes
                )
                PlayerMenuRow(
                    icon = Icons.Filled.Bedtime,
                    title = stringResource(R.string.player_menu_sleep_timer),
                    subtitle = sleepTimerStatus(),
                    onClick = onShowSleepTimer
                )
            }
        }
    }
}

@Composable
private fun sleepTimerStatus(): String {
    val endsAt = PlayerController.sleepTimerEndsAt
    if (PlayerController.sleepAtEndOfTrack) return stringResource(R.string.sleep_timer_status_end_of_song)
    if (endsAt == null) return stringResource(R.string.sleep_timer_off)
    var remaining by remember(endsAt) { mutableLongStateOf(PlayerController.sleepRemainingMs() ?: 0L) }
    LaunchedEffect(endsAt) {
        while (true) {
            remaining = PlayerController.sleepRemainingMs() ?: 0L
            delay(1_000L)
        }
    }
    return stringResource(R.string.sleep_timer_status_remaining, formatClock(remaining))
}

@Composable
private fun PlayerMenuRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlayerDialogFrame(
    visible: Boolean,
    icon: ImageVector,
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
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
                    KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
                }
                Text(
                    text = title,
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
            content()
        }
    }
}

@Composable
internal fun SongInfoDialog(visible: Boolean, track: Track, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val unknownArtist = stringResource(R.string.player_unknown_artist)
    val unknownAlbum = stringResource(R.string.player_unknown_album)
    val rows = listOf(
        stringResource(R.string.song_info_title) to track.title,
        stringResource(R.string.song_info_artist) to track.artist.ifBlank { unknownArtist },
        stringResource(R.string.song_info_album) to track.album.ifBlank { unknownAlbum },
        stringResource(R.string.song_info_album_artist) to track.albumArtist,
        stringResource(R.string.song_info_genre) to track.genre,
        stringResource(R.string.song_info_year) to (track.year?.toString() ?: ""),
        stringResource(R.string.song_info_track) to (track.trackNumber?.toString() ?: ""),
        stringResource(R.string.song_info_disc) to (track.discNumber?.toString() ?: ""),
        stringResource(R.string.song_info_duration) to formatClock(track.durationMs),
        stringResource(R.string.song_info_format) to track.format,
        stringResource(R.string.song_info_bit_depth) to (track.bitDepth?.let { stringResource(R.string.song_info_bit_depth_value, it) } ?: ""),
        stringResource(R.string.song_info_sample_rate) to (track.sampleRate?.let { stringResource(R.string.song_info_sample_rate_value, it / 1000f) } ?: ""),
        stringResource(R.string.song_info_size) to (if (track.sizeBytes > 0) Formatter.formatShortFileSize(context, track.sizeBytes) else ""),
        stringResource(R.string.song_info_path) to track.path.ifBlank { track.filename }
    ).filter { it.second.isNotBlank() }
    PlayerDialogFrame(
        visible = visible,
        icon = Icons.Filled.Info,
        title = stringResource(R.string.player_menu_song_info),
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            rows.forEach { (label, value) ->
                SongInfoRow(label = label, value = value)
            }
        }
    }
}

@Composable
private fun SongInfoRow(label: String, value: String) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
internal fun SleepTimerDialog(visible: Boolean, onDismiss: () -> Unit) {
    val endsAt = PlayerController.sleepTimerEndsAt
    val endOfTrack = PlayerController.sleepAtEndOfTrack
    PlayerDialogFrame(
        visible = visible,
        icon = Icons.Filled.Bedtime,
        title = stringResource(R.string.player_menu_sleep_timer),
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            SleepOptionRow(
                label = stringResource(R.string.sleep_timer_off),
                selected = endsAt == null && !endOfTrack,
                onClick = {
                    PlayerController.cancelSleepTimer()
                    onDismiss()
                }
            )
            SleepTimerMinutes.forEach { minutes ->
                SleepOptionRow(
                    label = pluralStringResource(R.plurals.sleep_timer_minutes, minutes, minutes),
                    selected = false,
                    onClick = {
                        PlayerController.startSleepTimer(minutes)
                        onDismiss()
                    }
                )
            }
            SleepOptionRow(
                label = stringResource(R.string.sleep_timer_end_of_song),
                selected = endOfTrack,
                onClick = {
                    PlayerController.sleepAfterCurrentTrack()
                    onDismiss()
                }
            )
            if (endsAt != null) {
                Text(
                    text = sleepTimerStatus(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalClintColors.current.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun ShuffleModeDialog(visible: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val colors = LocalClintColors.current
    val current = ShufflePreferences.mode
    val reshuffledMessage = stringResource(R.string.shuffle_reshuffled)
    LaunchedEffect(visible) {
        if (visible) ShufflePreferences.ensureLoaded(context)
    }
    PlayerDialogFrame(
        visible = visible,
        icon = Icons.Filled.Shuffle,
        title = stringResource(R.string.shuffle_mode_title),
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            ShuffleMode.entries.forEach { mode ->
                ShuffleModeOptionRow(
                    icon = shuffleModeIcon(mode),
                    title = stringResource(shuffleModeTitle(mode)),
                    description = stringResource(shuffleModeDescription(mode)),
                    selected = mode == current,
                    onClick = {
                        haptics.toggle(true)
                        ShufflePreferences.setMode(context, mode)
                        if (!PlayerController.shuffleEnabled) PlayerController.toggleShuffle()
                        onDismiss()
                    }
                )
            }
            if (PlayerController.canReshuffleUpcoming()) {
                RowDivider(color = colors.divider.copy(alpha = 0.6f))
                ShuffleModeOptionRow(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    title = stringResource(R.string.shuffle_reshuffle_upcoming),
                    description = stringResource(R.string.shuffle_reshuffle_upcoming_description),
                    selected = false,
                    onClick = {
                        if (PlayerController.reshuffleUpcoming(context)) {
                            haptics.confirm()
                            Toast.makeText(context, reshuffledMessage, Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SleepOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val rowBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "sleep_option_background"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface
        )
    }
}

private fun formatClock(ms: Long): String {
    val totalSeconds = (ms + 999) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

private fun visualizerStyleLabel(style: String): Int = when (style) {
    VisualizerPreferences.STYLE_BARS -> R.string.visualizer_style_bars
    VisualizerPreferences.STYLE_MIRROR -> R.string.visualizer_style_mirror
    VisualizerPreferences.STYLE_LINE -> R.string.visualizer_style_line
    VisualizerPreferences.STYLE_WAVE -> R.string.visualizer_style_wave
    VisualizerPreferences.STYLE_RING -> R.string.visualizer_style_ring
    VisualizerPreferences.STYLE_PULSE -> R.string.visualizer_style_pulse
    else -> R.string.visualizer_style_off
}
