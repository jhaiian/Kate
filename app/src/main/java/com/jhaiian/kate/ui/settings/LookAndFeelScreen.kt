package com.jhaiian.kate.ui.settings

import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.jhaiian.kate.ui.components.kateChoiceTint
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DensityLarge
import androidx.compose.material.icons.outlined.DensitySmall
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material.icons.outlined.SwipeRight
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtworkColors
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateHaptics
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.MatchAppSystemBars
import com.jhaiian.kate.ui.components.rememberSheetFlingConnection
import com.jhaiian.kate.ui.theme.AccentOptions
import com.jhaiian.kate.ui.theme.ArtShapeOptions
import com.jhaiian.kate.ui.theme.ArtSizeOptions
import com.jhaiian.kate.ui.theme.ArtworkColorScope
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.CornerStyleOptions
import com.jhaiian.kate.ui.theme.DoubleTapOptions
import com.jhaiian.kate.ui.theme.HapticsOptions
import com.jhaiian.kate.ui.theme.HomeSectionOptions
import com.jhaiian.kate.ui.theme.IntensityOptions
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LyricsAlignmentOptions
import com.jhaiian.kate.ui.theme.LyricsPreferences
import com.jhaiian.kate.ui.theme.LyricsTextSizeOptions
import com.jhaiian.kate.ui.theme.MiniProgressPreferences
import com.jhaiian.kate.ui.theme.MiniProgressStyleOptions
import com.jhaiian.kate.ui.theme.MiniProgressThicknessOptions
import com.jhaiian.kate.ui.theme.SeekBarPreferences
import com.jhaiian.kate.ui.theme.SeekColorOptions
import com.jhaiian.kate.ui.theme.SeekSquiggleHeightOptions
import com.jhaiian.kate.ui.theme.SeekSquiggleLengthOptions
import com.jhaiian.kate.ui.theme.SeekSquiggleSpeedOptions
import com.jhaiian.kate.ui.theme.SeekStyleOptions
import com.jhaiian.kate.ui.theme.SeekThicknessOptions
import com.jhaiian.kate.ui.theme.SeekThumbOptions
import com.jhaiian.kate.ui.theme.SeekWaveDensityOptions
import com.jhaiian.kate.ui.theme.SeekWaveShapeOptions
import com.jhaiian.kate.ui.player.SeekBarPreview
import com.jhaiian.kate.ui.player.seekBarLook
import com.jhaiian.kate.ui.player.shiftedHue
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.isCustomAccent
import com.jhaiian.kate.ui.theme.MiniPlayerStyleOptions
import com.jhaiian.kate.ui.theme.NavLabelOptions
import com.jhaiian.kate.ui.theme.NavTabOption
import com.jhaiian.kate.ui.theme.NavTabOptions
import com.jhaiian.kate.ui.theme.NavigationPreferences
import com.jhaiian.kate.ui.theme.PlayerBackgroundOptions
import com.jhaiian.kate.ui.theme.PlayerBackdropStrengthOptions
import com.jhaiian.kate.ui.theme.PlayerHeaderOptions
import com.jhaiian.kate.ui.theme.PlayerInfoAlignOptions
import com.jhaiian.kate.ui.theme.PlayerScreenBackgroundOptions
import com.jhaiian.kate.ui.theme.PlayerSkipButtonOptions
import com.jhaiian.kate.ui.theme.PlayerTitleSizeOptions
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Widgets
import com.jhaiian.kate.ui.theme.PlayerLayoutOptions
import com.jhaiian.kate.ui.theme.FontStyleOptions
import com.jhaiian.kate.ui.theme.TextSizeOptions
import com.jhaiian.kate.ui.theme.AnimationSpeedOptions
import com.jhaiian.kate.ui.theme.DisplayPreferences
import com.jhaiian.kate.ui.theme.MotionPreferences
import com.jhaiian.kate.ui.theme.kateFontFamily
import com.jhaiian.kate.ui.theme.FontStore
import com.jhaiian.kate.ui.theme.FineTune
import com.jhaiian.kate.ui.player.PlayerActions
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.PlayCircle
import com.jhaiian.kate.ui.theme.VinylSpeedOptions
import com.jhaiian.kate.ui.theme.CassetteColorOptions
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Speed
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import com.jhaiian.kate.ui.theme.ThemeOptions
import com.jhaiian.kate.ui.theme.resolveClintTheme
import com.jhaiian.kate.ui.theme.ThemeTuning

private enum class LookAndFeelSheet { THEME, ACCENT, INTENSITY }

private const val ClintGeckoUrl = "https://github.com/jhaiian/ClintGecko"

private const val PreviewProgress = 0.42f

@Composable
fun LookAndFeelHubScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onThemeColorsClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onButtonsClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onNavigationClick: () -> Unit,
    onDisplayClick: () -> Unit,
    onMotionClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onStylesClick: () -> Unit = {},
    onVisualizerClick: () -> Unit = {}
) {
    val colors = LocalClintColors.current
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.section_look_and_feel),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_all_message),
                onConfirm = {
                    confirmReset = false
                    onSettingsChange(LookAndFeelSettings())
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        LookPreviewCard(settings)
        Spacer(modifier = Modifier.height(20.dp))
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Style,
                title = stringResource(R.string.styles_title),
                subtitle = stringResource(R.string.styles_summary),
                colors = colors,
                onClick = onStylesClick
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.look_theme_colors_title),
                subtitle = themeSummary(settings),
                colors = colors,
                onClick = onThemeColorsClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Album,
                title = stringResource(R.string.look_player_title),
                subtitle = playerSummary(settings),
                colors = colors,
                onClick = onPlayerClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.GraphicEq,
                title = stringResource(R.string.visualizer_title),
                subtitle = stringResource(R.string.visualizer_summary),
                colors = colors,
                onClick = onVisualizerClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.TouchApp,
                title = stringResource(R.string.look_buttons_title),
                subtitle = buttonsSummary(settings),
                colors = colors,
                onClick = onButtonsClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.LibraryMusic,
                title = stringResource(R.string.nav_library),
                subtitle = librarySummary(settings),
                colors = colors,
                onClick = onLibraryClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Explore,
                title = stringResource(R.string.look_navigation_title),
                subtitle = stringResource(
                    R.string.look_navigation_summary,
                    settings.visibleTabs.size,
                    stringResource(NavLabelOptions.first { it.key == settings.navLabels }.labelRes)
                ),
                colors = colors,
                onClick = onNavigationClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Vibration,
                title = stringResource(R.string.look_motion_title),
                subtitle = stringResource(
                    R.string.look_motion_summary,
                    stringResource(HapticsOptions.first { it.key == settings.haptics }.labelRes)
                ),
                colors = colors,
                onClick = onMotionClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Smartphone,
                title = stringResource(R.string.section_display),
                subtitle = stringResource(displaySummaryRes(settings)),
                colors = colors,
                onClick = onDisplayClick
            )
        }
    }
}

