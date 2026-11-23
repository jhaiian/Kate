package com.jhaiian.kate.ui.components

import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.jhaiian.kate.ui.theme.LocalClintColors
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    val textArgb = colors.onSurface.toArgb()
    val linkArgb = colors.primary.toArgb()
    val fontSize = MaterialTheme.typography.bodyMedium.fontSize.takeIf { it != TextUnit.Unspecified }?.value ?: 14f
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                setLineSpacing(0f, 1.3f)
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { tv ->
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            tv.setTextColor(textArgb)
            tv.setLinkTextColor(linkArgb)
            markwon.setMarkdown(tv, markdown)
        }
    )
}
