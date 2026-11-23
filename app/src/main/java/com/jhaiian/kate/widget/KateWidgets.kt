package com.jhaiian.kate.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.glance.color.ColorProvider as DayNightColorProvider

private val Strip = DpSize(110.dp, 48.dp)
private val Bar = DpSize(250.dp, 48.dp)
private val Card = DpSize(250.dp, 110.dp)
private val Big = DpSize(250.dp, 200.dp)
private val SurfaceRadius = 20.dp
private const val MAX_ART_PX = 420
private const val MAX_COVER_PX = 720
private const val BITMAP_DENSITY_CAP = 1.75f

private fun DayNight.provider(): ColorProvider = DayNightColorProvider(day = day, night = night)

private val modernCorners: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun Context.bitmapDensity(): Float = min(resources.displayMetrics.density, BITMAP_DENSITY_CAP)

private fun prepare(context: Context) {
    WidgetState.ensureLoaded(context)
    FavoritesStore.ensureLoaded(context)
}

object KateWidgets {
    suspend fun updateAll(context: Context) {
        runCatching { NowPlayingWidget().updateAll(context) }
        runCatching { AlbumArtWidget().updateAll(context) }
        runCatching { UpNextWidget().updateAll(context) }
        runCatching { QuickPlayWidget().updateAll(context) }
    }
}

@Composable
private fun roundedBackground(color: DayNight, radius: Dp): GlanceModifier {
    val context = LocalContext.current
    if (modernCorners) return GlanceModifier.background(color.provider()).cornerRadius(radius)
    val size = LocalSize.current
    val density = context.bitmapDensity()
    val bitmap = WidgetArt.roundedRect(
        color = color.resolve(context.isNightMode()).toArgb(),
        widthPx = (size.width.value * density).roundToInt(),
        heightPx = (size.height.value * density).roundToInt(),
        radiusPx = radius.value * density
    )
    return GlanceModifier.background(imageProvider = ImageProvider(bitmap), contentScale = ContentScale.FillBounds, colorFilter = null)
}

@Composable
private fun WidgetSurface(palette: WidgetPalette, corners: String = WidgetSettings.CORNERS_SYSTEM, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val radius = when (corners) {
        WidgetSettings.CORNERS_SQUARE -> 0.dp
        WidgetSettings.CORNERS_ROUND -> 32.dp
        else -> SurfaceRadius
    }
    val modifier = if (modernCorners) {
        val base = GlanceModifier.fillMaxSize()
            .appWidgetBackground()
            .background(palette.background.provider())
        if (corners == WidgetSettings.CORNERS_SYSTEM) base.cornerRadius(android.R.dimen.system_app_widget_background_radius) else base.cornerRadius(radius)
    } else {
        val size = LocalSize.current
        val density = context.bitmapDensity()
        val bitmap = WidgetArt.roundedRect(
            color = palette.background.resolve(context.isNightMode()).toArgb(),
            widthPx = (size.width.value * density).roundToInt(),
            heightPx = (size.height.value * density).roundToInt(),
            radiusPx = radius.value * density
        )
        GlanceModifier.fillMaxSize()
            .appWidgetBackground()
            .background(imageProvider = ImageProvider(bitmap), contentScale = ContentScale.FillBounds, colorFilter = null)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun circleBackground(color: DayNight, size: Dp): GlanceModifier {
    val context = LocalContext.current
    if (modernCorners) return GlanceModifier.background(color.provider()).cornerRadius(size / 2)
    val px = (size.value * context.bitmapDensity()).roundToInt()
    val bitmap = WidgetArt.circle(color.resolve(context.isNightMode()).toArgb(), px)
    return GlanceModifier.background(imageProvider = ImageProvider(bitmap), contentScale = ContentScale.FillBounds, colorFilter = null)
}

@Composable
private fun IconAction(icon: Int, description: String, tint: ColorProvider, action: Action, size: Dp = 40.dp, iconSize: Dp = 24.dp) {
    Box(modifier = GlanceModifier.size(size).clickable(action), contentAlignment = Alignment.Center) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = description,
            modifier = GlanceModifier.size(iconSize),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

@Composable
private fun PlayButton(playing: Boolean, palette: WidgetPalette, size: Dp) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier.size(size).then(circleBackground(palette.accent, size)).clickable(actionRunCallback<PlayPauseAction>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(if (playing) R.drawable.ic_widget_pause else R.drawable.ic_widget_play),
            contentDescription = context.getString(if (playing) R.string.player_pause else R.string.player_play),
            modifier = GlanceModifier.size(size * 0.5f),
            colorFilter = ColorFilter.tint(palette.onAccent.provider())
        )
    }
}

@Composable
private fun ArtImage(path: String?, size: Dp, shape: String, palette: WidgetPalette, action: Action) {
    val context = LocalContext.current
    val px = (size.value * context.bitmapDensity()).roundToInt().coerceIn(1, MAX_ART_PX)
    val bitmap = remember(path, px, shape) { WidgetArt.square(path, px, shape) }
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.size(size).clickable(action),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = GlanceModifier.size(size).then(roundedBackground(palette.placeholder, size * 0.22f)).clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_music_note),
                contentDescription = null,
                modifier = GlanceModifier.size(size * 0.45f),
                colorFilter = ColorFilter.tint(palette.secondary.provider())
            )
        }
    }
}