@Composable
fun ThemeColorsScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    onCustomThemesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    var openSheet by remember { mutableStateOf<LookAndFeelSheet?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.look_theme_colors_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_theme_message),
                onConfirm = {
                    confirmReset = false
                    val defaults = LookAndFeelSettings()
                    onSettingsChange(
                        settings.copy(
                            theme = defaults.theme,
                            accent = defaults.accent,
                            intensity = defaults.intensity,
                            appArtworkColors = defaults.appArtworkColors,
                            paletteStyle = defaults.paletteStyle,
                            contrast = defaults.contrast,
                            customAccent = defaults.customAccent,
                            colorBackground = defaults.colorBackground,
                            colorCard = defaults.colorCard,
                            colorText = defaults.colorText,
                            colorSecondaryText = defaults.colorSecondaryText
                        )
                    )
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        LookPreviewCard(settings)
        Spacer(modifier = Modifier.height(14.dp))
        val uriHandler = LocalUriHandler.current
        val geckoName = stringResource(R.string.clint_gecko_name)
        val poweredBy = stringResource(R.string.theme_powered_by, geckoName)
        Text(
            text = buildAnnotatedString {
                append(poweredBy)
                val start = poweredBy.indexOf(geckoName)
                if (start >= 0) {
                    addStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold), start, start + geckoName.length)
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { runCatching { uriHandler.openUri(ClintGeckoUrl) } }
                .padding(start = 24.dp, end = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.section_theme), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.row_theme_title),
                subtitle = stringResource(ThemeOptions.first { it.key == settings.theme }.labelRes),
                colors = colors,
                onClick = { openSheet = LookAndFeelSheet.THEME }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.row_accent_title),
                subtitle = accentLabel(settings.accent),
                colors = colors,
                onClick = { openSheet = LookAndFeelSheet.ACCENT }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Gradient,
                title = stringResource(R.string.row_surface_intensity_title),
                subtitle = stringResource(IntensityOptions.first { it.key == settings.intensity }.labelRes),
                colors = colors,
                onClick = { openSheet = LookAndFeelSheet.INTENSITY }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_album_art), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.ColorLens,
                title = stringResource(R.string.row_app_artwork_colors_title),
                subtitle = stringResource(R.string.row_app_artwork_colors_subtitle),
                checked = settings.appArtworkColors,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(appArtworkColors = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.custom_themes_title), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Brush,
                title = stringResource(R.string.custom_themes_title),
                subtitle = if (isCustomAccent(settings.accent)) {
                    stringResource(R.string.custom_themes_row_active, accentLabel(settings.accent))
                } else {
                    stringResource(R.string.custom_themes_row_summary)
                },
                colors = colors,
                onClick = onCustomThemesClick
            )
        }
        ColorEngineSection(settings, onSettingsChange)
    }

    when (openSheet) {
        LookAndFeelSheet.THEME -> ThemeSheet(
            current = settings.theme,
            onSelect = { onSettingsChange(settings.copy(theme = it)); openSheet = null },
            onDismiss = { openSheet = null }
        )
        LookAndFeelSheet.ACCENT -> AccentSheet(
            current = settings.accent,
            theme = settings.theme,
            intensity = settings.intensity,
            tuning = settings.themeTuning,
            onSelect = { onSettingsChange(settings.copy(accent = it)); openSheet = null },
            onDismiss = { openSheet = null }
        )
        LookAndFeelSheet.INTENSITY -> IntensitySheet(
            current = settings.intensity,
            tuning = settings.themeTuning,
            theme = settings.theme,
            accent = settings.accent,
            appArtworkColors = settings.appArtworkColors,
            onSelect = { onSettingsChange(settings.copy(intensity = it)); openSheet = null },
            onDismiss = { openSheet = null }
        )
        null -> {}
    }
}

