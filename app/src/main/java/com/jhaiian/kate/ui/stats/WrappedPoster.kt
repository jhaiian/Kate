package com.jhaiian.kate.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.ListeningStats
import com.jhaiian.kate.data.RankedItem
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.SquircleShape
import com.jhaiian.kate.ui.library.ArtThumb
import com.jhaiian.kate.ui.theme.AppIcons
import com.jhaiian.kate.ui.theme.ClintColors
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlin.math.roundToInt

private const val PosterWidthPx = 1080
private const val PosterHeightPx = 1920
private const val PosterDensity = 3f
private const val PosterListSize = 5

private class PosterPalette(
    val text: Color,
    val subtle: Color,
    val accent: Color,
    val onAccent: Color,
    val panel: Color,
    val stroke: Color,
    val gradient: List<Color>
)

private fun posterPalette(colors: ClintColors): PosterPalette {
    val strength = if (colors.isLight) 0.24f else 0.4f
    return PosterPalette(
        text = colors.onSurface,
        subtle = colors.secondaryText,
        accent = colors.primary,
        onAccent = colors.onPrimary,
        panel = colors.onSurface.copy(alpha = if (colors.isLight) 0.06f else 0.08f),
        stroke = colors.onSurface.copy(alpha = if (colors.isLight) 0.08f else 0.1f),
        gradient = listOf(
            lerp(colors.background, colors.primary, strength),
            lerp(colors.background, colors.primary, strength * 0.35f),
            colors.background
        )
    )
}

private fun TextStyle.poster(
    size: TextUnit,
    line: TextUnit,
    weight: FontWeight,
    color: Color,
    spacing: TextUnit = 0.sp
): TextStyle = copy(fontSize = size, lineHeight = line, fontWeight = weight, color = color, letterSpacing = spacing)

private fun brandBackground(variant: String): Int = when (variant) {
    "midnight" -> R.drawable.ic_launcher_background_midnight
    "sunset" -> R.drawable.ic_launcher_background_sunset
    "ocean" -> R.drawable.ic_launcher_background_ocean
    "mint" -> R.drawable.ic_launcher_background_mint
    "light" -> R.drawable.ic_launcher_background_light
    else -> R.drawable.ic_launcher_background
}

@Composable
internal fun WrappedPosterFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        content = {
            CompositionLocalProvider(LocalDensity provides Density(PosterDensity, 1f)) {
                Box(propagateMinConstraints = true) { content() }
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(Constraints.fixed(PosterWidthPx, PosterHeightPx))
        val widthScale = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() / PosterWidthPx else Float.POSITIVE_INFINITY
        val heightScale = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() / PosterHeightPx else Float.POSITIVE_INFINITY
        val fitted = minOf(widthScale, heightScale)
        val scale = if (fitted.isFinite()) fitted else density / PosterDensity
        val width = (PosterWidthPx * scale).roundToInt().coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (PosterHeightPx * scale).roundToInt().coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
internal fun WrappedPoster(
    stats: ListeningStats,
    trackMap: Map<String, Track>,
    branding: Boolean,
    captureLayer: GraphicsLayer
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val palette = remember(colors) { posterPalette(colors) }
    val brandRes = remember(context) { brandBackground(AppIcons.current(context)) }
    val base = MaterialTheme.typography.bodyMedium
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                captureLayer.record { this@drawWithContent.drawContent() }
                drawLayer(captureLayer)
            }
            .drawBehind { drawPosterBackground(palette) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp)
        ) {
            PosterHeader(stats = stats, branding = branding, brandRes = brandRes, palette = palette, base = base)
            if (stats.topArtists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                PosterArtists(artists = stats.topArtists.take(PosterListSize), trackMap = trackMap, palette = palette, base = base)
            }
            if (stats.topSongs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                PosterSongs(songs = stats.topSongs.take(PosterListSize), trackMap = trackMap, palette = palette, base = base)
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(12.dp))
            PosterNumbers(stats = stats, palette = palette, base = base)
            if (branding) {
                Spacer(modifier = Modifier.height(12.dp))
                PosterFooter(brandRes = brandRes, palette = palette, base = base)
            }
        }
    }
}

