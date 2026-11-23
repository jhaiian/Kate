package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibrarySortOrder
import com.jhaiian.kate.data.SortDirection
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LocalClintColors

private val SortOptionLabels = mapOf(
    LibrarySortOrder.TITLE to R.string.sort_by_title,
    LibrarySortOrder.TRACK_NUMBER to R.string.sort_by_track_number,
    LibrarySortOrder.DISC_TRACK to R.string.sort_by_disc_track,
    LibrarySortOrder.FILENAME to R.string.sort_by_filename,
    LibrarySortOrder.PATH to R.string.sort_by_path,
    LibrarySortOrder.ARTIST to R.string.sort_by_artist,
    LibrarySortOrder.ALBUM to R.string.sort_by_album,
    LibrarySortOrder.YEAR to R.string.sort_by_year,
    LibrarySortOrder.YEAR_ALBUM to R.string.sort_by_year_album,
    LibrarySortOrder.DATE_ADDED to R.string.sort_by_date_added,
    LibrarySortOrder.QUEUE to R.string.sort_by_queue
)

@Composable
fun SortOptionsDialog(
    visible: Boolean,
    currentOrder: LibrarySortOrder,
    currentDirection: SortDirection,
    onDismiss: () -> Unit,
    onSelect: (LibrarySortOrder) -> Unit,
    onSelectDirection: (SortDirection) -> Unit
) {
    val colors = LocalClintColors.current
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
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = kateTileTint(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.sort_options_title),
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
            SortDirectionToggle(
                currentDirection = currentDirection,
                onSelectDirection = onSelectDirection,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                items(LibrarySortOrder.entries, key = { it.key }) { option ->
                    SortOptionRow(
                        label = stringResource(SortOptionLabels.getValue(option)),
                        selected = option == currentOrder,
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortDirectionToggle(
    currentDirection: SortDirection,
    onSelectDirection: (SortDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.background)
            .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        SortDirectionOption(
            label = stringResource(R.string.sort_direction_ascending),
            icon = Icons.Outlined.ArrowUpward,
            selected = currentDirection == SortDirection.ASCENDING,
            onClick = { onSelectDirection(SortDirection.ASCENDING) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        SortDirectionOption(
            label = stringResource(R.string.sort_direction_descending),
            icon = Icons.Outlined.ArrowDownward,
            selected = currentDirection == SortDirection.DESCENDING,
            onClick = { onSelectDirection(SortDirection.DESCENDING) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SortDirectionOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val optionBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "sort_direction_background"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(optionBackground)
            .clickable { onClick() }
            .padding(vertical = 9.dp)
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.secondaryText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface
        )
    }
}

@Composable
private fun SortOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val rowBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "option_row_background"
    )
    val indicatorFill by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 220),
        label = "option_indicator_fill"
    )
    val indicatorBorder by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.divider,
        animationSpec = tween(durationMillis = 220),
        label = "option_indicator_border"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurface,
        animationSpec = tween(durationMillis = 220),
        label = "option_label_color"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(indicatorFill)
                .border(
                    BorderStroke(1.6.dp, indicatorBorder),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(durationMillis = 120)),
                exit = scaleOut(tween(durationMillis = 140)) + fadeOut(tween(durationMillis = 140))
            ) {
                KateIcon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
    }
}