@Composable
fun PlayerLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.look_player_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_player_message),
                onConfirm = {
                    confirmReset = false
                    onSettingsChange(settings.withDefaultPlayerStyle())
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        LookPreviewCard(settings)
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.player_layout_title), colors.primary)
        SettingsCard {
            PlayerLayoutPicker(
                selected = settings.playerLayout,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerLayout = it))
                }
            )
            RowDivider(colors.divider)
            LandscapeLayoutRow(settings, onSettingsChange)
            AnimatedVisibility(visible = settings.playerLayout == PlayerStylePreferences.LAYOUT_VINYL) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.player_vinyl_speed_title))
                    ChoiceTiles(
                        options = VinylSpeedOptions.map { it.key to it.labelRes },
                        selected = settings.playerVinylSpeed,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(playerVinylSpeed = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = when (key) {
                                    PlayerStylePreferences.VINYL_SPEED_SLOW -> Icons.Outlined.SlowMotionVideo
                                    PlayerStylePreferences.VINYL_SPEED_45 -> Icons.Outlined.Speed
                                    else -> Icons.Outlined.Album
                                },
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Album,
                        title = stringResource(R.string.player_vinyl_tonearm_title),
                        subtitle = stringResource(R.string.player_vinyl_tonearm_subtitle),
                        checked = settings.playerVinylTonearm,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(playerVinylTonearm = it)) }
                    )
                }
            }
            AnimatedVisibility(visible = settings.playerLayout == PlayerStylePreferences.LAYOUT_CASSETTE) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.player_cassette_color_title))
                    ChoiceTiles(
                        options = CassetteColorOptions.map { it.key to it.labelRes },
                        selected = settings.playerCassetteColor,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(playerCassetteColor = it))
                        },
                        icon = { key, tint -> CassetteColorSwatch(key, tint) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_colors), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.Album,
                title = stringResource(R.string.row_player_artwork_colors_title),
                subtitle = stringResource(
                    if (settings.appArtworkColors) R.string.look_player_follows_app else R.string.row_player_artwork_colors_subtitle
                ),
                checked = settings.playerFollowsArtwork,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerArtworkColors = it)) },
                enabled = !settings.appArtworkColors
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_top_bar), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.player_header_title))
            ChoiceTiles(
                options = PlayerHeaderOptions.map { it.key to it.labelRes },
                selected = settings.playerHeader,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerHeader = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            PlayerStylePreferences.HEADER_QUEUE -> Icons.AutoMirrored.Outlined.QueueMusic
                            PlayerStylePreferences.HEADER_HIDDEN -> Icons.Outlined.VisibilityOff
                            else -> Icons.Outlined.Album
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.DragHandle,
                title = stringResource(R.string.player_show_handle_title),
                subtitle = stringResource(R.string.player_show_handle_subtitle),
                checked = settings.playerShowHandle,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowHandle = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.ExpandMore,
                title = stringResource(R.string.player_show_collapse_title),
                subtitle = stringResource(R.string.player_show_collapse_subtitle),
                checked = settings.playerShowCollapse,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowCollapse = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                title = stringResource(R.string.player_show_queue_button_title),
                subtitle = stringResource(R.string.player_show_queue_button_subtitle),
                checked = settings.playerShowQueueButton,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowQueueButton = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.MoreVert,
                title = stringResource(R.string.player_show_menu_button_title),
                subtitle = stringResource(R.string.player_show_menu_button_subtitle),
                checked = settings.playerShowMenuButton,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowMenuButton = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.GraphicEq,
                title = stringResource(R.string.player_show_audio_chip_title),
                subtitle = stringResource(R.string.player_show_audio_chip_subtitle),
                checked = settings.playerShowAudioChip,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowAudioChip = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_song_info), colors.primary)
        SettingsCard {
            AnimatedVisibility(visible = settings.playerLayout != PlayerStylePreferences.LAYOUT_MINIMAL && settings.playerLayout != PlayerStylePreferences.LAYOUT_CARD) {
                Column {
                    ChoiceGroupLabel(stringResource(R.string.player_info_align_title))
                    ChoiceTiles(
                        options = PlayerInfoAlignOptions.map { it.key to it.labelRes },
                        selected = settings.playerInfoAlign,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(playerInfoAlign = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = if (key == PlayerStylePreferences.ALIGN_CENTER) {
                                    Icons.Outlined.FormatAlignCenter
                                } else {
                                    Icons.AutoMirrored.Outlined.FormatAlignLeft
                                },
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    RowDivider(colors.divider)
                }
            }
            InfoFieldsRow(settings, onSettingsChange)
            RowDivider(colors.divider)
            TitleWeightRow(settings, onSettingsChange)
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.player_title_size_title))
            ChoiceTiles(
                options = PlayerTitleSizeOptions.map { it.key to it.labelRes },
                selected = settings.playerTitleSize,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerTitleSize = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(
                            when (key) {
                                PlayerStylePreferences.TITLE_SMALL -> 20.dp
                                PlayerStylePreferences.TITLE_LARGE -> 32.dp
                                else -> 26.dp
                            }
                        )
                    )
                }
            )
            AnimatedVisibility(visible = settings.playerLayout != PlayerStylePreferences.LAYOUT_MINIMAL && settings.playerLayout != PlayerStylePreferences.LAYOUT_CARD) {
                Column {
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Album,
                        title = stringResource(R.string.player_show_album_title),
                        subtitle = stringResource(R.string.player_show_album_subtitle),
                        checked = settings.playerShowAlbum,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(playerShowAlbum = it)) }
                    )
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.AutoMirrored.Outlined.ShortText,
                        title = stringResource(R.string.player_marquee_title),
                        subtitle = stringResource(R.string.player_marquee_subtitle),
                        checked = settings.playerMarquee,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(playerMarquee = it)) }
                    )
                }
            }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Subtitles,
                title = stringResource(R.string.player_lyric_line_title),
                subtitle = stringResource(R.string.player_lyric_line_subtitle),
                checked = settings.playerLyricLine,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerLyricLine = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_artwork), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.player_art_shape_title))
            ChoiceTiles(
                options = ArtShapeOptions.map { it.key to it.labelRes },
                selected = settings.playerArtShape,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerArtShape = it))
                },
                icon = { key, tint -> ArtShapeSwatch(key, tint) }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.player_art_size_title))
            ChoiceTiles(
                options = ArtSizeOptions.map { it.key to it.labelRes },
                selected = settings.playerArtSize,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerArtSize = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = if (key == PlayerStylePreferences.ART_SIZE_LARGE) Icons.Outlined.Fullscreen else Icons.Outlined.CropSquare,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            FineTuneSlider(
                title = stringResource(R.string.fine_tune_art_size),
                value = PlayerStylePreferences.artScale(settings.playerArtSize),
                range = PlayerStylePreferences.MIN_ART_SCALE..PlayerStylePreferences.MAX_ART_SCALE,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(playerArtSize = FineTune.key(it))) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.WbSunny,
                title = stringResource(R.string.player_glow_title),
                subtitle = stringResource(R.string.player_glow_subtitle),
                checked = settings.playerGlow,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerGlow = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Layers,
                title = stringResource(R.string.player_art_shadow_title),
                subtitle = stringResource(R.string.player_art_shadow_subtitle),
                checked = settings.playerArtShadow,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerArtShadow = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_background), colors.primary)
        SettingsCard {
            ChoiceTiles(
                options = PlayerScreenBackgroundOptions.map { it.key to it.labelRes },
                selected = settings.playerBackground,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(playerBackground = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            PlayerStylePreferences.BACKGROUND_ARTWORK -> Icons.Outlined.Image
                            PlayerStylePreferences.BACKGROUND_PLAIN -> Icons.Outlined.CheckBoxOutlineBlank
                            PlayerStylePreferences.BACKGROUND_GRADIENT -> Icons.Outlined.Gradient
                            PlayerStylePreferences.BACKGROUND_IMAGE -> Icons.Outlined.Wallpaper
                            else -> Icons.Outlined.BubbleChart
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            AnimatedVisibility(visible = settings.playerBackground != PlayerStylePreferences.BACKGROUND_PLAIN) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.player_backdrop_strength_title))
                    ChoiceTiles(
                        options = PlayerBackdropStrengthOptions.map { it.key to it.labelRes },
                        selected = settings.playerBackdropStrength,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(playerBackdropStrength = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = when (key) {
                                    PlayerStylePreferences.STRENGTH_SUBTLE -> Icons.Outlined.BrightnessLow
                                    PlayerStylePreferences.STRENGTH_VIVID -> Icons.Outlined.BrightnessHigh
                                    else -> Icons.Outlined.BrightnessMedium
                                },
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_seek_bar), colors.primary)
        SettingsCard {
            SeekBarPreviewBox(settings)
            ChoiceGroupLabel(stringResource(R.string.seek_style_title))
            SeekStyleTiles(
                settings = settings,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(seekStyle = it))
                }
            )
            AnimatedVisibility(visible = settings.seekStyle == SeekBarPreferences.STYLE_WAVEFORM) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.seek_wave_shape_title))
                    ChoiceTiles(
                        options = SeekWaveShapeOptions.map { it.key to it.labelRes },
                        selected = settings.seekWaveShape,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(seekWaveShape = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = if (key == SeekBarPreferences.WAVE_MIRRORED) Icons.Outlined.GraphicEq else Icons.Outlined.BarChart,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    ChoiceGroupLabel(stringResource(R.string.seek_wave_density_title))
                    ChoiceTiles(
                        options = SeekWaveDensityOptions.map { it.key to it.labelRes },
                        selected = settings.seekWaveDensity,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(seekWaveDensity = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = if (key == SeekBarPreferences.DENSITY_FINE) Icons.Outlined.DensitySmall else Icons.Outlined.DensityLarge,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    Text(
                        text = stringResource(R.string.seek_wave_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    )
                }
            }
            AnimatedVisibility(visible = settings.seekStyle == SeekBarPreferences.STYLE_SQUIGGLE) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.seek_squiggle_height_title))
                    ChoiceTiles(
                        options = SeekSquiggleHeightOptions.map { it.key to it.labelRes },
                        selected = settings.seekSquiggleHeight,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(seekSquiggleHeight = it))
                        },
                        icon = { key, tint ->
                            WaveSwatch(
                                amplitude = when (key) {
                                    SeekBarPreferences.SIZE_SMALL -> 0.3f
                                    SeekBarPreferences.SIZE_LARGE -> 1f
                                    else -> 0.6f
                                },
                                cycles = 2f,
                                tint = tint
                            )
                        }
                    )
                    ChoiceGroupLabel(stringResource(R.string.seek_squiggle_length_title))
                    ChoiceTiles(
                        options = SeekSquiggleLengthOptions.map { it.key to it.labelRes },
                        selected = settings.seekSquiggleLength,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(seekSquiggleLength = it))
                        },
                        icon = { key, tint ->
                            WaveSwatch(
                                amplitude = 0.6f,
                                cycles = when (key) {
                                    SeekBarPreferences.SIZE_SMALL -> 3.5f
                                    SeekBarPreferences.SIZE_LARGE -> 1f
                                    else -> 2f
                                },
                                tint = tint
                            )
                        }
                    )
                    ChoiceGroupLabel(stringResource(R.string.seek_squiggle_speed_title))
                    ChoiceTiles(
                        options = SeekSquiggleSpeedOptions.map { it.key to it.labelRes },
                        selected = settings.seekSquiggleSpeed,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(seekSquiggleSpeed = it))
                        },
                        icon = { key, tint ->
                            KateIcon(
                                imageVector = when (key) {
                                    SeekBarPreferences.SIZE_SMALL -> Icons.Outlined.SlowMotionVideo
                                    SeekBarPreferences.SIZE_LARGE -> Icons.Outlined.FastForward
                                    else -> Icons.Outlined.Speed
                                },
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.GraphicEq,
                        title = stringResource(R.string.seek_squiggle_reactive_title),
                        subtitle = stringResource(R.string.seek_squiggle_reactive_subtitle),
                        checked = settings.seekSquiggleReactive,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(seekSquiggleReactive = it)) }
                    )
                }
            }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.seek_thumb_title))
            ChoiceTiles(
                options = SeekThumbOptions.map { it.key to it.labelRes },
                selected = settings.seekThumb,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(seekThumb = it))
                },
                icon = { key, tint -> ThumbSwatch(key, tint) }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.seek_thickness_title))
            ChoiceTiles(
                options = SeekThicknessOptions.map { it.key to it.labelRes },
                selected = settings.seekThickness,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(seekThickness = it))
                },
                icon = { key, tint -> ThicknessSwatch(key, tint) }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.seek_color_title))
            ChoiceTiles(
                options = SeekColorOptions.map { it.key to it.labelRes },
                selected = settings.seekColor,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(seekColor = it))
                },
                icon = { key, _ -> SeekColorSwatch(gradient = key == SeekBarPreferences.COLOR_GRADIENT) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.seek_bubble_title),
                subtitle = stringResource(R.string.seek_bubble_subtitle),
                checked = settings.seekBubble,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(seekBubble = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Subtitles,
                title = stringResource(R.string.seek_markers_title),
                subtitle = stringResource(R.string.seek_markers_subtitle),
                checked = settings.seekLyricMarkers,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(seekLyricMarkers = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_buttons), colors.primary)
        SettingsCard {
            PlayerSlotRows(settings, onSettingsChange)
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.FavoriteBorder,
                title = stringResource(R.string.player_show_favorite),
                subtitle = stringResource(R.string.player_show_button_subtitle),
                checked = settings.playerShowFavorite,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerShowFavorite = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Widgets,
                title = stringResource(R.string.player_quick_actions_title),
                subtitle = stringResource(R.string.player_quick_actions_subtitle),
                checked = settings.playerQuickActions,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerQuickActions = it)) }
            )
            AnimatedVisibility(visible = settings.playerQuickActions) {
                Column {
                    RowDivider(colors.divider)
                    QuickActionsEditorRow(settings, onSettingsChange)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_gestures), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.SwipeLeft,
                title = stringResource(R.string.player_swipe_artwork_title),
                subtitle = stringResource(R.string.player_swipe_artwork_subtitle),
                checked = settings.playerSwipeArtwork,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerSwipeArtwork = it)) }
            )
            AnimatedVisibility(visible = settings.playerSwipeArtwork) {
                Column {
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.ViewCarousel,
                        title = stringResource(R.string.player_cover_preview_title),
                        subtitle = stringResource(R.string.player_cover_preview_subtitle),
                        checked = settings.playerCoverPreview,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(playerCoverPreview = it)) }
                    )
                }
            }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.SwipeRight,
                title = stringResource(R.string.mini_swipe_skip_title),
                subtitle = stringResource(R.string.mini_swipe_skip_subtitle),
                checked = settings.miniSwipeSkip,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(miniSwipeSkip = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.SwipeDown,
                title = stringResource(R.string.mini_swipe_dismiss_title),
                subtitle = stringResource(R.string.mini_swipe_dismiss_subtitle),
                checked = settings.miniSwipeDismiss,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(miniSwipeDismiss = it)) }
            )
            RowDivider(colors.divider)
            ActionPickerRow(
                icon = Icons.Outlined.TouchApp,
                title = stringResource(R.string.player_double_tap_title),
                current = settings.playerDoubleTap,
                options = PlayerActions.Gestures,
                onSelect = { onSettingsChange(settings.copy(playerDoubleTap = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Gesture,
                title = stringResource(R.string.double_tap_sides_title),
                subtitle = stringResource(R.string.double_tap_sides_subtitle),
                checked = settings.playerDoubleTapSides,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(playerDoubleTapSides = it)) }
            )
            RowDivider(colors.divider)
            ActionPickerRow(
                icon = Icons.Outlined.TouchApp,
                title = stringResource(R.string.long_press_title),
                current = settings.playerLongPress,
                options = PlayerActions.Gestures,
                onSelect = { onSettingsChange(settings.copy(playerLongPress = it)) }
            )
            RowDivider(colors.divider)
            ActionPickerRow(
                icon = Icons.Outlined.TouchApp,
                title = stringResource(R.string.mini_long_press_title),
                current = settings.miniLongPress,
                options = PlayerActions.Gestures,
                onSelect = { onSettingsChange(settings.copy(miniLongPress = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_lyrics), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.lyrics_background_title))
            ChoiceTiles(
                options = PlayerBackgroundOptions.map { it.key to it.labelRes },
                selected = settings.lyricsBackground,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(lyricsBackground = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            PlayerStylePreferences.BACKGROUND_ARTWORK -> Icons.Outlined.Image
                            PlayerStylePreferences.BACKGROUND_PLAIN -> Icons.Outlined.CheckBoxOutlineBlank
                            PlayerStylePreferences.BACKGROUND_IMAGE -> Icons.Outlined.Wallpaper
                            else -> Icons.Outlined.BubbleChart
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            RowDivider(colors.divider)
            LyricsFontRow(settings, onSettingsChange)
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.lyrics_text_size_title))
            ChoiceTiles(
                options = LyricsTextSizeOptions.map { it.key to it.labelRes },
                selected = settings.lyricsTextSize,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(lyricsTextSize = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(
                            when (key) {
                                LyricsPreferences.TEXT_SIZE_SMALL -> 20.dp
                                LyricsPreferences.TEXT_SIZE_LARGE -> 32.dp
                                else -> 26.dp
                            }
                        )
                    )
                }
            )
            RowDivider(colors.divider)
            LyricsLookExtras(settings, onSettingsChange)
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.lyrics_align_title))
            ChoiceTiles(
                options = LyricsAlignmentOptions.map { it.key to it.labelRes },
                selected = settings.lyricsAlignment,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(lyricsAlignment = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = if (key == LyricsPreferences.ALIGN_CENTER) {
                            Icons.Outlined.FormatAlignCenter
                        } else {
                            Icons.AutoMirrored.Outlined.FormatAlignLeft
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Mic,
                title = stringResource(R.string.lyrics_word_by_word_title),
                subtitle = stringResource(R.string.lyrics_word_by_word_subtitle),
                checked = settings.lyricsWordByWord,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(lyricsWordByWord = it)) }
            )
            if (settings.lyricsWordByWord) {
                RowDivider(colors.divider)
                LyricsWordStyleRow(settings, onSettingsChange)
            }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Gesture,
                title = stringResource(R.string.lyrics_line_sweep_title),
                subtitle = stringResource(R.string.lyrics_line_sweep_subtitle),
                checked = settings.lyricsLineSweep,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(lyricsLineSweep = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.BlurOn,
                title = stringResource(R.string.lyrics_soften_title),
                subtitle = stringResource(R.string.lyrics_soften_subtitle),
                checked = settings.lyricsSoftenLines,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(lyricsSoftenLines = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Fullscreen,
                title = stringResource(R.string.lyrics_immersive_title),
                subtitle = stringResource(R.string.lyrics_immersive_subtitle),
                checked = settings.lyricsImmersive,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(lyricsImmersive = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Lightbulb,
                title = stringResource(R.string.lyrics_keep_screen_on_title),
                subtitle = stringResource(R.string.lyrics_keep_screen_on_subtitle),
                checked = settings.lyricsKeepScreenOn,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(lyricsKeepScreenOn = it)) }
            )
        }
    }
}

