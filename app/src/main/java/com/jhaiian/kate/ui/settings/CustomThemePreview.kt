package com.jhaiian.kate.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateControlButton
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateMiniControls
import com.jhaiian.kate.ui.components.KateNavIcon
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.library.CategoryRow
import com.jhaiian.kate.ui.library.LibraryActionButton
import com.jhaiian.kate.ui.player.MiniArtSize
import com.jhaiian.kate.ui.player.MiniArtStart
import com.jhaiian.kate.ui.player.MiniPlayerRowHeight
import com.jhaiian.kate.ui.player.SeekBarPreview
import com.jhaiian.kate.ui.player.formatPlaybackTime
import com.jhaiian.kate.ui.player.seekBarLook
import com.jhaiian.kate.ui.theme.CustomPalette
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.ProvideClintColors
import com.jhaiian.kate.ui.theme.ThemeSlot
import com.jhaiian.kate.ui.theme.clintNavigationBarItemColors
import com.jhaiian.kate.ui.theme.contrastRatio
import com.jhaiian.kate.ui.theme.kateNavBarColor
import com.jhaiian.kate.ui.theme.toClintColors
import java.util.Locale

private class ContrastPair(val labelRes: Int, val foreground: ThemeSlot, val background: ThemeSlot)

private val ContrastPairs = listOf(
    ContrastPair(R.string.custom_theme_contrast_text_background, ThemeSlot.ON_SURFACE, ThemeSlot.BACKGROUND),
    ContrastPair(R.string.custom_theme_contrast_secondary_background, ThemeSlot.SECONDARY_TEXT, ThemeSlot.BACKGROUND),
    ContrastPair(R.string.custom_theme_contrast_text_card, ThemeSlot.ON_SURFACE, ThemeSlot.CARD_BACKGROUND),
    ContrastPair(R.string.custom_theme_contrast_on_primary, ThemeSlot.ON_PRIMARY, ThemeSlot.PRIMARY),
    ContrastPair(R.string.custom_theme_contrast_button, ThemeSlot.BUTTON_TEXT, ThemeSlot.BUTTON_BACKGROUND),
    ContrastPair(R.string.custom_theme_contrast_popup, ThemeSlot.POPUP_TEXT, ThemeSlot.POPUP_BACKGROUND),
    ContrastPair(R.string.custom_theme_contrast_error, ThemeSlot.ON_ERROR_CONTAINER, ThemeSlot.ERROR_CONTAINER)
)

private enum class PreviewScreen(val labelRes: Int) {
    LIBRARY(R.string.nav_library),
    PLAYER(R.string.custom_theme_preview_screen_player),
    MENU(R.string.custom_theme_preview_screen_menu),
    SETTINGS(R.string.nav_settings),
    DIALOG(R.string.custom_theme_preview_screen_dialog),
    ERROR(R.string.custom_theme_preview_screen_error)
}

private class PreviewDestination(val labelRes: Int, val selected: ImageVector, val unselected: ImageVector)

private val PreviewDestinations = listOf(
    PreviewDestination(R.string.nav_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    PreviewDestination(R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search),
    PreviewDestination(R.string.nav_stats, Icons.Filled.Insights, Icons.Outlined.Insights),
    PreviewDestination(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
internal fun ThemePreviewPage(palette: CustomPalette, isLight: Boolean, modifier: Modifier = Modifier) {
    val appColors = LocalClintColors.current
    var screenIndex by remember { mutableIntStateOf(0) }
    val draft = remember(palette, isLight) { palette.toClintColors(isLight) }
    val labels = PreviewScreen.entries.map { stringResource(it.labelRes) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.custom_theme_preview_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.onSurface
        )
        Text(
            text = stringResource(R.string.custom_theme_preview_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = appColors.secondaryText,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )
        PreviewTabs(options = labels, selected = screenIndex, onSelect = { screenIndex = it })
        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            ProvideClintColors(draft) {
                PhoneFrame(PreviewScreen.entries[screenIndex], appColors.divider)
            }
        }
        PreviewSectionTitle(R.string.custom_theme_palette_title, null)
        PaletteGrid(palette)
        PreviewSectionTitle(R.string.custom_theme_contrast_title, R.string.custom_theme_contrast_subtitle)
        ContrastCard(palette)
    }
}

@Composable
private fun PreviewTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .horizontalScroll(rememberScrollState())
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primary else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onPrimary else colors.secondaryText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PreviewSectionTitle(titleRes: Int, subtitleRes: Int?) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        if (subtitleRes != null) {
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PhoneFrame(screen: PreviewScreen, borderColor: Color) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 340.dp)
            .height(720.dp)
            .clip(shape)
            .border(2.dp, borderColor, shape)
    ) {
        when (screen) {
            PreviewScreen.LIBRARY -> LibraryScreenMock(showMenu = false)
            PreviewScreen.MENU -> LibraryScreenMock(showMenu = true)
            PreviewScreen.PLAYER -> PlayerScreenMock()
            PreviewScreen.SETTINGS -> SettingsScreenMock(showDialog = false)
            PreviewScreen.DIALOG -> SettingsScreenMock(showDialog = true)
            PreviewScreen.ERROR -> ErrorScreenMock()
        }
    }
}

