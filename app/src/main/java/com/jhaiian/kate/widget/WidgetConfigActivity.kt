package com.jhaiian.kate.widget

import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.rememberKateHaptics
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.settings.SectionHeader
import com.jhaiian.kate.ui.settings.SettingsSwitchRow
import com.jhaiian.kate.ui.theme.ClintComposeTheme
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WidgetConfigActivity : ComponentActivity() {
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val resultIntent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultIntent)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.provider?.className.orEmpty()
        val kind = when {
            provider.endsWith("AlbumArtWidgetReceiver") -> WidgetKind.ALBUM_ART
            provider.endsWith("UpNextWidgetReceiver") -> WidgetKind.UP_NEXT
            provider.endsWith("QuickPlayWidgetReceiver") -> WidgetKind.QUICK_PLAY
            else -> WidgetKind.NOW_PLAYING
        }
        val initial = WidgetSettingsStore.load(this, appWidgetId)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val look = remember { LookAndFeelSettings.load(context) }
            ClintComposeTheme(look.theme, look.appArtworkColors) {
                CompositionLocalProvider(
                    LocalLookAndFeel provides look,
                    LocalKateHaptics provides rememberKateHaptics(look.haptics)
                ) {
                    WidgetConfigScreen(
                        kind = kind,
                        initial = initial,
                        onCancel = { finish() },
                        onSave = { settings ->
                            WidgetSettingsStore.save(context, appWidgetId, settings)
                            val appContext = context.applicationContext
                            updateScope.launch { KateWidgets.updateAll(appContext) }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    kind: WidgetKind,
    initial: WidgetSettings,
    onCancel: () -> Unit,
    onSave: (WidgetSettings) -> Unit
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var settings by remember { mutableStateOf(initial) }
    val hasArt = kind != WidgetKind.QUICK_PLAY
    val hasPlayerOptions = kind == WidgetKind.NOW_PLAYING || kind == WidgetKind.ALBUM_ART
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            KateIconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.action_cancel), tint = buttonTint(colors.onSurface))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.widget_config_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(kindName(kind)),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText
                )
            }
            TextButton(onClick = {
                haptics.confirm()
                onSave(settings)
            }) {
                Text(text = stringResource(R.string.eq_save), color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
                    WidgetPreview(kind = kind, settings = settings)
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(stringResource(R.string.widget_section_look), colors.primary)
                    ConfigCard {
                        OptionLabel(stringResource(R.string.look_section_background))
                        OptionChips(
                            options = WidgetSettings.Backgrounds.map { it to backgroundLabel(it) },
                            selected = settings.background,
                            onSelect = {
                                haptics.click()
                                settings = settings.copy(background = it)
                            }
                        )
                        RowDivider(colors.divider)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_opacity),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = stringResource(R.string.widget_opacity_value, settings.opacity),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        Slider(
                            value = settings.opacity.toFloat(),
                            onValueChange = { value ->
                                val snapped = ((value / 5f).roundToInt() * 5).coerceIn(0, 100)
                                if (snapped != settings.opacity) {
                                    haptics.tick()
                                    settings = settings.copy(opacity = snapped)
                                }
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primary,
                                activeTrackColor = colors.primary,
                                inactiveTrackColor = colors.primary.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        RowDivider(colors.divider)
                        OptionLabel(stringResource(R.string.widget_corners_title))
                        OptionChips(
                            options = WidgetSettings.Corners.map { it to cornerLabel(it) },
                            selected = settings.corners,
                            onSelect = {
                                haptics.click()
                                settings = settings.copy(corners = it)
                            }
                        )
                        if (hasArt) {
                            RowDivider(colors.divider)
                            OptionLabel(stringResource(R.string.player_art_shape_title))
                            OptionChips(
                                options = WidgetSettings.ArtShapes.map { it to artShapeLabel(it) },
                                selected = settings.artShape,
                                onSelect = {
                                    haptics.click()
                                    settings = settings.copy(artShape = it)
                                }
                            )
                        }
                    }
                    if (hasPlayerOptions) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(stringResource(R.string.widget_section_show), colors.primary)
                        ConfigCard {
                            SettingsSwitchRow(
                                icon = Icons.Outlined.LinearScale,
                                title = stringResource(R.string.widget_show_progress),
                                subtitle = stringResource(R.string.widget_show_progress_subtitle),
                                checked = settings.showProgress,
                                colors = colors,
                                onCheckedChange = { settings = settings.copy(showProgress = it) }
                            )
                            RowDivider(colors.divider)
                            SettingsSwitchRow(
                                icon = Icons.Outlined.Person,
                                title = stringResource(R.string.widget_show_artist),
                                subtitle = stringResource(R.string.widget_show_artist_subtitle),
                                checked = settings.showArtist,
                                colors = colors,
                                onCheckedChange = { settings = settings.copy(showArtist = it) }
                            )
                            RowDivider(colors.divider)
                            SettingsSwitchRow(
                                icon = Icons.Outlined.Album,
                                title = stringResource(R.string.widget_show_album),
                                subtitle = stringResource(R.string.widget_show_album_subtitle),
                                checked = settings.showAlbum,
                                colors = colors,
                                onCheckedChange = { settings = settings.copy(showAlbum = it) }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(stringResource(R.string.widget_section_tap), colors.primary)
                        ConfigCard {
                            OptionChips(
                                options = WidgetSettings.TapActions.map { it to tapLabel(it) },
                                selected = settings.tapAction,
                                onSelect = {
                                    haptics.click()
                                    settings = settings.copy(tapAction = it)
                                }
                            )
                        }
                    }
                    AnimatedVisibility(visible = kind == WidgetKind.NOW_PLAYING) {
                        Column {
                            Spacer(modifier = Modifier.height(20.dp))
                            SectionHeader(stringResource(R.string.look_section_buttons), colors.primary)
                            ConfigCard {
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.SkipPrevious,
                                    title = stringResource(R.string.player_previous),
                                    subtitle = stringResource(R.string.widget_button_subtitle),
                                    checked = settings.showPrevious,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(showPrevious = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.Shuffle,
                                    title = stringResource(R.string.player_show_shuffle),
                                    subtitle = stringResource(R.string.widget_button_subtitle_wide),
                                    checked = settings.showShuffle,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(showShuffle = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.Repeat,
                                    title = stringResource(R.string.player_show_repeat),
                                    subtitle = stringResource(R.string.widget_button_subtitle_wide),
                                    checked = settings.showRepeat,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(showRepeat = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    title = stringResource(R.string.player_show_favorite),
                                    subtitle = stringResource(R.string.widget_button_subtitle_wide),
                                    checked = settings.showFavorite,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(showFavorite = it) }
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = kind == WidgetKind.QUICK_PLAY) {
                        Column {
                            Spacer(modifier = Modifier.height(20.dp))
                            SectionHeader(stringResource(R.string.widget_section_shortcuts), colors.primary)
                            ConfigCard {
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.Shuffle,
                                    title = stringResource(R.string.widget_quick_shuffle),
                                    subtitle = stringResource(R.string.widget_quick_shuffle_subtitle),
                                    checked = settings.quickShuffle,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(quickShuffle = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    title = stringResource(R.string.widget_quick_favorites),
                                    subtitle = stringResource(R.string.widget_quick_favorites_subtitle),
                                    checked = settings.quickFavorites,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(quickFavorites = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.LibraryAdd,
                                    title = stringResource(R.string.widget_quick_recent),
                                    subtitle = stringResource(R.string.widget_quick_recent_subtitle),
                                    checked = settings.quickRecent,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(quickRecent = it) }
                                )
                                RowDivider(colors.divider)
                                SettingsSwitchRow(
                                    icon = Icons.Outlined.History,
                                    title = stringResource(R.string.widget_quick_resume),
                                    subtitle = stringResource(R.string.widget_quick_resume_subtitle),
                                    checked = settings.quickResume,
                                    colors = colors,
                                    onCheckedChange = { settings = settings.copy(quickResume = it) }
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.widget_config_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                    )
                }
            }
        }
    }
}

private fun kindName(kind: WidgetKind): Int = when (kind) {
    WidgetKind.NOW_PLAYING -> R.string.widget_now_playing_name
    WidgetKind.ALBUM_ART -> R.string.widget_album_art_name
    WidgetKind.UP_NEXT -> R.string.widget_up_next_name
    WidgetKind.QUICK_PLAY -> R.string.widget_quick_play_name
}

private fun backgroundLabel(key: String): Int = when (key) {
    WidgetSettings.BACKGROUND_ARTWORK -> R.string.widget_background_artwork
    WidgetSettings.BACKGROUND_DARK -> R.string.theme_dark
    WidgetSettings.BACKGROUND_LIGHT -> R.string.theme_light
    else -> R.string.widget_background_system
}

private fun cornerLabel(key: String): Int = when (key) {
    WidgetSettings.CORNERS_SQUARE -> R.string.library_corners_sharp
    WidgetSettings.CORNERS_ROUND -> R.string.library_corners_round
    else -> R.string.widget_corners_system
}

private fun artShapeLabel(key: String): Int = when (key) {
    PlayerStylePreferences.ART_SHAPE_CIRCLE -> R.string.player_art_shape_circle
    PlayerStylePreferences.ART_SHAPE_SQUARE -> R.string.player_art_shape_square
    else -> R.string.player_art_shape_rounded
}

private fun tapLabel(key: String): Int = when (key) {
    WidgetSettings.TAP_OPEN_APP -> R.string.widget_tap_open_app
    WidgetSettings.TAP_PLAY_PAUSE -> R.string.widget_tap_play_pause
    else -> R.string.widget_tap_open_player
}

@Composable
private fun ConfigCard(content: @Composable () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
    ) {
        content()
    }
}

@Composable
private fun OptionLabel(text: String) {
    val colors = LocalClintColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = colors.onSurface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
    )
}

@Composable
private fun OptionChips(options: List<Pair<String, Int>>, selected: String, onSelect: (String) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        options.forEach { (key, label) ->
            val isSelected = key == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) colors.primary else colors.surfaceVariant)
                    .clickable { if (!isSelected) onSelect(key) }
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onPrimary else colors.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WidgetPreview(kind: WidgetKind, settings: WidgetSettings) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val night = colors.isLight.not()
    val palette = remember(settings, night) { widgetPalette(context, settings, null) }
    val background = palette.background.resolve(night)
    val content = palette.content.resolve(night)
    val secondary = palette.secondary.resolve(night)
    val accent = palette.accent.resolve(night)
    val onAccent = palette.onAccent.resolve(night)
    val track = palette.track.resolve(night)
    val artShape: Shape = when (settings.artShape) {
        PlayerStylePreferences.ART_SHAPE_CIRCLE -> CircleShape
        PlayerStylePreferences.ART_SHAPE_SQUARE -> RoundedCornerShape(3.dp)
        else -> RoundedCornerShape(10.dp)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.6f))
            .padding(24.dp)
    ) {
        when (kind) {
            WidgetKind.ALBUM_ART -> Box(
                contentAlignment = Alignment.BottomStart,
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(accent.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center).size(56.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        PreviewBar(Color.White, 72.dp, 7.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        if (settings.showArtist) PreviewBar(Color.White.copy(alpha = 0.7f), 48.dp, 5.dp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(accent)
                    ) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = null, tint = onAccent, modifier = Modifier.size(18.dp))
                    }
                }
            }
            WidgetKind.QUICK_PLAY -> Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(background)
                    .padding(vertical = 14.dp)
            ) {
                val tiles = listOfNotNull(
                    if (settings.quickShuffle) Icons.Outlined.Shuffle else null,
                    if (settings.quickFavorites) Icons.Outlined.FavoriteBorder else null,
                    if (settings.quickRecent) Icons.Outlined.LibraryAdd else null,
                    if (settings.quickResume) Icons.Outlined.History else null
                )
                tiles.forEach { icon ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(accent)) {
                        Icon(imageVector = icon, contentDescription = null, tint = onAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }
            WidgetKind.UP_NEXT -> Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(background)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(artShape).background(accent.copy(alpha = 0.4f)))
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        PreviewBar(secondary, 50.dp, 5.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        PreviewBar(content, 96.dp, 7.dp)
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(CircleShape).background(accent)) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = null, tint = onAccent, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                repeat(3) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        PreviewBar(secondary, 10.dp, 6.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        PreviewBar(content.copy(alpha = 0.8f), 120.dp, 6.dp)
                    }
                }
            }
            WidgetKind.NOW_PLAYING -> Column(
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(background)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp).aspectRatio(1f).clip(artShape).background(accent.copy(alpha = 0.4f)))
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        PreviewBar(content, 110.dp, 8.dp)
                        if (settings.showArtist || settings.showAlbum) {
                            Spacer(modifier = Modifier.height(6.dp))
                            PreviewBar(secondary, 70.dp, 6.dp)
                        }
                        if (settings.showProgress) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(track)) {
                                Box(modifier = Modifier.fillMaxWidth(0.4f).height(4.dp).background(accent))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (settings.showShuffle) Icon(Icons.Outlined.Shuffle, null, tint = secondary, modifier = Modifier.size(18.dp))
                            if (settings.showPrevious) Icon(Icons.Outlined.SkipPrevious, null, tint = content, modifier = Modifier.size(20.dp))
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(CircleShape).background(accent)) {
                                Icon(imageVector = Icons.Filled.Pause, contentDescription = null, tint = onAccent, modifier = Modifier.size(16.dp))
                            }
                            Icon(Icons.Filled.SkipNext, null, tint = content, modifier = Modifier.size(20.dp))
                            if (settings.showRepeat) Icon(Icons.Outlined.Repeat, null, tint = secondary, modifier = Modifier.size(18.dp))
                            if (settings.showFavorite) Icon(Icons.Outlined.FavoriteBorder, null, tint = secondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewBar(color: Color, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.width(width).height(height).clip(RoundedCornerShape(height / 2)).background(color))
}