@Composable
private fun SeekBarPreviewBox(settings: LookAndFeelSettings) {
    val colors = LocalClintColors.current
    val transition = rememberInfiniteTransition(label = "seek_preview")
    val progress = transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "seek_preview_progress"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        SeekBarPreview(look = settings.seekBarLook(), progress = { progress.value })
    }
}

@Composable
private fun SeekStyleTiles(settings: LookAndFeelSettings, onSelect: (String) -> Unit) {
    val colors = LocalClintColors.current
    val look = settings.seekBarLook()
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        SeekStyleOptions.forEach { option ->
            val selected = option.key == settings.seekStyle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(116.dp)
                    .kateChoice(selected)
                    .clickable { if (!selected) onSelect(option.key) }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                    SeekBarPreview(look = look.copy(style = option.key, lyricMarkers = false), progress = { 0.62f })
                }
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = kateChoiceText(selected),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WaveSwatch(amplitude: Float, cycles: Float, tint: Color) {
    Canvas(modifier = Modifier.size(width = 36.dp, height = 28.dp)) {
        val path = Path()
        val centerY = size.height / 2f
        val maxAmplitude = size.height * 0.38f * amplitude
        var x = 0f
        path.moveTo(0f, centerY)
        while (x <= size.width) {
            val angle = x / size.width * cycles * 2f * Math.PI.toFloat()
            path.lineTo(x, centerY + maxAmplitude * kotlin.math.sin(angle))
            x += 1.dp.toPx()
        }
        drawPath(path = path, color = tint, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ThumbSwatch(key: String, tint: Color) {
    Canvas(modifier = Modifier.size(width = 36.dp, height = 28.dp)) {
        val centerY = size.height / 2f
        drawLine(
            color = tint.copy(alpha = 0.45f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(0f, centerY),
            end = Offset(size.width * 0.6f, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        when (key) {
            SeekBarPreferences.THUMB_CIRCLE -> drawCircle(color = tint, radius = 6.dp.toPx(), center = Offset(size.width * 0.6f, centerY))
            SeekBarPreferences.THUMB_LINE -> drawLine(
                color = tint,
                start = Offset(size.width * 0.6f, centerY - 9.dp.toPx()),
                end = Offset(size.width * 0.6f, centerY + 9.dp.toPx()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ThicknessSwatch(key: String, tint: Color) {
    val stroke = when (key) {
        SeekBarPreferences.SIZE_SMALL -> 2.dp
        SeekBarPreferences.SIZE_LARGE -> 7.dp
        else -> 4.dp
    }
    Canvas(modifier = Modifier.size(width = 36.dp, height = 28.dp)) {
        drawLine(
            color = tint,
            start = Offset(stroke.toPx(), size.height / 2f),
            end = Offset(size.width - stroke.toPx(), size.height / 2f),
            strokeWidth = stroke.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun MiniProgressTiles(selected: String, gradient: Boolean, onSelect: (String) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        MiniProgressStyleOptions.forEach { option ->
            val isSelected = option.key == selected
            val tint = kateChoiceTint(isSelected)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(108.dp)
                    .kateChoice(isSelected)
                    .clickable { if (!isSelected) onSelect(option.key) }
                    .padding(horizontal = 10.dp, vertical = 14.dp)
            ) {
                MiniProgressSwatch(styleKey = option.key, tint = tint, gradient = gradient)
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = kateChoiceText(isSelected),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniProgressSwatch(styleKey: String, tint: Color, gradient: Boolean) {
    val colors = LocalClintColors.current
    val end = remember(tint, gradient) { if (gradient) shiftedHue(tint) else tint }
    Canvas(modifier = Modifier.size(width = 84.dp, height = 34.dp)) {
        val corner = 9.dp.toPx()
        val card = androidx.compose.ui.geometry.Size(size.width, size.height)
        val progress = 0.6f
        val brush = Brush.horizontalGradient(listOf(tint, end), startX = 0f, endX = size.width)
        drawRoundRect(
            color = colors.cardBackground,
            size = card,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
        )
        if (styleKey == MiniProgressPreferences.STYLE_FILL) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(tint.copy(alpha = 0.2f), end.copy(alpha = 0.2f)), startX = 0f, endX = size.width),
                size = androidx.compose.ui.geometry.Size(size.width * progress, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
            )
        }
        val art = 20.dp.toPx()
        val artLeft = 7.dp.toPx()
        val artTop = (size.height - art) / 2f
        drawRoundRect(
            color = tint.copy(alpha = 0.35f),
            topLeft = Offset(artLeft, artTop),
            size = androidx.compose.ui.geometry.Size(art, art),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
        val textLeft = artLeft + art + 6.dp.toPx()
        drawRoundRect(
            color = colors.secondaryText.copy(alpha = 0.5f),
            topLeft = Offset(textLeft, size.height / 2f - 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 3.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawRoundRect(
            color = colors.secondaryText.copy(alpha = 0.3f),
            topLeft = Offset(textLeft, size.height / 2f + 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 3.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        val playCenter = Offset(size.width - 14.dp.toPx(), size.height / 2f)
        val playRadius = 7.dp.toPx()
        drawCircle(color = colors.secondaryText.copy(alpha = 0.45f), radius = 3.dp.toPx(), center = playCenter)
        val stroke = 2.dp.toPx()
        when (styleKey) {
            MiniProgressPreferences.STYLE_BOTTOM, MiniProgressPreferences.STYLE_TOP -> {
                val y = if (styleKey == MiniProgressPreferences.STYLE_TOP) stroke / 2f + 1.dp.toPx() else size.height - stroke / 2f - 1.dp.toPx()
                drawLine(colors.surfaceVariant, Offset(corner, y), Offset(size.width - corner, y), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(brush = brush, start = Offset(corner, y), end = Offset(corner + (size.width - corner * 2f) * progress, y), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            MiniProgressPreferences.STYLE_PLAY_RING -> {
                drawCircle(color = colors.surfaceVariant, radius = playRadius, center = playCenter, style = Stroke(width = stroke))
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(playCenter.x - playRadius, playCenter.y - playRadius),
                    size = androidx.compose.ui.geometry.Size(playRadius * 2f, playRadius * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            MiniProgressPreferences.STYLE_ART_RING -> {
                val outset = 2.5.dp.toPx()
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(artLeft - outset, artTop - outset),
                    size = androidx.compose.ui.geometry.Size(art + outset * 2f, art + outset * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                    style = Stroke(width = stroke)
                )
            }
        }
    }
}

@Composable
private fun SeekColorSwatch(gradient: Boolean) {
    val colors = LocalClintColors.current
    val end = remember(colors.primary) { shiftedHue(colors.primary) }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (gradient) Brush.horizontalGradient(listOf(colors.primary, end)) else Brush.horizontalGradient(listOf(colors.primary, colors.primary))
            )
    )
}

@Composable
private fun PlayerLayoutPicker(selected: String, onSelect: (String) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        PlayerLayoutOptions.forEach { option ->
            val isSelected = option.key == selected
            val tint = kateChoiceTint(isSelected)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(96.dp)
                    .kateChoice(isSelected)
                    .clickable { if (!isSelected) onSelect(option.key) }
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                PlayerLayoutPreview(
                    layout = option.key,
                    tint = tint,
                    muted = colors.secondaryText.copy(alpha = 0.35f),
                    modifier = Modifier.width(64.dp).height(104.dp)
                )
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = kateChoiceText(isSelected),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerLayoutPreview(layout: String, tint: Color, muted: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val line = 3.dp.toPx()
        fun textLine(y: Float, fraction: Float, color: Color = muted, thickness: Float = line) {
            val half = w * fraction / 2f
            drawLine(color, Offset(w / 2f - half, y), Offset(w / 2f + half, y), strokeWidth = thickness, cap = StrokeCap.Round)
        }
        fun controls(y: Float) {
            drawLine(muted, Offset(w * 0.1f, y - h * 0.08f), Offset(w * 0.9f, y - h * 0.08f), strokeWidth = line * 0.7f, cap = StrokeCap.Round)
            drawCircle(tint, radius = w * 0.09f, center = Offset(w / 2f, y))
            drawCircle(muted, radius = w * 0.045f, center = Offset(w * 0.25f, y))
            drawCircle(muted, radius = w * 0.045f, center = Offset(w * 0.75f, y))
        }
        when (layout) {
            PlayerStylePreferences.LAYOUT_BIG_ART -> {
                drawRect(
                    brush = Brush.verticalGradient(listOf(tint.copy(alpha = 0.55f), tint.copy(alpha = 0.3f), Color.Transparent), 0f, h * 0.55f),
                    topLeft = Offset.Zero,
                    size = Size(w, h * 0.55f)
                )
                textLine(h * 0.64f, 0.7f, tint)
                textLine(h * 0.71f, 0.45f)
                controls(h * 0.9f)
            }
            PlayerStylePreferences.LAYOUT_MINIMAL -> {
                drawRoundRect(tint.copy(alpha = 0.45f), Offset(w * 0.38f, h * 0.1f), Size(w * 0.24f, w * 0.24f), CornerRadius(4.dp.toPx()))
                textLine(h * 0.38f, 0.85f, tint, line * 1.8f)
                textLine(h * 0.46f, 0.6f, tint, line * 1.8f)
                textLine(h * 0.55f, 0.4f)
                controls(h * 0.86f)
            }
            PlayerStylePreferences.LAYOUT_VINYL -> {
                val center = Offset(w * 0.46f, h * 0.3f)
                val radius = w * 0.38f
                drawCircle(Color(0xFF121214), radius = radius, center = center)
                for (ring in 1..3) {
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = radius * (0.5f + ring * 0.15f), center = center, style = Stroke(width = 1f))
                }
                drawCircle(tint, radius = radius * 0.36f, center = center)
                drawCircle(Color(0xFF121214), radius = radius * 0.06f, center = center)
                drawLine(muted, Offset(w * 0.92f, h * 0.04f), Offset(w * 0.74f, h * 0.36f), strokeWidth = line * 0.8f, cap = StrokeCap.Round)
                textLine(h * 0.64f, 0.7f, tint)
                textLine(h * 0.71f, 0.45f)
                controls(h * 0.9f)
            }
            PlayerStylePreferences.LAYOUT_CARD -> {
                val side = w * 0.88f
                val left = (w - side) / 2f
                val top = h * 0.05f
                drawRoundRect(tint.copy(alpha = 0.45f), Offset(left, top), Size(side, side), CornerRadius(8.dp.toPx()))
                val pill = line * 2.2f
                drawRoundRect(tint, Offset(left + w * 0.07f, top + side * 0.66f), Size(side * 0.46f, pill), CornerRadius(pill / 2f))
                drawRoundRect(muted, Offset(left + w * 0.07f, top + side * 0.66f + pill * 1.6f), Size(side * 0.62f, pill), CornerRadius(pill / 2f))
                drawCircle(muted, radius = w * 0.07f, center = Offset(left + side - w * 0.12f, top + side - w * 0.12f))
                controls(h * 0.86f)
            }
            PlayerStylePreferences.LAYOUT_CASSETTE -> {
                val top = h * 0.12f
                val height = w * 0.92f / 1.6f
                drawRoundRect(Color(0xFF1F1F22), Offset(w * 0.04f, top), Size(w * 0.92f, height), CornerRadius(4.dp.toPx()))
                drawRoundRect(Color(0xFFF3EAD7), Offset(w * 0.09f, top + height * 0.08f), Size(w * 0.82f, height * 0.5f), CornerRadius(2.dp.toPx()))
                drawRect(tint.copy(alpha = 0.7f), Offset(w * 0.12f, top + height * 0.14f), Size(height * 0.36f, height * 0.36f))
                drawCircle(Color(0xFFEDEDED), radius = w * 0.06f, center = Offset(w * 0.36f, top + height * 0.74f))
                drawCircle(Color(0xFFEDEDED), radius = w * 0.06f, center = Offset(w * 0.64f, top + height * 0.74f))
                textLine(h * 0.64f, 0.7f, tint)
                textLine(h * 0.71f, 0.45f)
                controls(h * 0.9f)
            }
            else -> {
                drawRoundRect(tint.copy(alpha = 0.45f), Offset(w * 0.14f, h * 0.06f), Size(w * 0.72f, w * 0.72f), CornerRadius(8.dp.toPx()))
                textLine(h * 0.64f, 0.7f, tint)
                textLine(h * 0.71f, 0.45f)
                controls(h * 0.9f)
            }
        }
    }
}

@Composable
private fun CassetteColorSwatch(key: String, tint: Color) {
    val colors = LocalClintColors.current
    val fill = when (key) {
        PlayerStylePreferences.CASSETTE_WHITE -> Color(0xFFECECEE)
        PlayerStylePreferences.CASSETTE_CLEAR -> colors.onSurface.copy(alpha = 0.12f)
        else -> Color(0xFF1F1F22)
    }
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(fill)
            .border(BorderStroke(1.5.dp, tint), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF9A9AA0)))
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF9A9AA0)))
        }
    }
}

@Composable
internal fun ChoiceGroupLabel(text: String) {
    val colors = LocalClintColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = colors.onSurface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp).settingsSpotlight(text)
    )
}

@Composable
private fun ChoiceTiles(
    options: List<Pair<String, Int>>,
    selected: String,
    onSelect: (String) -> Unit,
    icon: @Composable (key: String, tint: Color) -> Unit
) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        options.forEach { (key, labelRes) ->
            val isSelected = key == selected
            val tint = kateChoiceTint(isSelected)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .kateChoice(isSelected)
                    .clickable { if (!isSelected) onSelect(key) }
                    .padding(vertical = 18.dp)
            ) {
                icon(key, tint)
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = kateChoiceText(isSelected),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtShapeSwatch(shapeKey: String, tint: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(artPreviewShape(shapeKey, 9.dp))
            .background(tint)
    )
}

private fun artPreviewShape(shapeKey: String, rounded: Dp): Shape = when (shapeKey) {
    PlayerStylePreferences.ART_SHAPE_CIRCLE -> CircleShape
    PlayerStylePreferences.ART_SHAPE_SQUARE -> RoundedCornerShape(2.dp)
    else -> RoundedCornerShape(rounded)
}

@Composable
fun LibraryLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.nav_library),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_library_message),
                onConfirm = {
                    confirmReset = false
                    onSettingsChange(settings.withDefaultLibraryStyle())
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.look_section_artwork), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.library_corners_title))
            ChoiceTiles(
                options = CornerStyleOptions.map { it.key to it.labelRes },
                selected = settings.libraryCorners,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(libraryCorners = it))
                },
                icon = { key, tint ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(LibraryStylePreferences.scaledCorner(8.dp, key)))
                            .background(tint)
                    )
                }
            )
            FineTuneSlider(
                title = stringResource(R.string.fine_tune_corners),
                value = LibraryStylePreferences.cornerScale(settings.libraryCorners),
                range = 0f..LibraryStylePreferences.MAX_CORNER_SCALE,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(libraryCorners = FineTune.key(it))) }
            )
            Text(
                text = stringResource(R.string.library_corners_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_song_details), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.HighQuality,
                title = stringResource(R.string.library_show_badges_title),
                subtitle = stringResource(R.string.library_show_badges_subtitle),
                checked = settings.libraryShowBadges,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(libraryShowBadges = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Timer,
                title = stringResource(R.string.library_show_duration_title),
                subtitle = stringResource(R.string.library_show_duration_subtitle),
                checked = settings.libraryShowDuration,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(libraryShowDuration = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_home), colors.primary)
        SettingsCard {
            HomeOrderRows(settings, onSettingsChange)
        }
        LibraryCategoriesSection(settings, onSettingsChange)
        SongRowsSection(settings, onSettingsChange)
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_scrolling), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.UnfoldMore,
                title = stringResource(R.string.show_scrollbar_title),
                subtitle = stringResource(R.string.show_scrollbar_subtitle),
                checked = settings.showScrollbar,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(showScrollbar = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.SortByAlpha,
                title = stringResource(R.string.library_alphabet_index_title),
                subtitle = stringResource(R.string.library_alphabet_index_subtitle),
                checked = settings.libraryAlphabetIndex,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(libraryAlphabetIndex = it)) }
            )
        }
        ScanFiltersSection()
    }
}

