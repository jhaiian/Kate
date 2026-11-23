package com.jhaiian.kate.ui.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.jhaiian.kate.ui.theme.BackgroundStore
import com.jhaiian.kate.ui.theme.DisplayPreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LyricsPreferences
import com.jhaiian.kate.ui.theme.kateFontFamily

@Composable
fun CustomImageBackdrop(name: String, blur: Int, dim: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = LocalClintColors.current
    val file = remember(name) { BackgroundStore.file(context, name) } ?: return
    val nativeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val sizePx = if (blur > 0 && !nativeBlur) (720 - blur * 16).coerceAtLeast(48) else 1600
    val request = remember(file, sizePx) {
        ImageRequest.Builder(context)
            .data(file)
            .size(sizePx)
            .memoryCacheKey("${file.name}:$sizePx")
            .build()
    }
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(
                model = request,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.High
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (blur > 0 && nativeBlur) Modifier.blur(blur.dp) else Modifier)
        )
        if (dim > 0) {
            Box(modifier = Modifier.fillMaxSize().background(colors.background.copy(alpha = dim / 100f)))
        }
    }
}

fun TextStyle.withLyricsFont(key: String): TextStyle = when {
    key == LyricsPreferences.FONT_APP -> this
    key == DisplayPreferences.FONT_DEFAULT -> copy(fontFamily = FontFamily.Default)
    else -> kateFontFamily(key)?.let { copy(fontFamily = it) } ?: this
}