private fun titleStyle(palette: WidgetPalette, size: Int = 14, center: Boolean = false) = TextStyle(
    color = palette.content.provider(),
    fontSize = size.sp,
    fontWeight = FontWeight.Bold,
    textAlign = if (center) TextAlign.Center else TextAlign.Start
)

private fun subtitleStyle(palette: WidgetPalette, size: Int = 12, center: Boolean = false) = TextStyle(
    color = palette.secondary.provider(),
    fontSize = size.sp,
    textAlign = if (center) TextAlign.Center else TextAlign.Start
)

private fun tapAction(context: Context, settings: WidgetSettings, snapshot: WidgetSnapshot): Action = when {
    !snapshot.hasTrack -> actionStartActivity(openAppIntent(context))
    settings.tapAction == WidgetSettings.TAP_PLAY_PAUSE -> actionRunCallback<PlayPauseAction>()
    settings.tapAction == WidgetSettings.TAP_OPEN_APP -> actionStartActivity(openAppIntent(context))
    else -> actionStartActivity(openPlayerIntent(context))
}

private fun titleText(context: Context, snapshot: WidgetSnapshot): String =
    if (snapshot.hasTrack) snapshot.title.ifBlank { context.getString(R.string.widget_unknown_title) } else context.getString(R.string.widget_idle_title)

private fun subtitleText(context: Context, settings: WidgetSettings, snapshot: WidgetSnapshot): String {
    if (!snapshot.hasTrack) return context.getString(R.string.widget_idle_subtitle)
    val parts = buildList {
        if (settings.showArtist && snapshot.artist.isNotBlank()) add(snapshot.artist)
        if (settings.showAlbum && snapshot.album.isNotBlank()) add(snapshot.album)
    }
    if (parts.isEmpty() && !snapshot.isPlaying) return context.getString(R.string.widget_paused)
    return parts.joinToString(" • ")
}

@Composable
private fun rememberPalette(settings: WidgetSettings, snapshot: WidgetSnapshot): WidgetPalette {
    val context = LocalContext.current
    val seed = remember(snapshot.artPath, settings.background) {
        if (settings.background == WidgetSettings.BACKGROUND_ARTWORK) WidgetArt.seed(snapshot.artPath) else null
    }
    return widgetPalette(context, settings, seed)
}

@Composable
private fun Controls(settings: WidgetSettings, snapshot: WidgetSnapshot, palette: WidgetPalette, playSize: Dp, compact: Boolean) {
    val context = LocalContext.current
    val favorite = snapshot.trackId?.let { FavoritesStore.isFavorite(it) } == true
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (settings.showShuffle && !compact) {
            IconAction(
                icon = R.drawable.ic_widget_shuffle,
                description = context.getString(R.string.player_shuffle),
                tint = if (snapshot.shuffle) palette.accent.provider() else palette.secondary.provider(),
                action = actionRunCallback<ShuffleAction>(),
                size = 36.dp,
                iconSize = 20.dp
            )
        }
        if (settings.showPrevious) {
            IconAction(
                icon = R.drawable.ic_widget_previous,
                description = context.getString(R.string.player_previous),
                tint = palette.content.provider(),
                action = actionRunCallback<PreviousAction>()
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))
        PlayButton(playing = snapshot.isPlaying, palette = palette, size = playSize)
        Spacer(modifier = GlanceModifier.width(4.dp))
        IconAction(
            icon = R.drawable.ic_widget_next,
            description = context.getString(R.string.player_next),
            tint = palette.content.provider(),
            action = actionRunCallback<NextAction>()
        )
        if (settings.showRepeat && !compact) {
            IconAction(
                icon = if (snapshot.repeatMode == 1) R.drawable.ic_widget_repeat_one else R.drawable.ic_widget_repeat,
                description = context.getString(R.string.player_repeat),
                tint = if (snapshot.repeatMode != 0) palette.accent.provider() else palette.secondary.provider(),
                action = actionRunCallback<RepeatAction>(),
                size = 36.dp,
                iconSize = 20.dp
            )
        }
        if (settings.showFavorite && !compact && snapshot.hasTrack) {
            IconAction(
                icon = if (favorite) R.drawable.ic_widget_favorite else R.drawable.ic_widget_favorite_border,
                description = context.getString(R.string.player_show_favorite),
                tint = if (favorite) palette.accent.provider() else palette.secondary.provider(),
                action = actionRunCallback<FavoriteAction>(),
                size = 36.dp,
                iconSize = 20.dp
            )
        }
    }
}