private fun DrawScope.drawPosterBackground(palette: PosterPalette) {
    drawRect(Brush.verticalGradient(palette.gradient))
    val topGlow = Offset(size.width * 0.95f, size.height * 0.02f)
    val topRadius = size.width * 0.9f
    drawCircle(
        brush = Brush.radialGradient(listOf(palette.accent.copy(alpha = 0.34f), Color.Transparent), center = topGlow, radius = topRadius),
        radius = topRadius,
        center = topGlow
    )
    val lowGlow = Offset(size.width * 0.02f, size.height * 0.74f)
    val lowRadius = size.width * 0.8f
    drawCircle(
        brush = Brush.radialGradient(listOf(palette.accent.copy(alpha = 0.18f), Color.Transparent), center = lowGlow, radius = lowRadius),
        radius = lowRadius,
        center = lowGlow
    )
    val rings = Offset(size.width * 1.04f, size.height * 0.03f)
    val stroke = Stroke(width = 1.dp.toPx())
    for (ring in 1..8) {
        drawCircle(
            color = palette.text.copy(alpha = 0.06f - ring * 0.005f),
            radius = size.width * (0.12f + ring * 0.065f),
            center = rings,
            style = stroke
        )
    }
}

@Composable
private fun KateBrandIcon(brandRes: Int, size: Dp) {
    Box(modifier = Modifier.size(size).clip(SquircleShape)) {
        Image(painterResource(brandRes), null, Modifier.fillMaxSize().scale(1.5f))
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize().scale(1.5f))
    }
}

@Composable
private fun PosterHeader(stats: ListeningStats, branding: Boolean, brandRes: Int, palette: PosterPalette, base: TextStyle) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(summaryTitleRes(stats.period.range)).uppercase(),
                style = base.poster(11.sp, 14.sp, FontWeight.Bold, palette.accent, 2.2.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            BasicText(
                text = posterDates(context, stats.period),
                style = base.poster(24.sp, 28.sp, FontWeight.Black, palette.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(minFontSize = 15.sp, maxFontSize = 24.sp, stepSize = 0.5.sp)
            )
        }
        if (branding) {
            Spacer(modifier = Modifier.width(12.dp))
            KateBrandIcon(brandRes = brandRes, size = 40.dp)
        }
    }
}

