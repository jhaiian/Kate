package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
internal fun ArtistImagesSection(
    onEnableRequest: () -> Unit,
    onDisable: () -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onDeleteDownloaded: () -> Unit
) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
        SectionHeader(text = stringResource(R.string.section_artist_images), tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            SettingsSwitchRow(
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.row_artist_images_download),
                subtitle = stringResource(R.string.row_artist_images_download_summary),
                checked = ArtistImageStore.enabled,
                colors = colors,
                onCheckedChange = { checked -> if (checked) onEnableRequest() else onDisable() }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Wifi,
                title = stringResource(R.string.row_artist_images_wifi_only),
                subtitle = stringResource(R.string.row_artist_images_wifi_only_summary),
                checked = ArtistImageStore.wifiOnly,
                colors = colors,
                onCheckedChange = onWifiOnlyChange
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.DeleteSweep,
                title = stringResource(R.string.row_artist_images_delete),
                subtitle = stringResource(R.string.row_artist_images_delete_summary),
                colors = colors,
                onClick = onDeleteDownloaded
            )
        }
        Text(
            text = stringResource(R.string.artist_images_credit),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp)
        )
    }
}

@Composable
internal fun ArtistImagesConsentDialog(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 10.dp, top = 16.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .kateIconTile(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KateIcon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = kateTileTint(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.artist_images_consent_title),
                    style = MaterialTheme.typography.titleMedium,
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
            Text(
                text = stringResource(R.string.artist_images_consent_message),
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            Text(
                text = stringResource(R.string.artist_images_consent_details),
                color = colors.secondaryText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp)
            )
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.artist_images_consent_confirm),
                    role = DialogButtonRole.MAIN,
                    onClick = onConfirm
                )
            }
        }
    }
}