@Composable
private fun Progress(snapshot: WidgetSnapshot, palette: WidgetPalette) {
    LinearProgressIndicator(
        progress = snapshot.progress(),
        modifier = GlanceModifier.fillMaxWidth().height(4.dp),
        color = palette.accent.provider(),
        backgroundColor = palette.track.provider()
    )
}

class NowPlayingWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(Strip, Bar, Card, Big))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        prepare(context)
        provideContent { NowPlayingContent(appWidgetId) }
    }
}

@Composable
private fun NowPlayingContent(appWidgetId: Int) {
    val context = LocalContext.current
    val revision = WidgetSettingsStore.revision
    val settings = remember(appWidgetId, revision) { WidgetSettingsStore.load(context, appWidgetId) }
    val snapshot = WidgetState.snapshot
    val palette = rememberPalette(settings, snapshot)
    val size = LocalSize.current
    val tap = tapAction(context, settings, snapshot)
    val title = titleText(context, snapshot)
    val subtitle = subtitleText(context, settings, snapshot)
    WidgetSurface(palette, settings.corners) {
        when {
            size.height >= 180.dp -> Column(
                modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val artSize = min(size.width.value - 32f, size.height.value * 0.48f).coerceAtLeast(48f).dp
                ArtImage(snapshot.artPath, artSize, settings.artShape, palette, tap)
                Spacer(modifier = GlanceModifier.height(10.dp))
                Text(text = title, style = titleStyle(palette, 16, center = true), maxLines = 1, modifier = GlanceModifier.fillMaxWidth().clickable(tap))
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, style = subtitleStyle(palette, 13, center = true), maxLines = 1, modifier = GlanceModifier.fillMaxWidth())
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (settings.showProgress && snapshot.hasTrack) {
                    Progress(snapshot, palette)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
                Controls(settings, snapshot, palette, playSize = 52.dp, compact = false)
            }
            size.height >= 100.dp -> Row(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val artSize = (size.height.value - 24f).coerceIn(48f, 128f).dp
                ArtImage(snapshot.artPath, artSize, settings.artShape, palette, tap)
                Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(start = 12.dp)) {
                    Text(text = title, style = titleStyle(palette, 15), maxLines = 1, modifier = GlanceModifier.fillMaxWidth().clickable(tap))
                    if (subtitle.isNotBlank()) {
                        Text(text = subtitle, style = subtitleStyle(palette), maxLines = 1, modifier = GlanceModifier.fillMaxWidth())
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (settings.showProgress && snapshot.hasTrack) {
                        Progress(snapshot, palette)
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                    Controls(settings, snapshot, palette, playSize = 44.dp, compact = size.width < 300.dp)
                }
            }
            size.width >= 220.dp -> Row(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtImage(snapshot.artPath, 44.dp, settings.artShape, palette, tap)
                Column(modifier = GlanceModifier.defaultWeight().padding(horizontal = 10.dp).clickable(tap)) {
                    Text(text = title, style = titleStyle(palette), maxLines = 1)
                    if (subtitle.isNotBlank()) {
                        Text(text = subtitle, style = subtitleStyle(palette), maxLines = 1)
                    }
                }
                Controls(settings, snapshot, palette, playSize = 40.dp, compact = size.width < 320.dp)
            }
            else -> Row(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtImage(snapshot.artPath, 40.dp, settings.artShape, palette, tap)
                Spacer(modifier = GlanceModifier.defaultWeight())
                PlayButton(playing = snapshot.isPlaying, palette = palette, size = 40.dp)
                IconAction(
                    icon = R.drawable.ic_widget_next,
                    description = context.getString(R.string.player_next),
                    tint = palette.content.provider(),
                    action = actionRunCallback<NextAction>(),
                    size = 36.dp
                )
            }
        }
    }
}

class AlbumArtWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        prepare(context)
        provideContent { AlbumArtContent(appWidgetId) }
    }
}