@Composable
private fun SectionLabel(text: String, palette: PosterPalette, base: TextStyle) {
    Text(
        text = text.uppercase(),
        style = base.poster(10.sp, 14.sp, FontWeight.Bold, palette.subtle, 1.6.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun PosterArtists(artists: List<RankedItem>, trackMap: Map<String, Track>, palette: PosterPalette, base: TextStyle) {
    val first = artists.first()
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(120.dp)) {
            ArtThumb(
                first.trackId?.let { trackMap[it] },
                Icons.Filled.Person,
                RoundedCornerShape(22.dp),
                48.dp,
                Modifier.fillMaxSize(),
                artistImage = ArtistImageStore.images[first.key],
                allowHardware = false
            )
            Text(
                text = stringResource(R.string.wrapped_poster_top_badge),
                style = base.poster(11.sp, 14.sp, FontWeight.Black, palette.onAccent),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.accent)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            SectionLabel(text = stringResource(R.string.stats_top_artists), palette = palette, base = base)
            Spacer(modifier = Modifier.height(6.dp))
            BasicText(
                text = first.name,
                style = base.poster(19.sp, 22.sp, FontWeight.Black, palette.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 19.sp, stepSize = 0.5.sp)
            )
            Text(
                text = playsAndTime(first),
                style = base.poster(11.sp, 14.sp, FontWeight.Medium, palette.subtle),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artists.size > 1) Spacer(modifier = Modifier.height(6.dp))
            artists.drop(1).forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(16.dp)) {
                    Text(
                        text = (index + 2).toString(),
                        style = base.poster(11.sp, 14.sp, FontWeight.Black, palette.subtle),
                        modifier = Modifier.width(16.dp)
                    )
                    Text(
                        text = item.name,
                        style = base.poster(12.5.sp, 16.sp, FontWeight.SemiBold, palette.text),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterSongs(songs: List<RankedItem>, trackMap: Map<String, Track>, palette: PosterPalette, base: TextStyle) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = stringResource(R.string.stats_top_songs), palette = palette, base = base)
        Spacer(modifier = Modifier.height(8.dp))
        songs.forEachIndexed { index, item ->
            if (index > 0) Spacer(modifier = Modifier.height(4.dp))
            val highlight = if (index == 0) palette.accent else palette.subtle
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(34.dp)) {
                Text(
                    text = (index + 1).toString(),
                    style = base.poster(13.sp, 16.sp, FontWeight.Black, highlight),
                    modifier = Modifier.width(20.dp)
                )
                ArtThumb(
                    item.trackId?.let { trackMap[it] },
                    Icons.Filled.MusicNote,
                    RoundedCornerShape(9.dp),
                    16.dp,
                    Modifier.size(34.dp),
                    allowHardware = false
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = base.poster(13.5.sp, 17.sp, FontWeight.Bold, palette.text),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = base.poster(11.sp, 14.sp, FontWeight.Medium, palette.subtle),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays),
                    style = base.poster(11.sp, 14.sp, FontWeight.Bold, highlight),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PosterNumbers(stats: ListeningStats, palette: PosterPalette, base: TextStyle) {
    val context = LocalContext.current
    val genre = stats.topGenres.firstOrNull()?.name?.takeIf { it.isNotBlank() }
    val hour = stats.peakHour
    val day = stats.biggestDay
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            NumberTile(
                value = formatNumber(minutesOf(stats.totalMs)),
                label = stringResource(R.string.stats_minutes_label),
                palette = palette,
                base = base,
                highlight = true
            )
            NumberTile(formatNumber(stats.plays.toLong()), stringResource(R.string.stats_plays_label), palette, base)
            NumberTile(formatNumber(stats.uniqueSongs.toLong()), stringResource(R.string.stats_songs_label), palette, base)
            NumberTile(formatNumber(stats.uniqueArtists.toLong()), stringResource(R.string.stats_artists_label), palette, base)
        }
        if (genre != null || hour != null || day != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (genre != null) InfoTile(stringResource(R.string.wrapped_top_genre_label), genre, palette, base)
                if (hour != null) {
                    val persona = stringResource(personaRes(hour))
                    InfoTile(
                        label = stringResource(R.string.wrapped_clock_title),
                        value = persona,
                        palette = palette,
                        base = base,
                        lines = if (persona.trim().any { it.isWhitespace() }) 2 else 1
                    )
                }
                if (day != null) InfoTile(stringResource(R.string.wrapped_poster_big_day), shortDayLabel(context, day), palette, base)
            }
        }
    }
}

@Composable
private fun RowScope.NumberTile(
    value: String,
    label: String,
    palette: PosterPalette,
    base: TextStyle,
    highlight: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    val content = if (highlight) palette.onAccent else palette.text
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(if (highlight) palette.accent else palette.panel)
            .then(if (highlight) Modifier else Modifier.border(1.dp, palette.stroke, shape))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BasicText(
            text = value,
            style = base.poster(20.sp, 24.sp, FontWeight.Black, content),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 20.sp, stepSize = 0.5.sp)
        )
        Spacer(modifier = Modifier.height(1.dp))
        BasicText(
            text = label,
            style = base.poster(10.sp, 13.sp, FontWeight.SemiBold, if (highlight) content.copy(alpha = 0.82f) else palette.subtle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 10.sp, stepSize = 0.5.sp)
        )
    }
}

@Composable
private fun RowScope.InfoTile(label: String, value: String, palette: PosterPalette, base: TextStyle, lines: Int = 1) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(palette.panel)
            .border(1.dp, palette.stroke, shape)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        BasicText(
            text = label,
            style = base.poster(10.sp, 13.sp, FontWeight.SemiBold, palette.subtle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 10.sp, stepSize = 0.5.sp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth().height(32.dp)) {
            BasicText(
                text = value,
                style = base.poster(15.sp, 16.sp, FontWeight.Black, palette.text),
                maxLines = lines,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 15.sp, stepSize = 0.5.sp)
            )
        }
    }
}

@Composable
private fun PosterFooter(brandRes: Int, palette: PosterPalette, base: TextStyle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        KateBrandIcon(brandRes = brandRes, size = 20.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.wrapped_poster_made_with),
            style = base.poster(12.sp, 16.sp, FontWeight.Bold, palette.subtle, 0.4.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
