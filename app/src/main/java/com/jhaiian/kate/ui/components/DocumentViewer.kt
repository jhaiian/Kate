package com.jhaiian.kate.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.KateHttp
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

object DocumentViewer {
    private const val BASE_URL = "https://raw.githubusercontent.com/jhaiian/Kate/main/"
    const val CHANGELOG_URL = BASE_URL + "CHANGELOG.md"
    const val ATTRIBUTION_URL = BASE_URL + "Attribution.md"
    const val SUPPORTERS_URL = BASE_URL + "Supporters.md"

    class Document(@StringRes val title: Int) {
        var markdown by mutableStateOf<String?>(null)
        var failed by mutableStateOf(false)
    }

    var current by mutableStateOf<Document?>(null)
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client get() = KateHttp.client
    private var job: Job? = null

    fun show(@StringRes title: Int, url: String) {
        job?.cancel()
        val document = Document(title)
        current = document
        job = scope.launch {
            val text = withContext(Dispatchers.IO) { runCatching { fetch(url) }.getOrNull() }
            if (text != null) document.markdown = text else document.failed = true
        }
    }

    fun dismiss() {
        job?.cancel()
        job = null
        current = null
    }

    private fun fetch(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", "Kate").build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            response.body.string()
        }
    }
}

@Composable
fun DocumentViewerHost() {
    val document = DocumentViewer.current
    var shown by remember { mutableStateOf(document) }
    if (document != null) shown = document

    KateDialog(visible = document != null, onDismiss = DocumentViewer::dismiss) {
        val current = shown ?: return@KateDialog
        val colors = LocalClintColors.current
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(current.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            val markdown = current.markdown
            when {
                current.failed -> Text(
                    text = stringResource(R.string.document_viewer_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp)
                )
                markdown == null -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        trackColor = colors.surfaceVariant,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> Column(
                    modifier = Modifier
                        .heightIn(max = screenHeight * 0.65f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    MarkdownText(markdown)
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.document_viewer_close),
                    role = DialogButtonRole.MAIN,
                    onClick = DocumentViewer::dismiss
                )
            }
        }
    }
}
