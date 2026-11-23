package com.jhaiian.kate.ui.settings

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay

object SettingsSpotlight {
    private const val LIFETIME_MS = 4000L
    private var route: String? = null
    private var title: String? = null
    private var requestedAt = 0L

    var token by mutableIntStateOf(0)
        private set

    internal var centering = false
    internal var bottomInset = 0f

    fun request(route: String, title: String) {
        this.route = route
        this.title = title
        requestedAt = SystemClock.uptimeMillis()
        token++
    }

    internal fun claim(page: String?, rowTitle: String): Boolean {
        val wanted = title ?: return false
        if (SystemClock.uptimeMillis() - requestedAt > LIFETIME_MS) {
            route = null
            title = null
            return false
        }
        if (wanted != rowTitle || page != route) return false
        route = null
        title = null
        return true
    }
}

val LocalSettingsPage = staticCompositionLocalOf<String?> { null }

@OptIn(ExperimentalFoundationApi::class)
private class SpotlightBringIntoViewSpec(private val fallback: BringIntoViewSpec) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        if (!SettingsSpotlight.centering) return fallback.calculateScrollDistance(offset, size, containerSize)
        val visible = (containerSize - SettingsSpotlight.bottomInset).coerceAtLeast(size)
        return offset + size / 2f - visible / 2f
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSpotlightBringIntoViewSpec(): BringIntoViewSpec {
    val fallback = LocalBringIntoViewSpec.current
    return remember(fallback) { SpotlightBringIntoViewSpec(fallback) }
}

@Composable
private fun currentSettingsPage(): String? =
    LocalSettingsPage.current ?: (LocalViewModelStoreOwner.current as? NavBackStackEntry)?.destination?.route

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Modifier.settingsSpotlight(title: String): Modifier {
    val page = currentSettingsPage()
    val colors = LocalClintColors.current
    val density = LocalDensity.current
    val requester = remember { BringIntoViewRequester() }
    val glow = remember { Animatable(0f) }
    val bottomInset = with(density) { LocalBottomContentPadding.current.toPx() }
    LaunchedEffect(page, title) {
        snapshotFlow { SettingsSpotlight.token }.collect {
            if (SettingsSpotlight.claim(page, title)) {
                delay(360)
                SettingsSpotlight.bottomInset = bottomInset
                SettingsSpotlight.centering = true
                try {
                    requester.bringIntoView()
                } finally {
                    SettingsSpotlight.centering = false
                }
                delay(120)
                repeat(3) {
                    glow.animateTo(1f, tween(durationMillis = 240, easing = FastOutSlowInEasing))
                    glow.animateTo(0f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
                }
            }
        }
    }
    return this
        .bringIntoViewRequester(requester)
        .drawBehind {
            val amount = glow.value
            if (amount > 0f) {
                val inset = 4.dp.toPx()
                drawRoundRect(
                    color = colors.primary.copy(alpha = 0.22f * amount),
                    topLeft = Offset(-inset, -inset),
                    size = Size(this.size.width + inset * 2, this.size.height + inset * 2),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
            }
        }
}