@Composable
fun NavigationLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var confirmReset by remember { mutableStateOf(false) }
    val moveTab: (Int, Int) -> Unit = { from, to ->
        val order = settings.navTabOrder.toMutableList()
        order.add(to, order.removeAt(from))
        haptics.tick()
        onSettingsChange(settings.copy(navTabOrder = order))
    }
    LookAndFeelPage(
        title = stringResource(R.string.look_navigation_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_navigation_message),
                onConfirm = {
                    confirmReset = false
                    onSettingsChange(settings.withDefaultNavigation())
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.look_section_tabs), colors.primary)
        SettingsCard {
            settings.navTabOrder.forEachIndexed { index, tabKey ->
                val option = NavTabOptions.first { it.key == tabKey }
                key(tabKey) {
                    if (index > 0) RowDivider(colors.divider)
                    TabOrderRow(
                        option = option,
                        shown = tabKey in settings.visibleTabs,
                        locked = tabKey in NavigationPreferences.LOCKED_TABS,
                        canMoveUp = index > 0,
                        canMoveDown = index < settings.navTabOrder.lastIndex,
                        onShownChange = { show ->
                            val hidden = if (show) settings.navHiddenTabs - tabKey else settings.navHiddenTabs + tabKey
                            onSettingsChange(settings.copy(navHiddenTabs = hidden))
                        },
                        onMoveUp = { moveTab(index, index - 1) },
                        onMoveDown = { moveTab(index, index + 1) }
                    )
                }
            }
            Text(
                text = stringResource(R.string.nav_tabs_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_tab_labels), colors.primary)
        SettingsCard {
            ChoiceTiles(
                options = NavLabelOptions.map { it.key to it.labelRes },
                selected = settings.navLabels,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(navLabels = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            NavigationPreferences.LABELS_SELECTED -> Icons.Outlined.TouchApp
                            NavigationPreferences.LABELS_NEVER -> Icons.Outlined.HideSource
                            else -> Icons.Outlined.TextFields
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_start_screen), colors.primary)
        SettingsCard {
            val startOptions = settings.visibleTabs
                .filter { it != NavigationPreferences.TAB_SETTINGS }
                .map { key -> NavTabOptions.first { it.key == key } }
            ChoiceTiles(
                options = startOptions.map { it.key to it.labelRes },
                selected = settings.effectiveStartScreen,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(startScreen = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = startOptions.first { it.key == key }.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            Text(
                text = stringResource(R.string.start_screen_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_mini_player), colors.primary)
        SettingsCard {
            ChoiceTiles(
                options = MiniPlayerStyleOptions.map { it.key to it.labelRes },
                selected = settings.miniPlayerStyle,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(miniPlayerStyle = it))
                },
                icon = { key, tint -> MiniPlayerStyleSwatch(docked = key == NavigationPreferences.MINI_PLAYER_DOCKED, tint = tint) }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.mini_progress_title))
            MiniProgressTiles(
                selected = settings.miniProgressStyle,
                gradient = settings.miniProgressColor == SeekBarPreferences.COLOR_GRADIENT,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(miniProgressStyle = it))
                }
            )
            val edgeStyle = settings.miniProgressStyle == MiniProgressPreferences.STYLE_BOTTOM ||
                settings.miniProgressStyle == MiniProgressPreferences.STYLE_TOP
            val strokeStyle = edgeStyle ||
                settings.miniProgressStyle == MiniProgressPreferences.STYLE_PLAY_RING ||
                settings.miniProgressStyle == MiniProgressPreferences.STYLE_ART_RING
            AnimatedVisibility(visible = edgeStyle) {
                Column {
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Link,
                        title = stringResource(R.string.mini_progress_match_title),
                        subtitle = stringResource(R.string.mini_progress_match_subtitle),
                        checked = settings.miniProgressMatchPlayer,
                        colors = colors,
                        onCheckedChange = { onSettingsChange(settings.copy(miniProgressMatchPlayer = it)) }
                    )
                }
            }
            AnimatedVisibility(visible = strokeStyle) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.seek_thickness_title))
                    ChoiceTiles(
                        options = MiniProgressThicknessOptions.map { it.key to it.labelRes },
                        selected = settings.miniProgressThickness,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(miniProgressThickness = it))
                        },
                        icon = { key, tint ->
                            ThicknessSwatch(
                                key = when (key) {
                                    MiniProgressPreferences.THICKNESS_HAIRLINE -> SeekBarPreferences.SIZE_SMALL
                                    MiniProgressPreferences.THICKNESS_THICK -> SeekBarPreferences.SIZE_LARGE
                                    else -> SeekBarPreferences.SIZE_MEDIUM
                                },
                                tint = tint
                            )
                        }
                    )
                }
            }
            AnimatedVisibility(visible = settings.miniProgressStyle != MiniProgressPreferences.STYLE_HIDDEN) {
                Column {
                    RowDivider(colors.divider)
                    ChoiceGroupLabel(stringResource(R.string.seek_color_title))
                    ChoiceTiles(
                        options = SeekColorOptions.map { it.key to it.labelRes },
                        selected = settings.miniProgressColor,
                        onSelect = {
                            haptics.click()
                            onSettingsChange(settings.copy(miniProgressColor = it))
                        },
                        icon = { key, _ -> SeekColorSwatch(gradient = key == SeekBarPreferences.COLOR_GRADIENT) }
                    )
                }
            }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Timer,
                title = stringResource(R.string.mini_time_left_title),
                subtitle = stringResource(R.string.mini_time_left_subtitle),
                checked = settings.miniProgressTimeLeft,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(miniProgressTimeLeft = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_large_screens), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.VerticalSplit,
                title = stringResource(R.string.two_pane_title),
                subtitle = stringResource(R.string.two_pane_subtitle),
                checked = settings.twoPane,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(twoPane = it)) }
            )
        }
    }
}

