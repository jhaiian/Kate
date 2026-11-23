package com.jhaiian.kate.ui.components

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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.playback.ShuffleMode
import com.jhaiian.kate.playback.ShufflePreferences
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LocalClintColors

object ShufflePickRequest {
    var tracks by mutableStateOf<List<Track>?>(null)
        private set

    fun open(tracks: List<Track>) {
        if (tracks.isNotEmpty()) this.tracks = tracks
    }

    fun close() {
        tracks = null
    }
}

internal fun shuffleModeTitle(mode: ShuffleMode): Int = when (mode) {
    ShuffleMode.RANDOM -> R.string.shuffle_mode_random
    ShuffleMode.SPREAD_ARTISTS -> R.string.shuffle_mode_spread_artists
    ShuffleMode.LESS_REPEATS -> R.string.shuffle_mode_less_repeats
    ShuffleMode.DISCOVERY -> R.string.shuffle_mode_discovery
    ShuffleMode.ALBUMS -> R.string.shuffle_mode_albums
    ShuffleMode.ALBUMS_MIXED -> R.string.shuffle_mode_albums_mixed
}

internal fun shuffleModeDescription(mode: ShuffleMode): Int = when (mode) {
    ShuffleMode.RANDOM -> R.string.shuffle_mode_random_description
    ShuffleMode.SPREAD_ARTISTS -> R.string.shuffle_mode_spread_artists_description
    ShuffleMode.LESS_REPEATS -> R.string.shuffle_mode_less_repeats_description
    ShuffleMode.DISCOVERY -> R.string.shuffle_mode_discovery_description
    ShuffleMode.ALBUMS -> R.string.shuffle_mode_albums_description
    ShuffleMode.ALBUMS_MIXED -> R.string.shuffle_mode_albums_mixed_description
}

internal fun shuffleModeIcon(mode: ShuffleMode): ImageVector = when (mode) {
    ShuffleMode.RANDOM -> Icons.Filled.Shuffle
    ShuffleMode.SPREAD_ARTISTS -> Icons.Filled.Groups
    ShuffleMode.LESS_REPEATS -> Icons.Filled.History
    ShuffleMode.DISCOVERY -> Icons.Filled.Explore
    ShuffleMode.ALBUMS -> Icons.Filled.Album
    ShuffleMode.ALBUMS_MIXED -> Icons.Filled.LibraryMusic
}

@Composable
internal fun ShuffleModeOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val rowBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "shuffle_mode_background"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.secondaryText,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) colors.primary else colors.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText
            )
        }
        if (selected) {
            KateIcon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.padding(start = 8.dp).size(18.dp)
            )
        }
    }
}

@Composable
fun LibraryShuffleModeHost() {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val colors = LocalClintColors.current
    val pending = ShufflePickRequest.tracks
    val retained = remember { arrayOfNulls<List<Track>>(1) }
    if (pending != null) retained[0] = pending
    val tracks = pending ?: retained[0].orEmpty()
    val current = ShufflePreferences.mode
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    if (pending != null) ShufflePreferences.ensureLoaded(context)
    KateDialog(visible = pending != null, onDismiss = { ShufflePickRequest.close() }) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = screenHeight * 0.85f)
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
                    KateIcon(imageVector = Icons.Filled.Shuffle, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.shuffle_pick_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pluralStringResource(R.plurals.library_song_count, tracks.size, tracks.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1
                    )
                }
                KateIconButton(onClick = { ShufflePickRequest.close() }, modifier = Modifier.size(30.dp)) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            Column(
                modifier = Modifier
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
                            val chosen = ShufflePickRequest.tracks
                            haptics.confirm()
                            ShufflePreferences.setMode(context, mode)
                            ShufflePickRequest.close()
                            if (chosen != null) PlayerController.playShuffled(context, chosen)
                        }
                    )
                }
            }
        }
    }
}