@Composable
private fun PreviewNavBar(selectedIndex: Int) {
    NavigationBar(containerColor = kateNavBarColor(), windowInsets = WindowInsets(0, 0, 0, 0)) {
        PreviewDestinations.forEachIndexed { index, destination ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = {},
                colors = clintNavigationBarItemColors(),
                icon = {
                    KateNavIcon(
                        selected = selected,
                        icon = if (selected) destination.selected else destination.unselected,
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
private fun PreviewMiniPlayer() {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(MiniPlayerRowHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.cardBackground)
            .padding(start = MiniArtStart, end = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MiniArtSize)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.surfaceVariant)))
        ) {
            KateIcon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = stringResource(R.string.custom_theme_preview_song),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.custom_theme_preview_artist),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateMiniControls(isPlaying = true, onPlayPause = {}, onNext = {}, onPrevious = {})
    }
}

@Composable
private fun PreviewScrim(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
    ) {
        content()
    }
}

@Composable
private fun LibraryScreenMock(showMenu: Boolean) {
    val colors = LocalClintColors.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
                ) {
                    KateIcon(
                        imageVector = Icons.Outlined.LibraryMusic,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Box(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                        Text(
                            text = stringResource(R.string.nav_library),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    LibraryActionButton(
                        icon = Icons.Filled.Shuffle,
                        label = stringResource(R.string.library_shuffle),
                        onClick = {}
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    LibraryActionButton(
                        icon = Icons.Filled.PlayArrow,
                        label = stringResource(R.string.library_play),
                        primary = true,
                        onClick = {}
                    )
                }
                Text(
                    text = pluralStringResource(R.plurals.library_song_count, 1248, 1248),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 64.dp, end = 24.dp)
                )
                PreviewCategorySection(
                    title = stringResource(R.string.library_section_browse),
                    categories = listOf(
                        LibraryCategory.SONGS to 1248,
                        LibraryCategory.ALBUMS to 96,
                        LibraryCategory.ARTISTS to 214
                    )
                )
                PreviewCategorySection(
                    title = stringResource(R.string.library_section_collections),
                    categories = listOf(
                        LibraryCategory.FAVORITES to 42,
                        LibraryCategory.PLAYLISTS to 7
                    )
                )
            }
            PreviewMiniPlayer()
            PreviewNavBar(selectedIndex = 0)
        }
        if (showMenu) {
            PreviewScrim { PreviewTrackMenu() }
        }
    }
}

@Composable
private fun PreviewCategorySection(title: String, categories: List<Pair<LibraryCategory, Int>>) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        SectionHeader(text = title, tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            categories.forEachIndexed { index, (category, count) ->
                if (index > 0) RowDivider(color = colors.divider)
                CategoryRow(category = category, count = count, contentScope = null, onClick = {})
            }
        }
    }
}

@Composable
private fun PreviewTrackMenu() {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(R.string.custom_theme_preview_song),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.custom_theme_preview_artist),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        RowDivider(color = colors.surfaceVariant)
        PreviewMenuRow(
            Icons.Filled.Album,
            stringResource(R.string.player_menu_go_to_album),
            stringResource(R.string.custom_theme_preview_album)
        )
        PreviewMenuRow(
            Icons.Filled.Mic,
            stringResource(R.string.player_menu_go_to_artist),
            stringResource(R.string.custom_theme_preview_artist)
        )
        PreviewMenuRow(Icons.AutoMirrored.Filled.PlaylistAdd, stringResource(R.string.playlist_add_to), null)
        PreviewMenuRow(Icons.Outlined.Radio, stringResource(R.string.radio_start_from_song), null)
    }
}

@Composable
private fun PreviewMenuRow(icon: ImageVector, title: String, subtitle: String?) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlayerScreenMock() {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxSize().background(colors.background).clipToBounds()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 12.dp)
        ) {
            KateIconButton(onClick = {}) {
                KateIcon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = buttonTint(colors.onSurface),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.player_playing_from).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = stringResource(R.string.custom_theme_preview_album),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            KateIconButton(onClick = {}) {
                KateIcon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = buttonTint(colors.onSurface)
                )
            }
            KateIconButton(onClick = {}) {
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = buttonTint(colors.onSurface)
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.surfaceVariant)))
        ) {
            KateIcon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(84.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.custom_theme_preview_song),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.custom_theme_preview_artist),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            KateIconButton(onClick = {}) {
                KateIcon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = buttonTint(colors.primary)
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)) {
            SeekBarPreview(look = LocalLookAndFeel.current.seekBarLook(), progress = { 0.38f })
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = formatPlaybackTime(72_000L),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText
                )
                Text(
                    text = formatPlaybackTime(187_000L),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            KateControlButton(icon = Icons.Filled.Shuffle, contentDescription = null, onClick = {}, tint = colors.secondaryText, iconSize = 24.dp)
            KateControlButton(icon = Icons.Filled.SkipPrevious, contentDescription = null, onClick = {})
            KatePlayButton(isPlaying = true, onClick = {})
            KateControlButton(icon = Icons.Filled.SkipNext, contentDescription = null, onClick = {})
            KateControlButton(icon = Icons.Filled.Repeat, contentDescription = null, onClick = {}, tint = colors.primary, iconSize = 24.dp)
        }
    }
}

