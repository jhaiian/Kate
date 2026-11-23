package com.jhaiian.kate.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.DocumentViewer
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.update.UpdateChecker

@Composable
fun UpdateSettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    UpdateChecker.ensureLoaded(context)
    var showBetaConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        InfoPage(title = stringResource(R.string.update_settings_title), onBack = onBack) {
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(top = 8.dp)) {
                    SectionHeader(stringResource(R.string.update_section_updates), colors.primary)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Refresh,
                            title = stringResource(R.string.update_check_on_launch_title),
                            subtitle = stringResource(R.string.update_check_on_launch_summary),
                            checked = UpdateChecker.checkOnLaunch,
                            colors = colors,
                            onCheckedChange = { UpdateChecker.setCheckOnLaunch(context, it) }
                        )
                        RowDivider(colors.divider)
                        SettingsSwitchRow(
                            icon = Icons.Outlined.NetworkCell,
                            title = stringResource(R.string.update_skip_metered_title),
                            subtitle = stringResource(R.string.update_skip_metered_summary),
                            checked = UpdateChecker.skipOnMetered,
                            colors = colors,
                            onCheckedChange = { UpdateChecker.setSkipOnMetered(context, it) },
                            enabled = UpdateChecker.checkOnLaunch
                        )
                        RowDivider(colors.divider)
                        SettingsRow(
                            icon = Icons.Outlined.SystemUpdate,
                            title = stringResource(R.string.update_check_now_title),
                            subtitle = stringResource(R.string.update_check_now_summary),
                            colors = colors,
                            onClick = { UpdateChecker.check(context, silent = false) }
                        )
                        RowDivider(colors.divider)
                        SettingsRow(
                            icon = Icons.Outlined.History,
                            title = stringResource(R.string.update_changelog_title),
                            subtitle = stringResource(R.string.update_changelog_summary),
                            colors = colors,
                            onClick = { DocumentViewer.show(R.string.update_changelog_title, DocumentViewer.CHANGELOG_URL) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(stringResource(R.string.update_section_channel), colors.primary)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Science,
                            title = stringResource(R.string.update_beta_title),
                            subtitle = stringResource(R.string.update_beta_summary),
                            checked = UpdateChecker.betaChannel,
                            colors = colors,
                            onCheckedChange = { on ->
                                if (on) showBetaConfirm = true else UpdateChecker.setBetaChannel(context, false)
                            }
                        )
                    }
                }
            }
        }
        KateDialog(visible = showBetaConfirm, onDismiss = { showBetaConfirm = false }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 480.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardBackground)
                    .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
            ) {
                Text(
                    text = stringResource(R.string.update_beta_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.update_beta_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                    KateTextButton(
                        label = stringResource(R.string.action_cancel),
                        role = DialogButtonRole.SECONDARY,
                        onClick = { showBetaConfirm = false }
                    )
                    KateTextButton(
                        label = stringResource(R.string.update_beta_confirm),
                        role = DialogButtonRole.MAIN,
                        onClick = {
                            showBetaConfirm = false
                            UpdateChecker.setBetaChannel(context, true)
                        }
                    )
                }
            }
        }
    }
}
