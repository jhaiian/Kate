package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.tags.TagEditorRequest
import androidx.compose.material.icons.filled.Sell
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors

private const val SelectionTintAlpha = 0.2f
private val SelectionStroke = 2.dp
private val SelectionBarCorner = 24.dp

internal fun Set<String>?.toggled(id: String): Set<String>? {
    val current = this ?: emptySet()
    val next = if (id in current) current - id else current + id
    return next.ifEmpty { null }
}

internal fun Modifier.selectionHighlight(selected: Boolean, color: Color, corner: Dp): Modifier =
    if (!selected) {
        this
    } else {
        drawWithContent {
            drawContent()
            val radius = CornerRadius(corner.toPx())
            drawRoundRect(color.copy(alpha = SelectionTintAlpha), cornerRadius = radius)
            drawRoundRect(color, cornerRadius = radius, style = Stroke(width = SelectionStroke.toPx()))
        }
    }

@Composable
internal fun BoxScope.SelectionOverlay(
    selectedCount: Int?,
    resolveTracks: () -> List<Track>,
    onSelectAll: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val resources = context.resources
    val bottomInset = LocalBottomContentPadding.current
    var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
    var lastCount by remember { mutableIntStateOf(0) }
    SideEffect { if (selectedCount != null) lastCount = selectedCount }
    val shownCount = selectedCount ?: lastCount

    BackHandler(enabled = selectedCount != null, onBack = onClose)

    AnimatedVisibility(
        visible = selectedCount != null,
        enter = fadeIn(tween(durationMillis = 160)) + slideInVertically(
            spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
        ) { it },
        exit = fadeOut(tween(durationMillis = 140)) + slideOutVertically(tween(durationMillis = 180)) { it },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 12.dp, end = 12.dp, bottom = bottomInset + 8.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(SelectionBarCorner))
                .clip(RoundedCornerShape(SelectionBarCorner))
                .background(colors.cardBackground)
                .padding(vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                KateIconButton(onClick = onClose) {
                    KateIcon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.selection_close),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = pluralStringResource(R.plurals.selection_count, shownCount, shownCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                KateIconButton(onClick = {
                    haptics.click()
                    onSelectAll()
                }) {
                    KateIcon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = stringResource(R.string.selection_select_all),
                        tint = colors.primary
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                SelectionAction(
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    label = stringResource(R.string.selection_play_next),
                    modifier = Modifier.weight(1f)
                ) {
                    val tracks = resolveTracks()
                    PlayerController.playNext(context, tracks)
                    haptics.confirm()
                    Toast.makeText(
                        context,
                        resources.getQuantityString(R.plurals.selection_play_next_done, tracks.size, tracks.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    onClose()
                }
                SelectionAction(
                    icon = Icons.Filled.AddToQueue,
                    label = stringResource(R.string.selection_add_to_queue),
                    modifier = Modifier.weight(1f)
                ) {
                    val tracks = resolveTracks()
                    PlayerController.addToQueue(context, tracks)
                    haptics.confirm()
                    Toast.makeText(
                        context,
                        resources.getQuantityString(R.plurals.selection_add_to_queue_done, tracks.size, tracks.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    onClose()
                }
                SelectionAction(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = stringResource(R.string.playlist_add_to),
                    modifier = Modifier.weight(1f)
                ) {
                    addToPlaylistIds = resolveTracks().map { it.id }.distinct()
                    onClose()
                }
                SelectionAction(
                    icon = Icons.Filled.Sell,
                    label = stringResource(R.string.tag_editor_title),
                    modifier = Modifier.weight(1f)
                ) {
                    haptics.click()
                    TagEditorRequest.open(resolveTracks())
                    onClose()
                }
            }
        }
    }
    AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
}

@Composable
private fun SelectionAction(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(22.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
