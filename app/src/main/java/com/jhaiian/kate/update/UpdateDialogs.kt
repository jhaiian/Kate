package com.jhaiian.kate.update

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.MarkdownText
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
fun UpdateFlowHost() {
    if (!UpdateChecker.supported) return
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val step = UpdateChecker.step
    var shown by remember { mutableStateOf(step) }
    if (step != UpdateStep.None) shown = step
    val visible = step != UpdateStep.None
    val onDismiss: () -> Unit = {
        if (step == UpdateStep.Downloading) UpdateChecker.cancelDownload() else UpdateChecker.dismiss()
    }

    KateDialog(
        visible = visible,
        onDismiss = onDismiss,
        cancelable = shown !is UpdateStep.Available && shown != UpdateStep.Downloading
    ) {
        when (val current = shown) {
            UpdateStep.Checking -> UpdateDialogFrame(title = stringResource(R.string.update_checking_title)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    CircularProgressIndicator(color = LocalClintColors.current.primary, strokeWidth = 3.dp, modifier = Modifier.size(22.dp))
                    Text(
                        text = stringResource(R.string.update_checking_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalClintColors.current.secondaryText,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
                Actions { KateTextButton(stringResource(R.string.action_cancel), UpdateChecker::dismiss) }
            }
            UpdateStep.UpToDate -> MessageDialog(stringResource(R.string.update_up_to_date_title), stringResource(R.string.update_up_to_date_message))
            UpdateStep.CheckFailed -> MessageDialog(stringResource(R.string.update_check_failed_title), stringResource(R.string.update_check_failed_message))
            UpdateStep.DownloadFailed -> MessageDialog(stringResource(R.string.update_download_failed_title), stringResource(R.string.update_download_failed_message))
            is UpdateStep.Available -> {
                val channel = if (current.isBeta) stringResource(R.string.update_beta_suffix) else ""
                UpdateDialogFrame(title = stringResource(R.string.update_available_title, current.version, channel)) {
                    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                    Column(
                        modifier = Modifier
                            .heightIn(max = screenHeight * 0.5f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        MarkdownText(current.changelog)
                    }
                    Actions {
                        KateTextButton(stringResource(R.string.update_skip_version), { UpdateChecker.skip(context, current.versionCode) })
                        KateTextButton(stringResource(R.string.update_later), UpdateChecker::dismiss)
                        if (current.downloadUrl != null) {
                            KateTextButton(
                                label = stringResource(R.string.update_download),
                                role = DialogButtonRole.MAIN,
                                onClick = { UpdateChecker.download(context, current) }
                            )
                        } else {
                            KateTextButton(
                                label = stringResource(R.string.update_view_github),
                                role = DialogButtonRole.MAIN,
                                onClick = {
                                    UpdateChecker.dismiss()
                                    runCatching { uriHandler.openUri(UpdateChecker.RELEASES_URL) }
                                }
                            )
                        }
                    }
                }
            }
            UpdateStep.Downloading -> UpdateDialogFrame(title = stringResource(R.string.update_downloading_title)) {
                val colors = LocalClintColors.current
                val total = UpdateChecker.totalBytes
                val done = Formatter.formatShortFileSize(context, UpdateChecker.downloadedBytes)
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    if (total > 0) {
                        LinearProgressIndicator(
                            progress = { UpdateChecker.progress },
                            color = colors.primary,
                            trackColor = colors.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(color = colors.primary, trackColor = colors.surfaceVariant, modifier = Modifier.fillMaxWidth())
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        Text(
                            text = if (total > 0) {
                                stringResource(
                                    R.string.update_download_size_known,
                                    done,
                                    Formatter.formatShortFileSize(context, total),
                                    (UpdateChecker.progress * 100).toInt()
                                )
                            } else {
                                stringResource(R.string.update_download_size_unknown, done)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText
                        )
                        if (UpdateChecker.bytesPerSecond > 0) {
                            Text(
                                text = stringResource(R.string.update_download_speed, Formatter.formatShortFileSize(context, UpdateChecker.bytesPerSecond)),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.secondaryText
                            )
                        }
                    }
                }
                Actions { KateTextButton(stringResource(R.string.action_cancel), UpdateChecker::cancelDownload) }
            }
            UpdateStep.None -> Unit
        }
    }
}

@Composable
private fun UpdateDialogFrame(title: String, content: @Composable () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 480.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun Actions(content: @Composable () -> Unit) {
    KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp), content = content)
}

@Composable
private fun MessageDialog(title: String, message: String) {
    UpdateDialogFrame(title = title) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalClintColors.current.secondaryText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Actions {
            KateTextButton(label = stringResource(R.string.update_ok), role = DialogButtonRole.MAIN, onClick = UpdateChecker::dismiss)
        }
    }
}