@Composable
private fun AlbumArtContent(appWidgetId: Int) {
    val context = LocalContext.current
    val revision = WidgetSettingsStore.revision
    val settings = remember(appWidgetId, revision) { WidgetSettingsStore.load(context, appWidgetId) }
    val snapshot = WidgetState.snapshot
    val palette = rememberPalette(settings, snapshot)
    val size = LocalSize.current
    val density = context.bitmapDensity()
    val widthPx = (size.width.value * density).roundToInt().coerceIn(1, MAX_COVER_PX)
    val heightPx = (size.height.value * density).roundToInt().coerceIn(1, MAX_COVER_PX)
    val radiusPx = SurfaceRadius.value * density
    val cover = remember(snapshot.artPath, widthPx, heightPx) { WidgetArt.cover(snapshot.artPath, widthPx, heightPx, radiusPx) }
    val scrim = remember(widthPx, heightPx) { WidgetArt.scrim(widthPx, heightPx, radiusPx) }
    val tap = tapAction(context, settings, snapshot)
    val white = DayNight.same(androidx.compose.ui.graphics.Color.White)
    val whiteSoft = DayNight.same(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f))
    val base = GlanceModifier.fillMaxSize().appWidgetBackground().clickable(tap)
    Box(
        modifier = if (modernCorners) base.cornerRadius(android.R.dimen.system_app_widget_background_radius) else base,
        contentAlignment = Alignment.BottomStart
    ) {
        if (cover != null) {
            Image(
                provider = ImageProvider(cover),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = GlanceModifier.fillMaxSize().then(roundedBackground(palette.background, SurfaceRadius)), contentAlignment = Alignment.Center) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_music_note),
                    contentDescription = null,
                    modifier = GlanceModifier.size(48.dp),
                    colorFilter = ColorFilter.tint(palette.secondary.provider())
                )
            }
        }
        Image(
            provider = ImageProvider(scrim),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.Bottom) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = titleText(context, snapshot),
                        style = TextStyle(color = white.provider(), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    val subtitle = subtitleText(context, settings, snapshot)
                    if (subtitle.isNotBlank()) {
                        Text(text = subtitle, style = TextStyle(color = whiteSoft.provider(), fontSize = 12.sp), maxLines = 1)
                    }
                }
                if (snapshot.hasTrack) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    PlayButton(playing = snapshot.isPlaying, palette = palette, size = 44.dp)
                }
            }
            if (settings.showProgress && snapshot.hasTrack) {
                Spacer(modifier = GlanceModifier.height(10.dp))
                LinearProgressIndicator(
                    progress = snapshot.progress(),
                    modifier = GlanceModifier.fillMaxWidth().height(3.dp),
                    color = white.provider(),
                    backgroundColor = DayNight.same(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)).provider()
                )
            }
        }
    }
}

class UpNextWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        prepare(context)
        provideContent { UpNextContent(appWidgetId) }
    }
}

