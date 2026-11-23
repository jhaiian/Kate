package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.theme.SwitchStyleOptions
import com.jhaiian.kate.ui.theme.IconFamilyOptions
import com.jhaiian.kate.ui.theme.ChipShapeOptions
import com.jhaiian.kate.ui.theme.ChipSelectedOptions
import com.jhaiian.kate.ui.components.familyIcon
import com.jhaiian.kate.ui.components.choiceShape
import com.jhaiian.kate.ui.components.KateChip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import com.jhaiian.kate.ui.components.kateChoiceTint
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.theme.DialogStyleOptions
import com.jhaiian.kate.ui.theme.DialogLayoutOptions
import com.jhaiian.kate.ui.theme.ActionStyleOptions
import com.jhaiian.kate.ui.theme.ActionShapeOptions
import com.jhaiian.kate.ui.theme.ActionLabelOptions
import com.jhaiian.kate.ui.components.actionShape
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.theme.kateNavBarColor
import com.jhaiian.kate.ui.theme.clintNavigationBarItemColors
import com.jhaiian.kate.ui.theme.TileStyleOptions
import com.jhaiian.kate.ui.theme.TileShapeOptions
import com.jhaiian.kate.ui.theme.NavigationPreferences
import com.jhaiian.kate.ui.theme.NavTabOptions
import com.jhaiian.kate.ui.theme.NavIndicatorOptions
import com.jhaiian.kate.ui.theme.NavBarColorOptions
import com.jhaiian.kate.ui.components.tileShape
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.KateNavIcon
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.layout.WindowInsets
import com.jhaiian.kate.ui.theme.IconButtonStyleOptions
import com.jhaiian.kate.ui.theme.IconButtonSizeOptions
import com.jhaiian.kate.ui.theme.ButtonIconColorOptions
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.KateControlButton
import com.jhaiian.kate.ui.components.KateMiniControls
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.controlsSpacing
import com.jhaiian.kate.ui.components.playButtonShape
import com.jhaiian.kate.ui.components.sideButtonShape
import com.jhaiian.kate.ui.player.shiftedHue
import com.jhaiian.kate.ui.theme.ButtonPreset
import com.jhaiian.kate.ui.theme.ButtonPresets
import com.jhaiian.kate.ui.theme.ButtonStylePreferences
import com.jhaiian.kate.ui.theme.ControlSpacingOptions
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.MiniButtonStyleOptions
import com.jhaiian.kate.ui.theme.MiniButtonsOptions
import com.jhaiian.kate.ui.theme.PlayFillOptions
import com.jhaiian.kate.ui.theme.PlayShapeOptions
import com.jhaiian.kate.ui.theme.PlaySizeOptions
import com.jhaiian.kate.ui.theme.PlayerStyleOption
import com.jhaiian.kate.ui.theme.PressAnimationOptions
import com.jhaiian.kate.ui.theme.SideButtonOptions

