package com.jhaiian.kate.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsIn(key: String, animatedScope: AnimatedVisibilityScope?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    if (sharedScope == null || animatedScope == null) return this
    return with(sharedScope) {
        this@sharedBoundsIn.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WithSharedTransitionScope(content: @Composable SharedTransitionScope.() -> Unit) {
    val sharedScope = LocalSharedTransitionScope.current
    if (sharedScope != null) {
        sharedScope.content()
    } else {
        SharedTransitionLayout { content() }
    }
}