@Composable
internal fun TabOrderRow(
    option: NavTabOption,
    shown: Boolean,
    locked: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onShownChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (shown) kateTileTint() else buttonTint(colors.secondaryText),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = stringResource(option.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                color = if (shown) colors.onSurface else colors.secondaryText,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    when {
                        locked -> R.string.nav_tab_always_shown
                        shown -> R.string.nav_tab_shown
                        else -> R.string.nav_tab_hidden
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateIconButton(onClick = onMoveUp, enabled = canMoveUp) {
            KateIcon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(R.string.nav_tab_move_up),
                tint = if (canMoveUp) buttonTint(colors.onSurface) else buttonTint(colors.secondaryText).copy(alpha = 0.4f)
            )
        }
        KateIconButton(onClick = onMoveDown, enabled = canMoveDown) {
            KateIcon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.nav_tab_move_down),
                tint = if (canMoveDown) buttonTint(colors.onSurface) else buttonTint(colors.secondaryText).copy(alpha = 0.4f)
            )
        }
        ClintSwitch(
            checked = shown,
            onCheckedChange = {
                haptics.toggle(it)
                onShownChange(it)
            },
            enabled = !locked
        )
    }
}

@Composable
private fun MiniPlayerStyleSwatch(docked: Boolean, tint: Color) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.5.dp, tint), RoundedCornerShape(6.dp))
            .padding(if (docked) 1.5.dp else 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(if (docked) 0.dp else 3.dp))
                .background(tint)
        )
    }
}