@Composable
private fun UpNextContent(appWidgetId: Int) {
    val context = LocalContext.current
    val revision = WidgetSettingsStore.revision
    val settings = remember(appWidgetId, revision) { WidgetSettingsStore.load(context, appWidgetId) }
    val snapshot = WidgetState.snapshot
    val palette = rememberPalette(settings, snapshot)
    val open = actionStartActivity(if (snapshot.hasTrack) openPlayerIntent(context) else openAppIntent(context))
    val numbered = snapshot.upNext.mapIndexed { position, item -> (position + 1) to item }
    WidgetSurface(palette, settings.corners) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ArtImage(snapshot.artPath, 40.dp, settings.artShape, palette, open)
                Column(modifier = GlanceModifier.defaultWeight().padding(horizontal = 10.dp).clickable(open)) {
                    Text(text = context.getString(R.string.queue_now_playing), style = subtitleStyle(palette, 11), maxLines = 1)
                    Text(text = titleText(context, snapshot), style = titleStyle(palette), maxLines = 1)
                }
                if (snapshot.hasTrack) PlayButton(playing = snapshot.isPlaying, palette = palette, size = 36.dp)
            }
            Spacer(modifier = GlanceModifier.height(10.dp))
            Text(
                text = context.getString(R.string.widget_up_next_label),
                style = TextStyle(color = palette.accent.provider(), fontSize = 12.sp, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            if (numbered.isEmpty()) {
                Box(modifier = GlanceModifier.defaultWeight().fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = context.getString(R.string.widget_up_next_empty), style = subtitleStyle(palette, 13, center = true))
                }
            } else {
                LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
                    items(numbered, itemId = { it.second.index.toLong() }) { (position, item) ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable(actionRunCallback<PlayQueueItemAction>(actionParametersOf(QueueIndexKey to item.index))),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = position.toString(),
                                style = subtitleStyle(palette, 12, center = true),
                                modifier = GlanceModifier.width(24.dp)
                            )
                            Column(modifier = GlanceModifier.defaultWeight().padding(start = 6.dp)) {
                                Text(
                                    text = item.title.ifBlank { context.getString(R.string.widget_unknown_title) },
                                    style = TextStyle(color = palette.content.provider(), fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                    maxLines = 1
                                )
                                if (item.artist.isNotBlank()) {
                                    Text(text = item.artist, style = subtitleStyle(palette, 11), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class QuickShortcut(val kind: String, val icon: Int, val label: Int)

class QuickPlayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        prepare(context)
        provideContent { QuickPlayContent(appWidgetId) }
    }
}

@Composable
private fun QuickPlayContent(appWidgetId: Int) {
    val context = LocalContext.current
    val revision = WidgetSettingsStore.revision
    val settings = remember(appWidgetId, revision) { WidgetSettingsStore.load(context, appWidgetId) }
    val snapshot = WidgetState.snapshot
    val palette = rememberPalette(settings, snapshot)
    val size = LocalSize.current
    val shortcuts = buildList {
        if (settings.quickShuffle) add(QuickShortcut(QUICK_SHUFFLE_ALL, R.drawable.ic_widget_shuffle, R.string.widget_quick_shuffle))
        if (settings.quickFavorites) add(QuickShortcut(QUICK_FAVORITES, R.drawable.ic_widget_favorite, R.string.widget_quick_favorites))
        if (settings.quickRecent) add(QuickShortcut(QUICK_RECENT, R.drawable.ic_widget_new, R.string.widget_quick_recent))
        if (settings.quickResume) add(QuickShortcut(QUICK_RESUME, R.drawable.ic_widget_history, R.string.widget_quick_resume))
    }
    WidgetSurface(palette, settings.corners) {
        when {
            shortcuts.isEmpty() -> Text(
                text = context.getString(R.string.widget_quick_empty),
                style = subtitleStyle(palette, 13, center = true),
                modifier = GlanceModifier.padding(12.dp)
            )
            size.height < 100.dp || shortcuts.size <= 2 -> Row(
                modifier = GlanceModifier.fillMaxSize().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                shortcuts.forEach { shortcut ->
                    QuickTile(shortcut, palette, GlanceModifier.defaultWeight().fillMaxHeight())
                }
            }
            else -> Column(modifier = GlanceModifier.fillMaxSize().padding(6.dp)) {
                shortcuts.chunked(2).forEach { pair ->
                    Row(modifier = GlanceModifier.defaultWeight().fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        pair.forEach { shortcut ->
                            QuickTile(shortcut, palette, GlanceModifier.defaultWeight().fillMaxHeight())
                        }
                        if (pair.size == 1) Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTile(shortcut: QuickShortcut, palette: WidgetPalette, modifier: GlanceModifier) {
    val context = LocalContext.current
    val label = context.getString(shortcut.label)
    Column(
        modifier = modifier.clickable(actionRunCallback<QuickPlayAction>(actionParametersOf(QuickPlayKey to shortcut.kind))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = GlanceModifier.size(40.dp).then(circleBackground(palette.accent, 40.dp)), contentAlignment = Alignment.Center) {
            Image(
                provider = ImageProvider(shortcut.icon),
                contentDescription = label,
                modifier = GlanceModifier.size(22.dp),
                colorFilter = ColorFilter.tint(palette.onAccent.provider())
            )
        }
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(text = label, style = TextStyle(color = palette.content.provider(), fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center), maxLines = 1)
    }
}

class NowPlayingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NowPlayingWidget()
}

class AlbumArtWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AlbumArtWidget()
}

class UpNextWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UpNextWidget()
}

class QuickPlayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickPlayWidget()
}
