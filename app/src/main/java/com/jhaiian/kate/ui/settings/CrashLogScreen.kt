package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.crash.CrashHandler
import com.jhaiian.kate.crash.CrashReportItem
import com.jhaiian.kate.crash.CrashUiState
import com.jhaiian.kate.crash.MAX_CRASH_CLIP_CHARS
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.theme.ClintColors
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

private const val CRASH_FILENAME_DATE_FORMAT = "yyyyMMdd_HHmmss"
private const val CRASH_DISPLAY_DATE_FORMAT = "MMM d, yyyy  HH:mm:ss"

private fun readCrashReportsFromDisk(context: Context): List<CrashReportItem> {
    val appContext = context.applicationContext
    val fileDateFmt = SimpleDateFormat(CRASH_FILENAME_DATE_FORMAT, Locale.US)
    val displayFmt = SimpleDateFormat(CRASH_DISPLAY_DATE_FORMAT, Locale.US)
    CrashHandler.deleteOldReports(appContext)
    return CrashHandler.getCrashFiles(appContext).map { file ->
        val nameWithoutExt = file.nameWithoutExtension.removePrefix("crash_")
        val date = runCatching { fileDateFmt.parse(nameWithoutExt) }.getOrNull()
        val title = date?.let { displayFmt.format(it) } ?: file.name
        CrashReportItem(file, title, file.readText())
    }
}

@Composable
fun CrashLogScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = remember { CrashUiState() }
    val lastDetail = remember { LastCrashReport() }
    val bottomInset = LocalBottomContentPadding.current

    fun copyToClipboard(content: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val truncated = content.length > MAX_CRASH_CLIP_CHARS
        val clipped = if (truncated) content.take(MAX_CRASH_CLIP_CHARS) + "\n${context.getString(R.string.crash_log_truncated)}" else content
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Kate Crash Report", clipped))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val msg = if (truncated) context.getString(R.string.crash_copied_truncated) else context.getString(R.string.crash_copied)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        state.isLoading = true
        val items = withContext(Dispatchers.IO) { readCrashReportsFromDisk(context) }
        state.reports.clear()
        state.reports.addAll(items)
        state.isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = buttonTint(colors.onSurface))
                }
                Text(
                    text = stringResource(R.string.crash_log_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KateTextButton(
                    label = stringResource(R.string.crash_clear_all),
                    role = DialogButtonRole.LINK,
                    enabled = state.reports.isNotEmpty(),
                    onClick = { state.clearAllConfirmOpen = true }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 20.dp)) {
                        when {
                            state.isLoading -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = colors.primary)
                                }
                            }
                            state.reports.isEmpty() -> {
                                Text(
                                    text = stringResource(R.string.crash_no_reports),
                                    color = colors.secondaryText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                                )
                            }
                            else -> {
                                state.reports.forEach { item ->
                                    CrashLogCard(
                                        item = item,
                                        colors = colors,
                                        onOpen = { state.detailReport = item },
                                        onCopy = { copyToClipboard(item.content) },
                                        onDelete = {
                                            state.detailReport = null
                                            scope.launch {
                                                withContext(Dispatchers.IO) { item.file.delete() }
                                                state.reports.remove(item)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        ClearAllCrashLogsDialog(
            visible = state.clearAllConfirmOpen,
            onConfirm = {
                state.clearAllConfirmOpen = false
                scope.launch {
                    withContext(Dispatchers.IO) { CrashHandler.clearAllReports(context.applicationContext) }
                    state.reports.clear()
                }
            },
            onDismiss = { state.clearAllConfirmOpen = false }
        )

        state.detailReport?.let { lastDetail.item = it }
        (state.detailReport ?: lastDetail.item)?.let { item ->
            CrashLogDetailDialog(
                visible = state.detailReport != null,
                item = item,
                onCopy = {
                    copyToClipboard(item.content)
                    state.detailReport = null
                },
                onDelete = {
                    scope.launch {
                        withContext(Dispatchers.IO) { item.file.delete() }
                        state.reports.remove(item)
                    }
                    state.detailReport = null
                },
                onDismiss = { state.detailReport = null }
            )
        }
    }
}

private class LastCrashReport {
    var item: CrashReportItem? = null
}

@Composable
private fun CrashLogCard(
    item: CrashReportItem,
    colors: ClintColors,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(color = colors.cardBackground, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KateIcon(Icons.Filled.BugReport, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.title,
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            KateIconButton(onClick = onCopy) {
                KateIcon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.crash_copy), tint = colors.primary)
            }
            KateIconButton(onClick = onDelete) {
                KateIcon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete), tint = buttonTint(colors.secondaryText))
            }
        }
    }
}

@Composable
private fun CrashLogDialogSurface(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    KateIcon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            content()
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            footer()
        }
    }
}

@Composable
private fun ClearAllCrashLogsDialog(visible: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    CrashLogDialogSurface(
        visible = visible,
        title = stringResource(R.string.crash_clear_title),
        onDismiss = onDismiss,
        footer = {
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.crash_clear_confirm),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onConfirm
                )
            }
        }
    ) {
        Text(
            text = stringResource(R.string.crash_clear_message),
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun CrashLogDetailDialog(
    visible: Boolean,
    item: CrashReportItem,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    val onSurfaceArgb = colors.onSurface.toArgb()
    val displayContent = if (item.content.length > MAX_CRASH_CLIP_CHARS) {
        item.content.take(MAX_CRASH_CLIP_CHARS) + "\n" + stringResource(R.string.crash_log_truncated)
    } else {
        item.content
    }

    CrashLogDialogSurface(
        visible = visible,
        title = item.title,
        onDismiss = onDismiss,
        footer = {
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_delete),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onDelete
                )
                KateTextButton(
                    label = stringResource(R.string.back),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.action_copy),
                    role = DialogButtonRole.MAIN,
                    onClick = onCopy
                )
            }
        }
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
            factory = { ctx ->
                android.widget.ScrollView(ctx).apply {
                    addView(
                        TextView(ctx).apply {
                            setTextColor(onSurfaceArgb)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                            typeface = android.graphics.Typeface.MONOSPACE
                            setTextIsSelectable(true)
                            setPadding(48, 20, 48, 20)
                            text = displayContent
                        }
                    )
                }
            },
            update = { scrollView ->
                val tv = scrollView.getChildAt(0) as TextView
                tv.setTextColor(onSurfaceArgb)
                tv.text = displayContent
            }
        )
    }
}