@Composable
fun DisplayLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.section_display),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_display_message),
                onConfirm = {
                    confirmReset = false
                    val defaults = LookAndFeelSettings()
                    onSettingsChange(
                        settings.copy(
                            hideStatusBar = defaults.hideStatusBar,
                            hideNavigationBar = defaults.hideNavigationBar,
                            fontStyle = defaults.fontStyle,
                            textSize = defaults.textSize,
                            appBackgroundImage = defaults.appBackgroundImage,
                            imageBlur = defaults.imageBlur,
                            imageDim = defaults.imageDim
                        )
                    )
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.look_section_system_bars), colors.primary)
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.VisibilityOff,
                title = stringResource(R.string.row_hide_status_bar),
                subtitle = stringResource(R.string.row_hide_status_bar_subtitle),
                checked = settings.hideStatusBar,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(hideStatusBar = it)) }
            )
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.VisibilityOff,
                title = stringResource(R.string.row_hide_navigation_bar),
                subtitle = stringResource(R.string.row_hide_navigation_bar_subtitle),
                checked = settings.hideNavigationBar,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(hideNavigationBar = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_text), colors.primary)
        SettingsCard {
            TextPreview(settings)
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.font_style_title))
            FontStylePicker(
                selected = settings.fontStyle,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(fontStyle = it))
                },
                onDelete = { key ->
                    FontStore.delete(key)
                    onSettingsChange(
                        settings.copy(
                            fontStyle = if (settings.fontStyle == key) DisplayPreferences.DEFAULT_FONT_STYLE else settings.fontStyle,
                            lyricsFont = if (settings.lyricsFont == key) LyricsPreferences.FONT_APP else settings.lyricsFont
                        )
                    )
                }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.text_size_title))
            ChoiceTiles(
                options = TextSizeOptions.map { it.key to it.labelRes },
                selected = settings.textSize,
                onSelect = {
                    haptics.click()
                    onSettingsChange(settings.copy(textSize = it))
                },
                icon = { key, tint ->
                    Text(
                        text = "Aa",
                        color = tint,
                        fontWeight = FontWeight.Bold,
                        fontSize = (18f * DisplayPreferences.textScale(key)).sp
                    )
                }
            )
            FineTuneSlider(
                title = stringResource(R.string.fine_tune_text_size),
                value = DisplayPreferences.textScale(settings.textSize),
                range = DisplayPreferences.MIN_TEXT_SCALE..DisplayPreferences.MAX_TEXT_SCALE,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(textSize = FineTune.key(it))) }
            )
        }
        BackgroundImagesSection(settings, onSettingsChange)
        AppIdentitySection()
    }
}

