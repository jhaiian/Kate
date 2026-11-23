package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
internal fun ListeningHistorySection(
    onEnabledChange: (Boolean) -> Unit,
    onClearRequest: () -> Unit
) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
        SectionHeader(text = stringResource(R.string.section_listening), tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            SettingsSwitchRow(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.row_record_history_title),
                subtitle = stringResource(R.string.row_record_history_summary),
                checked = ListeningHistory.enabled,
                colors = colors,
                onCheckedChange = onEnabledChange
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.DeleteSweep,
                title = stringResource(R.string.row_clear_history_title),
                subtitle = stringResource(R.string.row_clear_history_summary),
                colors = colors,
                onClick = onClearRequest
            )
        }
    }
}

@Composable
internal fun ClearHistoryDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.row_clear_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.clear_history_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.clear_history_confirm),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onConfirm
                )
            }
        }
    }
}