@Composable
fun ButtonsLookScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var confirmReset by remember { mutableStateOf(false) }
    val change: (LookAndFeelSettings) -> Unit = {
        haptics.click()
        onSettingsChange(it)
    }
    LookAndFeelPage(
        title = stringResource(R.string.look_buttons_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.look_reset_buttons_message),
                onConfirm = {
                    confirmReset = false
                    onSettingsChange(settings.withDefaultButtons())
                },
                onDismiss = { confirmReset = false }
            )
        },
        pinned = { ButtonsPreviewCard() }
    ) {
        SectionHeader(stringResource(R.string.button_section_presets), colors.primary)
        SettingsCard {
            PresetPicker(
                selected = settings.buttonPreset,
                onSelect = { change(settings.withButtonPreset(it)) }
            )
            Text(
                text = stringResource(R.string.button_preset_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_player), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.button_play_shape_title))
            AdaptiveChoiceTiles(
                options = PlayShapeOptions,
                selected = settings.buttonPlayShape,
                onSelect = { change(settings.copy(buttonPlayShape = it)) }
            ) { key, tint -> ShapeSwatch(key, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_play_size_title))
            AdaptiveChoiceTiles(
                options = PlaySizeOptions,
                selected = settings.buttonPlaySize,
                onSelect = { change(settings.copy(buttonPlaySize = it)) }
            ) { key, tint ->
                Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(
                                when (key) {
                                    ButtonStylePreferences.SIZE_SMALL -> 16.dp
                                    ButtonStylePreferences.SIZE_LARGE -> 28.dp
                                    else -> 22.dp
                                }
                            )
                            .clip(playButtonShape(settings.buttonPlayShape))
                            .background(tint)
                    )
                }
            }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_fill_title))
            AdaptiveChoiceTiles(
                options = PlayFillOptions,
                selected = settings.buttonPlayFill,
                onSelect = { change(settings.copy(buttonPlayFill = it)) }
            ) { key, tint -> FillSwatch(key, settings.buttonPlayShape, tint) }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Lightbulb,
                title = stringResource(R.string.button_glow_title),
                subtitle = stringResource(R.string.button_glow_subtitle),
                checked = settings.buttonPlayGlow,
                colors = colors,
                enabled = settings.buttonPlayShape != ButtonStylePreferences.SHAPE_NONE,
                onCheckedChange = { onSettingsChange(settings.copy(buttonPlayGlow = it)) }
            )
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_side_title))
            AdaptiveChoiceTiles(
                options = SideButtonOptions,
                selected = settings.buttonSideStyle,
                onSelect = { change(settings.copy(buttonSideStyle = it)) }
            ) { key, tint -> SideSwatch(key, settings.buttonPlayShape, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_press_title))
            AdaptiveChoiceTiles(
                options = PressAnimationOptions,
                selected = settings.buttonPress,
                onSelect = { change(settings.copy(buttonPress = it)) }
            ) { key, tint -> PressSwatch(key, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_spacing_title))
            AdaptiveChoiceTiles(
                options = ControlSpacingOptions,
                selected = settings.buttonSpacing,
                onSelect = { change(settings.copy(buttonSpacing = it)) }
            ) { key, tint -> SpacingSwatch(key, tint) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_icons), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.button_icon_color_title))
            AdaptiveChoiceTiles(
                options = ButtonIconColorOptions,
                selected = settings.buttonIconColor,
                onSelect = { change(settings.copy(buttonIconColor = it)) }
            ) { key, tint -> IconColorSwatch(key, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_icon_style_title))
            AdaptiveChoiceTiles(
                options = IconButtonStyleOptions,
                selected = settings.iconButtonStyle,
                onSelect = { change(settings.copy(iconButtonStyle = it)) }
            ) { key, tint -> IconStyleSwatch(key, settings.buttonPlayShape, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_icon_size_title))
            AdaptiveChoiceTiles(
                options = IconButtonSizeOptions,
                selected = settings.iconButtonSize,
                onSelect = { change(settings.copy(iconButtonSize = it)) }
            ) { key, tint ->
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(if (key == ButtonStylePreferences.ICON_LARGE) 30.dp else 22.dp)
                )
            }
            Text(
                text = stringResource(R.string.button_icon_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_tiles), colors.primary)
        SettingsCard {
            TileSampleRow()
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_tile_shape_title))
            AdaptiveChoiceTiles(
                options = TileShapeOptions,
                selected = settings.tileShape,
                onSelect = { change(settings.copy(tileShape = it)) }
            ) { key, tint ->
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(tileShape(key))
                        .background(tint)
                )
            }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_tile_style_title))
            AdaptiveChoiceTiles(
                options = TileStyleOptions,
                selected = settings.tileStyle,
                onSelect = { change(settings.copy(tileStyle = it)) }
            ) { key, tint -> TileStyleSwatch(key, settings.tileShape, tint) }
            RowDivider(colors.divider)
            SettingsSwitchRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                title = stringResource(R.string.button_row_arrows_title),
                subtitle = stringResource(R.string.button_row_arrows_subtitle),
                checked = settings.rowArrows,
                colors = colors,
                onCheckedChange = { onSettingsChange(settings.copy(rowArrows = it)) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_nav), colors.primary)
        SettingsCard {
            NavSampleBar(settings.navLabels)
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_nav_indicator_title))
            AdaptiveChoiceTiles(
                options = NavIndicatorOptions,
                selected = settings.navIndicator,
                onSelect = { change(settings.copy(navIndicator = it)) }
            ) { key, tint -> NavIndicatorSwatch(key, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_nav_bar_color_title))
            AdaptiveChoiceTiles(
                options = NavBarColorOptions,
                selected = settings.navBarColor,
                onSelect = { change(settings.copy(navBarColor = it)) }
            ) { key, tint -> NavBarColorSwatch(key, tint) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_actions), colors.primary)
        SettingsCard {
            ActionSampleRow()
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_action_shape_title))
            AdaptiveChoiceTiles(
                options = ActionShapeOptions,
                selected = settings.actionShape,
                onSelect = { change(settings.copy(actionShape = it)) }
            ) { key, tint ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .clip(actionShape(key))
                        .background(tint)
                )
            }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_action_style_title))
            AdaptiveChoiceTiles(
                options = ActionStyleOptions,
                selected = settings.actionStyle,
                onSelect = { change(settings.copy(actionStyle = it)) }
            ) { key, tint -> ActionStyleSwatch(key, settings.actionShape, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_action_labels_title))
            AdaptiveChoiceTiles(
                options = ActionLabelOptions,
                selected = settings.actionLabels,
                onSelect = { change(settings.copy(actionLabels = it)) }
            ) { key, tint ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KateIcon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                    if (key == ButtonStylePreferences.LABELS_ICON_TEXT) {
                        Box(
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .size(width = 18.dp, height = 5.dp)
                                .clip(CircleShape)
                                .background(tint)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_dialogs), colors.primary)
        SettingsCard {
            DialogSample()
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_dialog_style_title))
            AdaptiveChoiceTiles(
                options = DialogStyleOptions,
                selected = settings.dialogStyle,
                onSelect = { change(settings.copy(dialogStyle = it)) }
            ) { key, tint -> DialogStyleSwatch(key, settings.actionShape, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_dialog_layout_title))
            AdaptiveChoiceTiles(
                options = DialogLayoutOptions,
                selected = settings.dialogLayout,
                onSelect = { change(settings.copy(dialogLayout = it)) }
            ) { key, tint -> DialogLayoutSwatch(key, tint) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_chips), colors.primary)
        SettingsCard {
            ChipSampleRow()
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_chip_shape_title))
            AdaptiveChoiceTiles(
                options = ChipShapeOptions,
                selected = settings.chipShape,
                onSelect = { change(settings.copy(chipShape = it)) }
            ) { key, tint ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 22.dp)
                        .clip(choiceShape(key, true))
                        .border(BorderStroke(2.dp, tint), choiceShape(key, true))
                )
            }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_chip_selected_title))
            AdaptiveChoiceTiles(
                options = ChipSelectedOptions,
                selected = settings.chipSelected,
                onSelect = { change(settings.copy(chipSelected = it)) }
            ) { key, tint ->
                MiniPill(
                    fill = when (key) {
                        ButtonStylePreferences.CHIP_SOLID -> "filled"
                        ButtonStylePreferences.CHIP_OUTLINE -> "outlined"
                        else -> "soft"
                    },
                    shapeKey = ButtonStylePreferences.ACTION_PILL,
                    tint = tint,
                    width = 36.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_switches), colors.primary)
        SettingsCard {
            SwitchSampleRow()
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_switch_style_title))
            AdaptiveChoiceTiles(
                options = SwitchStyleOptions,
                selected = settings.switchStyle,
                onSelect = { change(settings.copy(switchStyle = it)) }
            ) { key, tint -> SwitchStyleSwatch(key, tint) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_icon_family), colors.primary)
        SettingsCard {
            AdaptiveChoiceTiles(
                options = IconFamilyOptions,
                selected = settings.iconFamily,
                onSelect = { change(settings.copy(iconFamily = it)) }
            ) { key, tint ->
                Icon(
                    imageVector = if (key == ButtonStylePreferences.FAMILY_AUTO) {
                        Icons.Outlined.Settings
                    } else {
                        familyIcon(Icons.Filled.Settings, key)
                    },
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = stringResource(R.string.button_family_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.button_section_mini), colors.primary)
        SettingsCard {
            ChoiceGroupLabel(stringResource(R.string.button_mini_style_title))
            AdaptiveChoiceTiles(
                options = MiniButtonStyleOptions,
                selected = settings.miniButtonStyle,
                onSelect = { change(settings.copy(miniButtonStyle = it)) }
            ) { key, tint -> MiniStyleSwatch(key, settings.buttonPlayShape, tint) }
            RowDivider(colors.divider)
            ChoiceGroupLabel(stringResource(R.string.button_mini_buttons_title))
            AdaptiveChoiceTiles(
                options = MiniButtonsOptions,
                selected = settings.miniButtons,
                onSelect = { change(settings.copy(miniButtons = it)) }
            ) { key, tint -> MiniButtonsSwatch(key, tint) }
        }
    }
}