@Composable
private fun SettingsScreenMock(showDialog: Boolean) {
    val colors = LocalClintColors.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = {}) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = stringResource(R.string.look_theme_colors_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = {}) {
                    KateIcon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        tint = buttonTint(colors.secondaryText)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(stringResource(R.string.section_theme), colors.primary)
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.row_theme_title),
                        subtitle = stringResource(R.string.theme_system),
                        colors = colors,
                        onClick = {}
                    )
                    RowDivider(colors.divider)
                    SettingsRow(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.row_accent_title),
                        subtitle = stringResource(R.string.accent_custom),
                        colors = colors,
                        onClick = {}
                    )
                    RowDivider(colors.divider)
                    SettingsRow(
                        icon = Icons.Outlined.Gradient,
                        title = stringResource(R.string.row_surface_intensity_title),
                        subtitle = stringResource(R.string.surface_intensity_soft),
                        colors = colors,
                        onClick = {}
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(stringResource(R.string.look_section_album_art), colors.primary)
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.ColorLens,
                        title = stringResource(R.string.row_app_artwork_colors_title),
                        subtitle = stringResource(R.string.row_app_artwork_colors_subtitle),
                        checked = true,
                        colors = colors,
                        onCheckedChange = {}
                    )
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.DragHandle,
                        title = stringResource(R.string.player_show_handle_title),
                        subtitle = stringResource(R.string.player_show_handle_subtitle),
                        checked = false,
                        colors = colors,
                        onCheckedChange = {}
                    )
                }
            }
            PreviewNavBar(selectedIndex = 3)
        }
        if (showDialog) {
            PreviewScrim { PreviewDialogCard() }
        }
    }
}

@Composable
private fun PreviewDialogCard() {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
    ) {
        Text(
            text = stringResource(R.string.custom_theme_preview_dialog_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.custom_theme_preview_dialog_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
            KateTextButton(
                label = stringResource(R.string.action_cancel),
                role = DialogButtonRole.SECONDARY,
                onClick = {}
            )
            KateTextButton(
                label = stringResource(R.string.custom_theme_delete),
                role = DialogButtonRole.MAIN,
                onClick = {}
            )
        }
    }
}

@Composable
private fun ErrorScreenMock() {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.colorErrorContainer)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            KateIcon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = colors.colorError,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.custom_theme_preview_error_banner),
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorOnErrorContainer,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds().padding(16.dp)
        ) {
            val fieldShape = RoundedCornerShape(12.dp)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(fieldShape)
                        .border(2.dp, colors.colorError, fieldShape)
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.custom_theme_name_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.colorError,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.custom_theme_preview_song),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    KateIcon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = colors.colorError,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.custom_theme_name_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.colorError,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 18.dp)
                ) {
                    KateIcon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = colors.colorError,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.custom_theme_delete),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.colorError,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }
            PreviewDialogCard()
        }
        PreviewNavBar(selectedIndex = 3)
    }
}

@Composable
private fun PaletteGrid(palette: CustomPalette) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 520.dp) 3 else 2
        Column {
            ThemeSlot.entries.chunked(columns).forEach { rowSlots ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 10.dp)
                ) {
                    rowSlots.forEach { slot ->
                        PaletteChip(slot, palette, Modifier.weight(1f).fillMaxHeight())
                    }
                    repeat(columns - rowSlots.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteChip(slot: ThemeSlot, palette: CustomPalette, modifier: Modifier) {
    val colors = LocalClintColors.current
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.cardBackground)
            .border(1.dp, colors.divider, shape)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(44.dp).background(palette.color(slot)))
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(slot.titleRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = colorHex(palette[slot]),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ContrastCard(palette: CustomPalette) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBackground)
    ) {
        ContrastPairs.forEachIndexed { index, pair ->
            if (index > 0) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
            }
            ContrastRow(palette, pair)
        }
    }
}

@Composable
private fun ContrastRow(palette: CustomPalette, pair: ContrastPair) {
    val colors = LocalClintColors.current
    val foreground = palette.color(pair.foreground)
    val background = palette.color(pair.background)
    val ratio = contrastRatio(foreground, background)
    val good = ratio >= 4.5f
    val sampleShape = RoundedCornerShape(8.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 46.dp, height = 32.dp)
                .clip(sampleShape)
                .background(background)
                .border(1.dp, colors.divider, sampleShape)
        ) {
            Text(
                text = stringResource(R.string.custom_theme_contrast_sample),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = foreground
            )
        }
        Text(
            text = stringResource(pair.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f:1", ratio),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (good) colors.primary else colors.colorErrorContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(if (good) R.string.custom_theme_contrast_good else R.string.custom_theme_contrast_low),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (good) colors.onPrimary else colors.colorOnErrorContainer
                )
            }
        }
    }
}
