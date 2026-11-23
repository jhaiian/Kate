package com.jhaiian.kate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
fun JumpToPlayingButton(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val label = stringResource(R.string.library_jump_to_playing)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClickLabel = label) {
                    haptics.click()
                    onClick()
                }
        ) {
            KateIcon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = label,
                tint = colors.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

fun LazyListState.isItemInView(index: Int, bottomInsetPx: Int = 0, topInsetPx: Int = 0): Boolean {
    if (index < 0) return true
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    val center = item.offset + item.size / 2
    return center > info.viewportStartOffset + topInsetPx && center < info.viewportEndOffset - bottomInsetPx
}

fun LazyGridState.isItemInView(index: Int, bottomInsetPx: Int = 0, topInsetPx: Int = 0): Boolean {
    if (index < 0) return true
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    val center = item.offset.y + item.size.height / 2
    return center > info.viewportStartOffset + topInsetPx && center < info.viewportEndOffset - bottomInsetPx
}