@Composable
internal fun buttonsSummary(settings: LookAndFeelSettings): String {
    val preset = ButtonPresets.firstOrNull { it.key == settings.buttonPreset }
    val presetLabel = stringResource(preset?.labelRes ?: R.string.button_preset_custom)
    val miniLabel = stringResource(MiniButtonsOptions.first { it.key == settings.miniButtons }.labelRes)
    return listOf(presetLabel, miniLabel).joinToString(" · ")
}

@Composable
private fun ButtonsPreviewCard() {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val track = PlayerController.currentTrack
    var playing by remember { mutableStateOf(true) }
    var shuffle by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    val togglePlay = {
        haptics.click()
        playing = !playing
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.look_preview_label).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            KateIconButton(onClick = { haptics.click() }) {
                KateIcon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = buttonTint(colors.onSurface))
            }
            KateIconButton(onClick = {
                haptics.toggle(!liked)
                liked = !liked
            }) {
                KateIcon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (liked) colors.primary else buttonTint(colors.onSurface)
                )
            }
            KateIconButton(onClick = { haptics.click() }) {
                KateIcon(imageVector = Icons.Filled.MoreVert, contentDescription = null, tint = buttonTint(colors.onSurface))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant)
                .padding(start = 9.dp, end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
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
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 4.dp)) {
                Text(
                    text = track?.title ?: stringResource(R.string.button_preview_song),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.button_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            KateMiniControls(
                isPlaying = playing,
                onPlayPause = togglePlay,
                onNext = { haptics.click() },
                onPrevious = { haptics.click() }
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            val showExtras = maxWidth >= 290.dp
            val spacing = controlsSpacing(LocalLookAndFeel.current.buttonSpacing)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (showExtras) spacing.arrangement else Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().height(96.dp)
            ) {
                if (showExtras) {
                    KateControlButton(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = stringResource(R.string.player_shuffle),
                        tint = if (shuffle) colors.primary else colors.secondaryText,
                        iconSize = 22.dp,
                        highlighted = shuffle,
                        onClick = {
                            haptics.toggle(!shuffle)
                            shuffle = !shuffle
                        }
                    )
                }
                KateControlButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(R.string.player_previous),
                    onClick = { haptics.click() }
                )
                KatePlayButton(isPlaying = playing, onClick = togglePlay)
                KateControlButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = stringResource(R.string.player_next),
                    onClick = { haptics.click() }
                )
                if (showExtras) {
                    KateControlButton(
                        icon = Icons.Filled.Repeat,
                        contentDescription = stringResource(R.string.player_repeat),
                        tint = if (repeat) colors.primary else colors.secondaryText,
                        iconSize = 22.dp,
                        highlighted = repeat,
                        onClick = {
                            haptics.click()
                            repeat = !repeat
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetPicker(selected: String, onSelect: (ButtonPreset) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        ButtonPresets.forEach { preset ->
            val isSelected = preset.key == selected
            PickerTile(
                label = stringResource(preset.labelRes),
                isSelected = isSelected,
                onClick = { if (!isSelected) onSelect(preset) },
                modifier = Modifier.width(92.dp)
            ) {
                PresetSwatch(preset, colors.primary, colors.onPrimary)
            }
        }
        if (selected == ButtonStylePreferences.PRESET_CUSTOM) {
            PickerTile(
                label = stringResource(R.string.button_preset_custom),
                isSelected = true,
                onClick = {},
                modifier = Modifier.width(92.dp)
            ) {
                Box(modifier = Modifier.height(38.dp), contentAlignment = Alignment.Center) {
                    KateIcon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetSwatch(preset: ButtonPreset, tint: Color, onTint: Color) {
    val sideShape = sideButtonShape(preset.playShape)
    val gap = when (preset.spacing) {
        ButtonStylePreferences.SPACING_TIGHT -> 2.dp
        ButtonStylePreferences.SPACING_WIDE -> 8.dp
        else -> 5.dp
    }
    val playSize = if (preset.playSize == ButtonStylePreferences.SIZE_LARGE) 38.dp else 32.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap),
        modifier = Modifier.height(38.dp)
    ) {
        SideDot(preset.sideStyle, sideShape, tint)
        PlaySwatch(preset.playShape, preset.playFill, tint, onTint, playSize)
        SideDot(preset.sideStyle, sideShape, tint)
    }
}

@Composable
private fun SideDot(style: String, shape: Shape, tint: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(18.dp)
            .clip(shape)
            .then(
                when (style) {
                    ButtonStylePreferences.SIDE_SOFT -> Modifier.background(tint.copy(alpha = 0.18f))
                    ButtonStylePreferences.SIDE_OUTLINED -> Modifier.border(BorderStroke(1.dp, tint.copy(alpha = 0.6f)), shape)
                    else -> Modifier
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun PlaySwatch(shapeKey: String, fill: String, tint: Color, onTint: Color, size: Dp) {
    val shape = playButtonShape(shapeKey)
    val bare = shapeKey == ButtonStylePreferences.SHAPE_NONE
    val solid = fill == ButtonStylePreferences.FILL_SOLID || fill == ButtonStylePreferences.FILL_GRADIENT
    val gradientEnd = remember(tint) { shiftedHue(tint) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(shape)
            .then(
                when {
                    bare -> Modifier
                    fill == ButtonStylePreferences.FILL_GRADIENT -> Modifier.background(Brush.linearGradient(listOf(tint, gradientEnd)))
                    fill == ButtonStylePreferences.FILL_SOLID -> Modifier.background(tint)
                    fill == ButtonStylePreferences.FILL_SOFT -> Modifier.background(tint.copy(alpha = 0.2f))
                    else -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = if (solid && !bare) onTint else tint,
            modifier = Modifier.size(if (bare) size * 0.8f else size * 0.6f)
        )
    }
}

@Composable
private fun PickerTile(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    swatch: @Composable () -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .kateChoice(isSelected)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        swatch()
        Text(
            text = label,
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

@Composable
private fun AdaptiveChoiceTiles(
    options: List<PlayerStyleOption>,
    selected: String,
    onSelect: (String) -> Unit,
    icon: @Composable (key: String, tint: Color) -> Unit
) {
    val colors = LocalClintColors.current
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val count = options.size.coerceAtLeast(1)
        val tileWidth = (maxWidth - 24.dp - 10.dp * (count - 1)) / count
        val fits = tileWidth >= 76.dp
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fits) Modifier else Modifier.horizontalScroll(rememberScrollState()))
                .padding(12.dp)
        ) {
            options.forEach { option ->
                val isSelected = option.key == selected
                PickerTile(
                    label = stringResource(option.labelRes),
                    isSelected = isSelected,
                    onClick = { if (!isSelected) onSelect(option.key) },
                    modifier = if (fits) Modifier.weight(1f) else Modifier.width(84.dp)
                ) {
                    Box(modifier = Modifier.height(30.dp), contentAlignment = Alignment.Center) {
                        icon(option.key, kateChoiceTint(isSelected))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeSwatch(key: String, tint: Color) {
    if (key == ButtonStylePreferences.SHAPE_NONE) {
        KateIcon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
    } else {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(playButtonShape(key))
                .background(tint)
        )
    }
}

@Composable
private fun FillSwatch(key: String, shapeKey: String, tint: Color) {
    val colors = LocalClintColors.current
    val shapeForSwatch = if (shapeKey == ButtonStylePreferences.SHAPE_NONE) ButtonStylePreferences.SHAPE_CIRCLE else shapeKey
    PlaySwatch(shapeForSwatch, key, tint, colors.cardBackground, 28.dp)
}

@Composable
private fun SideSwatch(key: String, shapeKey: String, tint: Color) {
    val shape = if (key == ButtonStylePreferences.SIDE_PLAIN) CircleShape else sideButtonShape(shapeKey)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(shape)
            .then(
                when (key) {
                    ButtonStylePreferences.SIDE_SOFT -> Modifier.background(tint.copy(alpha = 0.18f))
                    ButtonStylePreferences.SIDE_OUTLINED -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                    else -> Modifier
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (key == ButtonStylePreferences.SIDE_PLAIN) 26.dp else 18.dp)
        )
    }
}

@Composable
private fun PressSwatch(key: String, tint: Color) {
    val transition = rememberInfiniteTransition(label = "press_demo")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1500, easing = LinearEasing)),
        label = "press_demo_phase"
    )
    Canvas(modifier = Modifier.size(30.dp)) {
        val outer = size.minDimension / 2f
        val base = outer * 0.6f
        val scale = when (key) {
            ButtonStylePreferences.PRESS_BOUNCE -> bounceCurve(phase)
            ButtonStylePreferences.PRESS_SHRINK -> shrinkCurve(phase)
            else -> 1f
        }
        if (key == ButtonStylePreferences.PRESS_RIPPLE) {
            val spread = (phase / 0.7f).coerceIn(0f, 1f)
            drawCircle(
                color = tint.copy(alpha = (1f - spread) * 0.6f),
                radius = base + (outer - base) * spread,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        drawCircle(color = tint, radius = base * scale)
    }
}

private fun bounceCurve(t: Float): Float = when {
    t < 0.18f -> 1f - 0.24f * (t / 0.18f)
    t < 0.4f -> 0.76f + 0.38f * ((t - 0.18f) / 0.22f)
    t < 0.55f -> 1.14f - 0.14f * ((t - 0.4f) / 0.15f)
    else -> 1f
}

private fun shrinkCurve(t: Float): Float = when {
    t < 0.22f -> 1f - 0.2f * (t / 0.22f)
    t < 0.5f -> 0.8f + 0.2f * ((t - 0.22f) / 0.28f)
    else -> 1f
}

@Composable
private fun SpacingSwatch(key: String, tint: Color) {
    val gap = when (key) {
        ButtonStylePreferences.SPACING_TIGHT -> 2.dp
        ButtonStylePreferences.SPACING_WIDE -> 11.dp
        else -> 6.dp
    }
    Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(tint))
    }
}

@Composable
private fun MiniStyleSwatch(key: String, shapeKey: String, tint: Color) {
    val colors = LocalClintColors.current
    val shape = sideButtonShape(shapeKey)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(if (key == ButtonStylePreferences.MINI_PLAIN) CircleShape else shape)
            .then(
                when (key) {
                    ButtonStylePreferences.MINI_FILLED -> Modifier.background(tint)
                    ButtonStylePreferences.MINI_OUTLINED -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                    else -> Modifier
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = if (key == ButtonStylePreferences.MINI_FILLED) colors.cardBackground else tint,
            modifier = Modifier.size(if (key == ButtonStylePreferences.MINI_PLAIN) 26.dp else 18.dp)
        )
    }
}

@Composable
private fun MiniButtonsSwatch(key: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (key == ButtonStylePreferences.MINI_BUTTONS_ALL) {
            KateIcon(imageVector = Icons.Filled.SkipPrevious, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        KateIcon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        if (key != ButtonStylePreferences.MINI_BUTTONS_PLAY) {
            KateIcon(imageVector = Icons.Filled.SkipNext, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun IconColorSwatch(key: String, tint: Color) {
    val colors = LocalClintColors.current
    Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    if (key == ButtonStylePreferences.COLOR_ACCENT) {
                        colors.primary
                    } else {
                        colors.onSurface
                    }
                )
                .border(BorderStroke(2.dp, tint), CircleShape)
        )
    }
}

@Composable
private fun IconStyleSwatch(key: String, shapeKey: String, tint: Color) {
    val shape = if (key == ButtonStylePreferences.ICON_PLAIN) CircleShape else sideButtonShape(shapeKey)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(shape)
            .then(
                when (key) {
                    ButtonStylePreferences.ICON_SOFT -> Modifier.background(tint.copy(alpha = 0.18f))
                    ButtonStylePreferences.ICON_OUTLINED -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                    else -> Modifier
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (key == ButtonStylePreferences.ICON_PLAIN) 24.dp else 18.dp)
        )
    }
}

@Composable
private fun TileSampleRow() {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(modifier = Modifier.kateIconTile(40.dp), contentAlignment = Alignment.Center) {
            KateIcon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = kateTileTint(),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                text = stringResource(R.string.library_category_songs),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.button_tile_sample_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        KateRowArrow()
    }
}

@Composable
private fun TileStyleSwatch(key: String, shapeKey: String, tint: Color) {
    val colors = LocalClintColors.current
    val shape = tileShape(shapeKey)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(shape)
            .then(
                when (key) {
                    ButtonStylePreferences.TILE_SOLID -> Modifier.background(tint)
                    ButtonStylePreferences.TILE_OUTLINED -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                    ButtonStylePreferences.TILE_NONE -> Modifier
                    else -> Modifier.background(tint.copy(alpha = 0.2f))
                }
            )
    ) {
        KateIcon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = if (key == ButtonStylePreferences.TILE_SOLID) colors.cardBackground else tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun NavSampleBar(labels: String) {
    var selected by remember { mutableStateOf(0) }
    val haptics = LocalKateHaptics.current
    NavigationBar(
        containerColor = kateNavBarColor(),
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        NavTabOptions.forEachIndexed { index, option ->
            val isSelected = index == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    haptics.click()
                    selected = index
                },
                colors = clintNavigationBarItemColors(),
                icon = {
                    KateNavIcon(
                        selected = isSelected,
                        icon = option.icon,
                        contentDescription = stringResource(option.labelRes)
                    )
                },
                label = if (labels == NavigationPreferences.LABELS_NEVER) {
                    null
                } else {
                    { Text(stringResource(option.labelRes), maxLines = 1) }
                },
                alwaysShowLabel = labels == NavigationPreferences.LABELS_ALWAYS
            )
        }
    }
}

@Composable
private fun NavIndicatorSwatch(key: String, tint: Color) {
    Box(modifier = Modifier.size(width = 40.dp, height = 30.dp), contentAlignment = Alignment.Center) {
        if (key == ButtonStylePreferences.NAV_PILL) {
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 22.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.2f))
            )
        }
        KateIcon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        if (key == ButtonStylePreferences.NAV_DOT) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
        }
    }
}

@Composable
private fun NavBarColorSwatch(key: String, tint: Color) {
    val colors = LocalClintColors.current
    val fill = when (key) {
        ButtonStylePreferences.NAV_BAR_BACKGROUND -> colors.background
        ButtonStylePreferences.NAV_BAR_TINTED -> colors.primary.copy(alpha = 0.12f).compositeOver(colors.cardBackground)
        else -> colors.cardBackground
    }
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(3.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fill)
                .border(BorderStroke(1.dp, tint), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ActionSampleRow() {
    val haptics = LocalKateHaptics.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        KateActionButton(
            label = stringResource(R.string.library_play),
            icon = Icons.Filled.PlayArrow,
            primary = true,
            onClick = { haptics.click() },
            modifier = Modifier.weight(1f)
        )
        KateActionButton(
            label = stringResource(R.string.library_shuffle),
            icon = Icons.Filled.Shuffle,
            primary = false,
            onClick = { haptics.click() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DialogSample() {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceVariant)
    ) {
        Text(
            text = stringResource(R.string.button_dialog_sample_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = stringResource(R.string.button_dialog_sample_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
            KateTextButton(
                label = stringResource(R.string.action_cancel),
                role = DialogButtonRole.SECONDARY,
                onClick = { haptics.click() }
            )
            KateTextButton(
                label = stringResource(R.string.button_dialog_sample_ok),
                role = DialogButtonRole.MAIN,
                onClick = { haptics.confirm() }
            )
        }
    }
}

@Composable
private fun MiniPill(fill: String, shapeKey: String, tint: Color, width: Dp = 22.dp) {
    val shape = actionShape(shapeKey)
    Box(
        modifier = Modifier
            .size(width = width, height = 14.dp)
            .clip(shape)
            .then(
                when (fill) {
                    "filled" -> Modifier.background(tint)
                    "soft" -> Modifier.background(tint.copy(alpha = 0.25f))
                    "outlined" -> Modifier.border(BorderStroke(1.5.dp, tint), shape)
                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (fill == "text") {
            Box(modifier = Modifier.size(width = width * 0.6f, height = 3.dp).clip(CircleShape).background(tint))
        }
    }
}

@Composable
private fun ActionStyleSwatch(key: String, shapeKey: String, tint: Color) {
    val (first, second) = when (key) {
        ButtonStylePreferences.ACTION_FILLED -> "filled" to "filled"
        ButtonStylePreferences.ACTION_OUTLINED -> "outlined" to "outlined"
        ButtonStylePreferences.ACTION_SOFT -> "soft" to "soft"
        else -> "filled" to "soft"
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        MiniPill(first, shapeKey, tint)
        MiniPill(second, shapeKey, tint)
    }
}

@Composable
private fun DialogStyleSwatch(key: String, shapeKey: String, tint: Color) {
    val (first, second) = when (key) {
        ButtonStylePreferences.DIALOG_FILLED_MAIN -> "text" to "filled"
        ButtonStylePreferences.DIALOG_FILLED -> "soft" to "filled"
        ButtonStylePreferences.DIALOG_OUTLINED -> "outlined" to "outlined"
        else -> "text" to "text"
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        MiniPill(first, shapeKey, tint)
        MiniPill(second, shapeKey, tint)
    }
}

@Composable
private fun DialogLayoutSwatch(key: String, tint: Color) {
    Box(modifier = Modifier.size(width = 44.dp, height = 30.dp), contentAlignment = Alignment.Center) {
        when (key) {
            ButtonStylePreferences.DIALOG_STACKED -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(width = 40.dp, height = 9.dp).clip(CircleShape).background(tint))
                Box(modifier = Modifier.size(width = 40.dp, height = 9.dp).clip(CircleShape).background(tint.copy(alpha = 0.35f)))
            }
            ButtonStylePreferences.DIALOG_FULL -> Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(width = 19.dp, height = 10.dp).clip(CircleShape).background(tint.copy(alpha = 0.35f)))
                Box(modifier = Modifier.size(width = 19.dp, height = 10.dp).clip(CircleShape).background(tint))
            }
            else -> Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(width = 12.dp, height = 10.dp).clip(CircleShape).background(tint.copy(alpha = 0.35f)))
                Box(modifier = Modifier.size(width = 12.dp, height = 10.dp).clip(CircleShape).background(tint))
            }
        }
    }
}

@Composable
private fun ChipSampleRow() {
    val haptics = LocalKateHaptics.current
    var picked by remember { mutableStateOf(0) }
    val labels = listOf(
        stringResource(R.string.library_category_albums),
        stringResource(R.string.library_category_artists),
        stringResource(R.string.library_category_genres)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        labels.forEachIndexed { index, label ->
            KateChip(
                label = label,
                selected = index == picked,
                onClick = {
                    haptics.click()
                    picked = index
                }
            )
        }
    }
}

@Composable
private fun SwitchSampleRow() {
    val colors = LocalClintColors.current
    var on by remember { mutableStateOf(true) }
    SettingsSwitchRow(
        icon = Icons.Outlined.Lightbulb,
        title = stringResource(R.string.button_switch_sample_title),
        subtitle = stringResource(R.string.button_switch_sample_subtitle),
        checked = on,
        colors = colors,
        onCheckedChange = { on = it }
    )
}

@Composable
private fun SwitchStyleSwatch(key: String, tint: Color) {
    Box(modifier = Modifier.size(width = 40.dp, height = 30.dp), contentAlignment = Alignment.Center) {
        when (key) {
            ButtonStylePreferences.SWITCH_CHECKBOX -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(tint)
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = LocalClintColors.current.cardBackground, modifier = Modifier.size(14.dp))
            }
            ButtonStylePreferences.SWITCH_MINIMAL -> Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .size(width = 34.dp, height = 18.dp)
                    .clip(CircleShape)
                    .background(tint)
                    .padding(3.dp)
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(LocalClintColors.current.cardBackground))
            }
            else -> Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .size(width = 38.dp, height = 22.dp)
                    .clip(CircleShape)
                    .background(tint)
                    .padding(3.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(16.dp).clip(CircleShape).background(LocalClintColors.current.cardBackground)
                ) {
                    if (key == ButtonStylePreferences.SWITCH_ICONS) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = tint, modifier = Modifier.size(11.dp))
                    }
                }
            }
        }
    }
}