@Composable
private fun TextPreview(settings: LookAndFeelSettings) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = stringResource(R.string.text_preview_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Text(
            text = stringResource(R.string.text_preview_body),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FontStylePicker(selected: String, onSelect: (String) -> Unit, onDelete: (String) -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val failed = stringResource(R.string.font_import_failed)
    var deleting by remember { mutableStateOf<String?>(null) }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val key = FontStore.import(context, uri)
            if (key != null) onSelect(key) else Toast.makeText(context, failed, Toast.LENGTH_SHORT).show()
        }
    }
    val options = FontStyleOptions.map { Triple(it.key, stringResource(it.labelRes), kateFontFamily(it.key)) } +
        FontStore.fonts.map { name -> FontStore.key(name).let { key -> Triple(key, FontStore.label(key), kateFontFamily(key)) } }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        options.forEach { (key, label, family) ->
            val isSelected = key == selected
            val tint = kateChoiceTint(isSelected)
            val imported = FontStore.isFontKey(key)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(84.dp)
                    .kateChoice(isSelected)
                    .combinedClickable(
                        onClick = { if (!isSelected) onSelect(key) },
                        onLongClick = if (imported) ({ deleting = key }) else null
                    )
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = "Aa",
                    fontFamily = family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = tint
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = kateChoiceText(isSelected),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(84.dp)
                .kateChoice(false)
                .clickable { importer.launch(arrayOf("font/ttf", "font/otf", "font/*", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream")) }
                .padding(vertical = 14.dp)
        ) {
            KateIcon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(31.dp))
            Text(
                text = stringResource(R.string.font_add),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
    Text(
        text = stringResource(R.string.font_add_hint),
        style = MaterialTheme.typography.bodySmall,
        color = colors.secondaryText,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
    )
    ConfirmDialog(
        visible = deleting != null,
        title = stringResource(R.string.font_delete_title),
        message = stringResource(R.string.font_delete_message, deleting?.let { FontStore.label(it) }.orEmpty()),
        confirmLabel = stringResource(R.string.font_remove),
        onConfirm = {
            deleting?.let(onDelete)
            deleting = null
        },
        onDismiss = { deleting = null }
    )
}

@Composable
fun MotionLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val view = LocalView.current
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.look_motion_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_motion_message),
                onConfirm = {
                    confirmReset = false
                    val defaults = LookAndFeelSettings()
                    onSettingsChange(settings.copy(haptics = defaults.haptics, animationSpeed = defaults.animationSpeed))
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.look_section_haptics), colors.primary)
        SettingsCard {
            ChoiceTiles(
                options = HapticsOptions.map { it.key to it.labelRes },
                selected = settings.haptics,
                onSelect = {
                    KateHaptics(view, it).confirm()
                    onSettingsChange(settings.copy(haptics = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            KateHaptics.HAPTICS_OFF -> Icons.Outlined.Block
                            KateHaptics.HAPTICS_STRONG -> Icons.Filled.Vibration
                            else -> Icons.Outlined.Vibration
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            Text(
                text = stringResource(R.string.look_haptics_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.look_section_animations), colors.primary)
        SettingsCard {
            ChoiceTiles(
                options = AnimationSpeedOptions.map { it.key to it.labelRes },
                selected = settings.animationSpeed,
                onSelect = {
                    KateHaptics(view, settings.haptics).click()
                    onSettingsChange(settings.copy(animationSpeed = it))
                },
                icon = { key, tint ->
                    KateIcon(
                        imageVector = when (key) {
                            MotionPreferences.SPEED_OFF -> Icons.Outlined.Block
                            MotionPreferences.SPEED_FAST -> Icons.Outlined.FastForward
                            MotionPreferences.SPEED_SLOW -> Icons.Outlined.SlowMotionVideo
                            else -> Icons.Outlined.PlayCircle
                        },
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            Text(
                text = stringResource(R.string.animation_speed_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
    }
}

@Composable
private fun themeSummary(settings: LookAndFeelSettings): String {
    val theme = stringResource(ThemeOptions.first { it.key == settings.theme }.labelRes)
    val colorSource = if (settings.appArtworkColors) {
        stringResource(R.string.look_summary_album_art)
    } else {
        accentLabel(settings.accent)
    }
    val intensity = stringResource(IntensityOptions.first { it.key == settings.intensity }.labelRes)
    return listOf(theme, colorSource, intensity).joinToString(" · ")
}

@Composable
private fun playerSummary(settings: LookAndFeelSettings): String {
    val colorSource = stringResource(
        if (settings.playerFollowsArtwork) R.string.look_player_summary_art else R.string.look_player_summary_accent
    )
    val layout = stringResource(PlayerLayoutOptions.first { it.key == settings.playerLayout }.labelRes)
    val shape = stringResource(ArtShapeOptions.first { it.key == settings.playerArtShape }.labelRes)
    val background = stringResource(PlayerScreenBackgroundOptions.first { it.key == settings.playerBackground }.labelRes)
    return listOf(layout, colorSource, shape, background).joinToString(" · ")
}

@Composable
private fun librarySummary(settings: LookAndFeelSettings): String {
    val corners = stringResource(CornerStyleOptions.firstOrNull { it.key == settings.libraryCorners }?.labelRes ?: R.string.fine_tune_custom)
    val details = stringResource(
        if (settings.libraryShowBadges && settings.libraryShowDuration) R.string.library_details_all else R.string.library_details_fewer
    )
    return listOf(corners, details).joinToString(" · ")
}

private fun displaySummaryRes(settings: LookAndFeelSettings): Int = when {
    settings.hideStatusBar && settings.hideNavigationBar -> R.string.look_display_summary_both
    settings.hideStatusBar -> R.string.look_display_summary_status
    settings.hideNavigationBar -> R.string.look_display_summary_navigation
    else -> R.string.look_display_summary_visible
}

@Composable
internal fun LookAndFeelPage(
    title: String,
    onBack: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    pinned: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalClintColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = buttonTint(colors.onSurface))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = onReset) {
                    KateIcon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = stringResource(R.string.look_reset_action),
                        tint = buttonTint(colors.secondaryText)
                    )
                }
            }
            val bottomPadding = LocalBottomContentPadding.current
            val list: @Composable (Modifier) -> Unit = { listModifier ->
                LazyColumn(
                    modifier = listModifier,
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                            content()
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
            if (pinned == null) {
                list(Modifier.fillMaxSize())
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val sideBySide = maxWidth >= 600.dp && maxWidth > maxHeight
                    if (sideBySide) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = bottomPadding)
                            ) {
                                pinned()
                            }
                            list(Modifier.weight(0.55f).fillMaxHeight())
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                                    pinned()
                                }
                            }
                            list(Modifier.fillMaxWidth().weight(1f))
                        }
                    }
                }
            }
        }
        overlay()
    }
}

@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground),
        content = content
    )
}

@Composable
private fun LookPreviewCard(settings: LookAndFeelSettings) {
    ArtworkColorScope(enabled = settings.playerFollowsArtwork) {
        val colors = LocalClintColors.current
        val track = PlayerController.currentTrack
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.look_preview_label).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.secondaryText
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(artPreviewShape(settings.playerArtShape, 14.dp))
                        .background(colors.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (track != null) {
                        AlbumArt(track = track, modifier = Modifier.fillMaxSize()) {
                            KateIcon(imageVector = Icons.Filled.MusicNote, contentDescription = null, tint = colors.primary)
                        }
                    } else {
                        KateIcon(imageVector = Icons.Filled.MusicNote, contentDescription = null, tint = colors.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(
                        text = track?.title ?: stringResource(R.string.look_preview_no_song),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            track == null -> stringResource(R.string.look_preview_hint)
                            track.artist.isBlank() -> stringResource(R.string.player_unknown_artist)
                            else -> track.artist
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                KatePlayButton(
                    isPlaying = false,
                    onClick = {},
                    baseSize = 44.dp,
                    followSize = false,
                    allowGlow = false,
                    enabled = false
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.primary.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(PreviewProgress)
                        .fillMaxHeight()
                        .background(colors.primary)
                )
            }
        }
    }
}

@Composable
internal fun ResetDefaultsDialog(
    visible: Boolean,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.look_reset_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.look_reset_confirm),
                    role = DialogButtonRole.MAIN,
                    onClick = onConfirm
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSheet(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.cardBackground) {
        MatchAppSystemBars()
        Text(
            text = stringResource(R.string.sheet_select_theme),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOptions.forEach { option ->
                val icon = when (option.key) {
                    "light" -> Icons.Outlined.LightMode
                    "dark" -> Icons.Outlined.DarkMode
                    else -> Icons.Outlined.Brightness6
                }
                val selected = current == option.key
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .kateChoice(selected)
                        .clickable { onSelect(option.key) }
                        .padding(vertical = 18.dp)
                ) {
                    KateIcon(imageVector = icon, contentDescription = null, tint = kateChoiceTint(selected), modifier = Modifier.size(28.dp))
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = kateChoiceText(selected),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccentSheet(
    current: String,
    theme: String,
    intensity: String,
    tuning: ThemeTuning,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val resolvedTheme = if (theme == "light" || theme == "dark") theme else if (colors.isLight) "light" else "dark"
    val swatchColors = remember(resolvedTheme, intensity, tuning) {
        AccentOptions.associate { it.key to resolveClintTheme(context, resolvedTheme, it.key, intensity, tuning = tuning).primary }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.cardBackground) {
        MatchAppSystemBars()
        Text(
            text = stringResource(R.string.sheet_select_accent),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(76.dp),
            modifier = Modifier.fillMaxWidth().height(360.dp).nestedScroll(rememberSheetFlingConnection(sheetState, consumeFling = false)).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AccentOptions) { option ->
                val selected = current == option.key
                val swatch = swatchColors[option.key] ?: colors.primary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(option.key) }
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(swatch),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            KateIcon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) colors.primary else colors.secondaryText,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp).width(72.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntensitySheet(
    current: String,
    tuning: ThemeTuning,
    theme: String,
    accent: String,
    appArtworkColors: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val resolvedTheme = if (theme == "light" || theme == "dark") theme else if (colors.isLight) "light" else "dark"
    val artworkSeed = if (appArtworkColors) ArtworkColors.seed else null
    val previewAccent = if (artworkSeed != null) "artwork" else accent
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.cardBackground) {
        MatchAppSystemBars()
        Text(
            text = stringResource(R.string.sheet_select_surface_intensity),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            IntensityOptions.forEach { option ->
                val selected = current == option.key
                val resolved = remember(resolvedTheme, previewAccent, option.key, artworkSeed, tuning) {
                    resolveClintTheme(context, resolvedTheme, previewAccent, option.key, artworkSeed, tuning)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .kateChoice(selected)
                        .clickable { onSelect(option.key) }
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(resolved.background)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(resolved.surface)
                        )
                    }
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = kateChoiceText(selected),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f).padding(start = 14.dp)
                    )
                    if (selected) {
                        KateIcon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.primary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
