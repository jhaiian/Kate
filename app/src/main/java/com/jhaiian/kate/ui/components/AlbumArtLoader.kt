package com.jhaiian.kate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.jhaiian.kate.data.ArtistImage
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.Track

@Composable
fun AlbumArt(
    track: Track,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    allowHardware: Boolean = true,
    placeholder: @Composable BoxScope.() -> Unit
) {
    val artKey = track.artKey
    if (artKey == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center, content = placeholder)
        return
    }
    val context = LocalContext.current
    val request = remember(artKey, allowHardware) {
        ImageRequest.Builder(context)
            .data(LibraryTrackCache.artFile(context, artKey))
            .memoryCacheKey(if (allowHardware) artKey else "$artKey:software")
            .allowHardware(allowHardware)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request, contentScale = contentScale)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
        val state by painter.state.collectAsState()
        when (state) {
            is AsyncImagePainter.State.Loading -> LoadingShimmer()
            is AsyncImagePainter.State.Error -> placeholder()
            else -> Unit
        }
    }
}

@Composable
fun ArtistArt(
    image: ArtistImage?,
    fallback: Track?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    allowHardware: Boolean = true,
    placeholder: @Composable BoxScope.() -> Unit
) {
    if (image == null) {
        if (fallback != null) {
            AlbumArt(track = fallback, modifier = modifier, contentScale = contentScale, allowHardware = allowHardware, placeholder = placeholder)
        } else {
            Box(modifier = modifier, contentAlignment = Alignment.Center, content = placeholder)
        }
        return
    }
    val context = LocalContext.current
    val request = remember(image, allowHardware) {
        ImageRequest.Builder(context)
            .data(image.file)
            .memoryCacheKey("artist:${image.file.name}:${image.version}" + if (allowHardware) "" else ":software")
            .allowHardware(allowHardware)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request, contentScale = contentScale)
    val state by painter.state.collectAsState()
    if (state is AsyncImagePainter.State.Error) {
        if (fallback != null) {
            AlbumArt(track = fallback, modifier = modifier, contentScale = contentScale, allowHardware = allowHardware, placeholder = placeholder)
        } else {
            Box(modifier = modifier, contentAlignment = Alignment.Center, content = placeholder)
        }
        return
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
        if (state is AsyncImagePainter.State.Loading) {
            LoadingShimmer()
        }
    }
}
