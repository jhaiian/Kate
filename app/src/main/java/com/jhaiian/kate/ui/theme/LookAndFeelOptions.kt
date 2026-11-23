package com.jhaiian.kate.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.KateHaptics

data class ThemeOption(val key: String, @StringRes val labelRes: Int)
data class AccentOption(val key: String, @StringRes val labelRes: Int)
data class IntensityOption(val key: String, @StringRes val labelRes: Int)
data class HapticsOption(val key: String, @StringRes val labelRes: Int)
data class PlayerStyleOption(val key: String, @StringRes val labelRes: Int)
data class NavTabOption(val key: String, @StringRes val labelRes: Int, val icon: ImageVector)

val ThemeOptions = listOf(
    ThemeOption("system", R.string.theme_system),
    ThemeOption("light", R.string.theme_light),
    ThemeOption("dark", R.string.theme_dark)
)

val IntensityOptions = listOf(
    IntensityOption("no_tint", R.string.surface_intensity_no_tint),
    IntensityOption("soft_tint", R.string.surface_intensity_soft),
    IntensityOption("strong_tint", R.string.surface_intensity_strong),
    IntensityOption("pure_mode", R.string.surface_intensity_pure),
    IntensityOption("amoled_no_tint", R.string.surface_intensity_amoled_no_tint)
)

val AccentOptions = listOf(
    AccentOption("material_you", R.string.accent_material_you),
    AccentOption(ThemePreferences.ACCENT_CUSTOM, R.string.accent_custom),
    AccentOption("purple", R.string.accent_purple),
    AccentOption("deep_purple", R.string.accent_deep_purple),
    AccentOption("royal_purple", R.string.accent_royal_purple),
    AccentOption("amethyst", R.string.accent_amethyst),
    AccentOption("lavender", R.string.accent_lavender),
    AccentOption("plum", R.string.accent_plum),
    AccentOption("violet", R.string.accent_violet),
    AccentOption("default", R.string.accent_default),
    AccentOption("charcoal", R.string.accent_charcoal),
    AccentOption("slate", R.string.accent_slate),
    AccentOption("graphite", R.string.accent_graphite),
    AccentOption("obsidian", R.string.accent_obsidian),
    AccentOption("onyx", R.string.accent_onyx),
    AccentOption("titanium", R.string.accent_titanium),
    AccentOption("indigo", R.string.accent_indigo),
    AccentOption("blue", R.string.accent_blue),
    AccentOption("midnight", R.string.accent_midnight),
    AccentOption("cyan", R.string.accent_cyan),
    AccentOption("sky", R.string.accent_sky),
    AccentOption("teal", R.string.accent_teal),
    AccentOption("azure", R.string.accent_azure),
    AccentOption("yellow", R.string.accent_yellow),
    AccentOption("lemon", R.string.accent_lemon),
    AccentOption("gold", R.string.accent_gold),
    AccentOption("amber", R.string.accent_amber),
    AccentOption("sand", R.string.accent_sand),
    AccentOption("sepia", R.string.accent_sepia),
    AccentOption("mustard", R.string.accent_mustard),
    AccentOption("red", R.string.accent_red),
    AccentOption("crimson", R.string.accent_crimson),
    AccentOption("ruby", R.string.accent_ruby),
    AccentOption("pink", R.string.accent_pink),
    AccentOption("coral", R.string.accent_coral),
    AccentOption("burgundy", R.string.accent_burgundy),
    AccentOption("scarlet", R.string.accent_scarlet),
    AccentOption("orange", R.string.accent_orange),
    AccentOption("deep_orange", R.string.accent_deep_orange),
    AccentOption("tangerine", R.string.accent_tangerine),
    AccentOption("apricot", R.string.accent_apricot),
    AccentOption("copper", R.string.accent_copper),
    AccentOption("peach", R.string.accent_peach),
    AccentOption("terracotta", R.string.accent_terracotta),
    AccentOption("green", R.string.accent_green),
    AccentOption("emerald", R.string.accent_emerald),
    AccentOption("mint", R.string.accent_mint),
    AccentOption("forest", R.string.accent_forest),
    AccentOption("sage", R.string.accent_sage),
    AccentOption("lime", R.string.accent_lime),
    AccentOption("olive", R.string.accent_olive)
)

val PaletteStyleOptions = listOf(
    PlayerStyleOption(ThemePreferences.STYLE_AUTO, R.string.palette_style_auto),
    PlayerStyleOption(ThemePreferences.STYLE_TONAL_SPOT, R.string.palette_style_tonal_spot),
    PlayerStyleOption(ThemePreferences.STYLE_VIBRANT, R.string.palette_style_vibrant),
    PlayerStyleOption(ThemePreferences.STYLE_EXPRESSIVE, R.string.palette_style_expressive),
    PlayerStyleOption(ThemePreferences.STYLE_FIDELITY, R.string.palette_style_fidelity),
    PlayerStyleOption(ThemePreferences.STYLE_CONTENT, R.string.palette_style_content),
    PlayerStyleOption(ThemePreferences.STYLE_RAINBOW, R.string.palette_style_rainbow),
    PlayerStyleOption(ThemePreferences.STYLE_FRUIT_SALAD, R.string.palette_style_fruit_salad),
    PlayerStyleOption(ThemePreferences.STYLE_NEUTRAL, R.string.palette_style_neutral),
    PlayerStyleOption(ThemePreferences.STYLE_MONOCHROME, R.string.palette_style_monochrome)
)

val ContrastOptions = listOf(
    PlayerStyleOption(ThemePreferences.CONTRAST_STANDARD, R.string.contrast_standard),
    PlayerStyleOption(ThemePreferences.CONTRAST_MEDIUM, R.string.contrast_medium),
    PlayerStyleOption(ThemePreferences.CONTRAST_HIGH, R.string.contrast_high)
)

val HapticsOptions = listOf(
    HapticsOption(KateHaptics.HAPTICS_OFF, R.string.haptics_off),
    HapticsOption(KateHaptics.HAPTICS_LIGHT, R.string.haptics_light),
    HapticsOption(KateHaptics.HAPTICS_STRONG, R.string.haptics_strong)
)

val ArtShapeOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.ART_SHAPE_ROUNDED, R.string.player_art_shape_rounded),
    PlayerStyleOption(PlayerStylePreferences.ART_SHAPE_SQUARE, R.string.player_art_shape_square),
    PlayerStyleOption(PlayerStylePreferences.ART_SHAPE_CIRCLE, R.string.player_art_shape_circle)
)

val ArtSizeOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.ART_SIZE_NORMAL, R.string.player_art_size_normal),
    PlayerStyleOption(PlayerStylePreferences.ART_SIZE_LARGE, R.string.player_art_size_large)
)

val FontStyleOptions = listOf(
    PlayerStyleOption(DisplayPreferences.FONT_DEFAULT, R.string.font_style_default),
    PlayerStyleOption(DisplayPreferences.FONT_CONDENSED, R.string.font_style_condensed),
    PlayerStyleOption(DisplayPreferences.FONT_SERIF, R.string.font_style_serif),
    PlayerStyleOption(DisplayPreferences.FONT_MONO, R.string.font_style_mono),
    PlayerStyleOption(DisplayPreferences.FONT_CASUAL, R.string.font_style_casual)
)

val TextSizeOptions = listOf(
    PlayerStyleOption(DisplayPreferences.TEXT_SMALL, R.string.text_size_small),
    PlayerStyleOption(DisplayPreferences.TEXT_DEFAULT, R.string.text_size_default),
    PlayerStyleOption(DisplayPreferences.TEXT_LARGE, R.string.text_size_large),
    PlayerStyleOption(DisplayPreferences.TEXT_LARGER, R.string.text_size_larger)
)

val AnimationSpeedOptions = listOf(
    PlayerStyleOption(MotionPreferences.SPEED_OFF, R.string.animation_speed_off),
    PlayerStyleOption(MotionPreferences.SPEED_FAST, R.string.animation_speed_fast),
    PlayerStyleOption(MotionPreferences.SPEED_NORMAL, R.string.animation_speed_normal),
    PlayerStyleOption(MotionPreferences.SPEED_SLOW, R.string.animation_speed_slow)
)

val PlayerLayoutOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_CLASSIC, R.string.player_layout_classic),
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_BIG_ART, R.string.player_layout_big_art),
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_MINIMAL, R.string.player_layout_minimal),
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_VINYL, R.string.player_layout_vinyl),
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_CASSETTE, R.string.player_layout_cassette),
    PlayerStyleOption(PlayerStylePreferences.LAYOUT_CARD, R.string.player_layout_card)
)

val VinylSpeedOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.VINYL_SPEED_SLOW, R.string.player_vinyl_speed_slow),
    PlayerStyleOption(PlayerStylePreferences.VINYL_SPEED_33, R.string.player_vinyl_speed_33),
    PlayerStyleOption(PlayerStylePreferences.VINYL_SPEED_45, R.string.player_vinyl_speed_45)
)

val CassetteColorOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.CASSETTE_BLACK, R.string.player_cassette_black),
    PlayerStyleOption(PlayerStylePreferences.CASSETTE_CLEAR, R.string.player_cassette_clear),
    PlayerStyleOption(PlayerStylePreferences.CASSETTE_WHITE, R.string.player_cassette_white)
)

val PlayerBackgroundOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.BACKGROUND_DRIFT, R.string.player_background_drift),
    PlayerStyleOption(PlayerStylePreferences.BACKGROUND_ARTWORK, R.string.player_background_artwork),
    PlayerStyleOption(PlayerStylePreferences.BACKGROUND_PLAIN, R.string.player_background_plain),
    PlayerStyleOption(PlayerStylePreferences.BACKGROUND_IMAGE, R.string.player_background_image)
)

val PlayerScreenBackgroundOptions = PlayerBackgroundOptions +
    PlayerStyleOption(PlayerStylePreferences.BACKGROUND_GRADIENT, R.string.player_background_gradient)

val TitleWeightOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_DEFAULT, R.string.title_weight_default),
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_LIGHT, R.string.title_weight_light),
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_REGULAR, R.string.title_weight_regular),
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_MEDIUM, R.string.title_weight_medium),
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_BOLD, R.string.title_weight_bold),
    PlayerStyleOption(PlayerStylePreferences.WEIGHT_BLACK, R.string.title_weight_black)
)

val PlayerBackdropStrengthOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.STRENGTH_SUBTLE, R.string.player_backdrop_subtle),
    PlayerStyleOption(PlayerStylePreferences.STRENGTH_NORMAL, R.string.player_backdrop_normal),
    PlayerStyleOption(PlayerStylePreferences.STRENGTH_VIVID, R.string.player_backdrop_vivid)
)

val PlayerHeaderOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.HEADER_ALBUM, R.string.player_header_album),
    PlayerStyleOption(PlayerStylePreferences.HEADER_QUEUE, R.string.player_header_queue),
    PlayerStyleOption(PlayerStylePreferences.HEADER_HIDDEN, R.string.player_header_hidden)
)

val PlayerInfoAlignOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.ALIGN_START, R.string.player_info_align_start),
    PlayerStyleOption(PlayerStylePreferences.ALIGN_CENTER, R.string.player_info_align_center)
)

val PlayerTitleSizeOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.TITLE_SMALL, R.string.player_title_size_small),
    PlayerStyleOption(PlayerStylePreferences.TITLE_MEDIUM, R.string.player_title_size_medium),
    PlayerStyleOption(PlayerStylePreferences.TITLE_LARGE, R.string.player_title_size_large)
)

val PlayerSkipButtonOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.SKIP_TRACKS, R.string.player_skip_buttons_tracks),
    PlayerStyleOption(PlayerStylePreferences.SKIP_SEEK, R.string.player_skip_buttons_seek)
)

val CornerStyleOptions = listOf(
    PlayerStyleOption(LibraryStylePreferences.CORNERS_SHARP, R.string.library_corners_sharp),
    PlayerStyleOption(LibraryStylePreferences.CORNERS_ROUNDED, R.string.library_corners_rounded),
    PlayerStyleOption(LibraryStylePreferences.CORNERS_ROUND, R.string.library_corners_round)
)

val NavTabOptions = listOf(
    NavTabOption(NavigationPreferences.TAB_LIBRARY, R.string.nav_library, Icons.Outlined.LibraryMusic),
    NavTabOption(NavigationPreferences.TAB_SEARCH, R.string.nav_search, Icons.Outlined.Search),
    NavTabOption(NavigationPreferences.TAB_STATS, R.string.nav_stats, Icons.Outlined.Insights),
    NavTabOption(NavigationPreferences.TAB_SETTINGS, R.string.nav_settings, Icons.Outlined.Settings)
)

val NavLabelOptions = listOf(
    PlayerStyleOption(NavigationPreferences.LABELS_ALWAYS, R.string.nav_labels_always),
    PlayerStyleOption(NavigationPreferences.LABELS_SELECTED, R.string.nav_labels_selected),
    PlayerStyleOption(NavigationPreferences.LABELS_NEVER, R.string.nav_labels_never)
)

val MiniPlayerStyleOptions = listOf(
    PlayerStyleOption(NavigationPreferences.MINI_PLAYER_FLOATING, R.string.mini_player_floating),
    PlayerStyleOption(NavigationPreferences.MINI_PLAYER_DOCKED, R.string.mini_player_docked)
)

val LyricsTextSizeOptions = listOf(
    PlayerStyleOption(LyricsPreferences.TEXT_SIZE_SMALL, R.string.lyrics_text_size_small),
    PlayerStyleOption(LyricsPreferences.TEXT_SIZE_MEDIUM, R.string.lyrics_text_size_medium),
    PlayerStyleOption(LyricsPreferences.TEXT_SIZE_LARGE, R.string.lyrics_text_size_large)
)

val LyricsAlignmentOptions = listOf(
    PlayerStyleOption(LyricsPreferences.ALIGN_START, R.string.lyrics_align_start),
    PlayerStyleOption(LyricsPreferences.ALIGN_CENTER, R.string.lyrics_align_center)
)

val LyricsWordStyleOptions = listOf(
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_SWEEP, R.string.lyrics_word_style_sweep),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_POP, R.string.lyrics_word_style_pop),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_FADE, R.string.lyrics_word_style_fade),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_GLOW, R.string.lyrics_word_style_glow),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_HIGHLIGHTER, R.string.lyrics_word_style_highlighter),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_PILL, R.string.lyrics_word_style_pill),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_REVEAL, R.string.lyrics_word_style_reveal),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_WAVE, R.string.lyrics_word_style_wave),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_STRETCH, R.string.lyrics_word_style_stretch),
    PlayerStyleOption(LyricsPreferences.WORD_STYLE_FOCUS, R.string.lyrics_word_style_focus)
)

val SeekStyleOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.STYLE_CLASSIC, R.string.seek_style_classic),
    PlayerStyleOption(SeekBarPreferences.STYLE_WAVEFORM, R.string.seek_style_waveform),
    PlayerStyleOption(SeekBarPreferences.STYLE_SQUIGGLE, R.string.seek_style_squiggle),
    PlayerStyleOption(SeekBarPreferences.STYLE_PILL, R.string.seek_style_pill),
    PlayerStyleOption(SeekBarPreferences.STYLE_DOTS, R.string.seek_style_dots)
)

val SeekThumbOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.THUMB_CIRCLE, R.string.seek_thumb_circle),
    PlayerStyleOption(SeekBarPreferences.THUMB_LINE, R.string.seek_thumb_line),
    PlayerStyleOption(SeekBarPreferences.THUMB_HIDDEN, R.string.seek_thumb_hidden)
)

val SeekThicknessOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.SIZE_SMALL, R.string.seek_thickness_thin),
    PlayerStyleOption(SeekBarPreferences.SIZE_MEDIUM, R.string.seek_thickness_normal),
    PlayerStyleOption(SeekBarPreferences.SIZE_LARGE, R.string.seek_thickness_thick)
)

val SeekColorOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.COLOR_ACCENT, R.string.seek_color_accent),
    PlayerStyleOption(SeekBarPreferences.COLOR_GRADIENT, R.string.seek_color_gradient)
)

val SeekWaveShapeOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.WAVE_MIRRORED, R.string.seek_wave_mirrored),
    PlayerStyleOption(SeekBarPreferences.WAVE_BOTTOM, R.string.seek_wave_bottom)
)

val SeekWaveDensityOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.DENSITY_FINE, R.string.seek_wave_fine),
    PlayerStyleOption(SeekBarPreferences.DENSITY_CHUNKY, R.string.seek_wave_chunky)
)

val SeekSquiggleHeightOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.SIZE_SMALL, R.string.seek_squiggle_height_subtle),
    PlayerStyleOption(SeekBarPreferences.SIZE_MEDIUM, R.string.seek_squiggle_height_normal),
    PlayerStyleOption(SeekBarPreferences.SIZE_LARGE, R.string.seek_squiggle_height_bold)
)

val SeekSquiggleLengthOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.SIZE_SMALL, R.string.seek_squiggle_length_tight),
    PlayerStyleOption(SeekBarPreferences.SIZE_MEDIUM, R.string.seek_squiggle_length_normal),
    PlayerStyleOption(SeekBarPreferences.SIZE_LARGE, R.string.seek_squiggle_length_long)
)

val SeekSquiggleSpeedOptions = listOf(
    PlayerStyleOption(SeekBarPreferences.SIZE_SMALL, R.string.seek_squiggle_speed_slow),
    PlayerStyleOption(SeekBarPreferences.SIZE_MEDIUM, R.string.seek_squiggle_speed_normal),
    PlayerStyleOption(SeekBarPreferences.SIZE_LARGE, R.string.seek_squiggle_speed_fast)
)

val MiniProgressStyleOptions = listOf(
    PlayerStyleOption(MiniProgressPreferences.STYLE_BOTTOM, R.string.mini_progress_bottom),
    PlayerStyleOption(MiniProgressPreferences.STYLE_TOP, R.string.mini_progress_top),
    PlayerStyleOption(MiniProgressPreferences.STYLE_PLAY_RING, R.string.mini_progress_play_ring),
    PlayerStyleOption(MiniProgressPreferences.STYLE_ART_RING, R.string.mini_progress_art_ring),
    PlayerStyleOption(MiniProgressPreferences.STYLE_FILL, R.string.mini_progress_fill),
    PlayerStyleOption(MiniProgressPreferences.STYLE_HIDDEN, R.string.mini_progress_hidden)
)

val MiniProgressThicknessOptions = listOf(
    PlayerStyleOption(MiniProgressPreferences.THICKNESS_HAIRLINE, R.string.mini_progress_hairline),
    PlayerStyleOption(MiniProgressPreferences.THICKNESS_THIN, R.string.seek_thickness_thin),
    PlayerStyleOption(MiniProgressPreferences.THICKNESS_THICK, R.string.seek_thickness_thick)
)

val DoubleTapOptions = listOf(
    PlayerStyleOption(PlayerStylePreferences.DOUBLE_TAP_FAVORITE, R.string.player_double_tap_favorite),
    PlayerStyleOption(PlayerStylePreferences.DOUBLE_TAP_PLAY_PAUSE, R.string.player_double_tap_play_pause),
    PlayerStyleOption(PlayerStylePreferences.DOUBLE_TAP_NONE, R.string.player_double_tap_none)
)

val HomeSectionOptions = listOf(
    NavTabOption(HomePreferences.SECTION_CONTINUE, R.string.home_continue_listening, Icons.Outlined.PlayCircle),
    NavTabOption(HomePreferences.SECTION_RECENTLY_PLAYED, R.string.home_recently_played, Icons.Outlined.History),
    NavTabOption(HomePreferences.SECTION_MOST_PLAYED, R.string.home_most_played, Icons.Outlined.Whatshot),
    NavTabOption(HomePreferences.SECTION_RECENTLY_ADDED, R.string.library_category_recently_added, Icons.Outlined.NewReleases),
    NavTabOption(HomePreferences.SECTION_PLAYLISTS, R.string.home_your_playlists, Icons.AutoMirrored.Outlined.QueueMusic)
)

val PlayShapeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SHAPE_CIRCLE, R.string.button_shape_circle),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_SQUIRCLE, R.string.button_shape_squircle),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_ROUNDED, R.string.button_shape_rounded),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_SQUARE, R.string.button_shape_square),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_NONE, R.string.button_shape_none)
)

val PlaySizeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SIZE_SMALL, R.string.button_size_small),
    PlayerStyleOption(ButtonStylePreferences.SIZE_MEDIUM, R.string.button_size_medium),
    PlayerStyleOption(ButtonStylePreferences.SIZE_LARGE, R.string.button_size_large)
)

val PlayFillOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.FILL_SOLID, R.string.button_fill_solid),
    PlayerStyleOption(ButtonStylePreferences.FILL_GRADIENT, R.string.button_fill_gradient),
    PlayerStyleOption(ButtonStylePreferences.FILL_OUTLINED, R.string.button_fill_outlined),
    PlayerStyleOption(ButtonStylePreferences.FILL_SOFT, R.string.button_fill_soft)
)

val SideButtonOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SIDE_PLAIN, R.string.button_side_plain),
    PlayerStyleOption(ButtonStylePreferences.SIDE_SOFT, R.string.button_side_soft),
    PlayerStyleOption(ButtonStylePreferences.SIDE_OUTLINED, R.string.button_side_outlined)
)

val PressAnimationOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.PRESS_BOUNCE, R.string.button_press_bounce),
    PlayerStyleOption(ButtonStylePreferences.PRESS_SHRINK, R.string.button_press_shrink),
    PlayerStyleOption(ButtonStylePreferences.PRESS_RIPPLE, R.string.button_press_ripple),
    PlayerStyleOption(ButtonStylePreferences.PRESS_NONE, R.string.button_press_none)
)

val ControlSpacingOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SPACING_TIGHT, R.string.button_spacing_tight),
    PlayerStyleOption(ButtonStylePreferences.SPACING_NORMAL, R.string.button_spacing_normal),
    PlayerStyleOption(ButtonStylePreferences.SPACING_WIDE, R.string.button_spacing_wide)
)

val MiniButtonStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.MINI_PLAIN, R.string.button_mini_plain),
    PlayerStyleOption(ButtonStylePreferences.MINI_FILLED, R.string.button_mini_filled),
    PlayerStyleOption(ButtonStylePreferences.MINI_OUTLINED, R.string.button_mini_outlined)
)

val MiniButtonsOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.MINI_BUTTONS_PLAY, R.string.button_mini_buttons_play),
    PlayerStyleOption(ButtonStylePreferences.MINI_BUTTONS_PLAY_NEXT, R.string.button_mini_buttons_play_next),
    PlayerStyleOption(ButtonStylePreferences.MINI_BUTTONS_ALL, R.string.button_mini_buttons_all)
)

val IconButtonStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.ICON_PLAIN, R.string.button_icon_plain),
    PlayerStyleOption(ButtonStylePreferences.ICON_SOFT, R.string.button_icon_soft),
    PlayerStyleOption(ButtonStylePreferences.ICON_OUTLINED, R.string.button_icon_outlined)
)

val IconButtonSizeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.ICON_NORMAL, R.string.button_icon_normal),
    PlayerStyleOption(ButtonStylePreferences.ICON_LARGE, R.string.button_icon_large)
)

val ButtonIconColorOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.COLOR_ACCENT, R.string.button_icon_color_accent),
    PlayerStyleOption(ButtonStylePreferences.COLOR_NEUTRAL, R.string.button_icon_color_neutral)
)

val TileShapeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SHAPE_CIRCLE, R.string.button_shape_circle),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_SQUIRCLE, R.string.button_shape_squircle),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_ROUNDED, R.string.button_shape_rounded),
    PlayerStyleOption(ButtonStylePreferences.SHAPE_SQUARE, R.string.button_shape_square)
)

val TileStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.TILE_SOFT, R.string.button_fill_soft),
    PlayerStyleOption(ButtonStylePreferences.TILE_SOLID, R.string.button_fill_solid),
    PlayerStyleOption(ButtonStylePreferences.TILE_OUTLINED, R.string.button_fill_outlined),
    PlayerStyleOption(ButtonStylePreferences.TILE_NONE, R.string.button_tile_icon_only)
)

val NavIndicatorOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.NAV_PILL, R.string.button_nav_pill),
    PlayerStyleOption(ButtonStylePreferences.NAV_DOT, R.string.button_nav_dot),
    PlayerStyleOption(ButtonStylePreferences.NAV_NONE, R.string.button_nav_none)
)

val NavBarColorOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.NAV_BAR_CARD, R.string.button_nav_bar_card),
    PlayerStyleOption(ButtonStylePreferences.NAV_BAR_BACKGROUND, R.string.button_nav_bar_background),
    PlayerStyleOption(ButtonStylePreferences.NAV_BAR_TINTED, R.string.button_nav_bar_tinted)
)

val ActionShapeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.ACTION_PILL, R.string.button_action_pill),
    PlayerStyleOption(ButtonStylePreferences.ACTION_ROUNDED, R.string.button_shape_rounded),
    PlayerStyleOption(ButtonStylePreferences.ACTION_SQUARE, R.string.button_shape_square)
)

val ActionStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.ACTION_FILLED_SOFT, R.string.button_action_filled_soft),
    PlayerStyleOption(ButtonStylePreferences.ACTION_FILLED, R.string.button_action_filled),
    PlayerStyleOption(ButtonStylePreferences.ACTION_OUTLINED, R.string.button_action_outlined),
    PlayerStyleOption(ButtonStylePreferences.ACTION_SOFT, R.string.button_action_soft)
)

val ActionLabelOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.LABELS_ICON_TEXT, R.string.button_labels_icon_text),
    PlayerStyleOption(ButtonStylePreferences.LABELS_ICON, R.string.button_labels_icon)
)

val DialogStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.DIALOG_TEXT, R.string.button_dialog_text),
    PlayerStyleOption(ButtonStylePreferences.DIALOG_FILLED_MAIN, R.string.button_dialog_filled_main),
    PlayerStyleOption(ButtonStylePreferences.DIALOG_FILLED, R.string.button_dialog_filled),
    PlayerStyleOption(ButtonStylePreferences.DIALOG_OUTLINED, R.string.button_dialog_outlined)
)

val DialogLayoutOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.DIALOG_SIDE, R.string.button_dialog_side),
    PlayerStyleOption(ButtonStylePreferences.DIALOG_FULL, R.string.button_dialog_full),
    PlayerStyleOption(ButtonStylePreferences.DIALOG_STACKED, R.string.button_dialog_stacked)
)

val ChipShapeOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.CHIP_ROUNDED, R.string.button_shape_rounded),
    PlayerStyleOption(ButtonStylePreferences.CHIP_PILL, R.string.button_action_pill),
    PlayerStyleOption(ButtonStylePreferences.CHIP_SQUARE, R.string.button_shape_square)
)

val ChipSelectedOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.CHIP_SOFT, R.string.button_fill_soft),
    PlayerStyleOption(ButtonStylePreferences.CHIP_SOLID, R.string.button_fill_solid),
    PlayerStyleOption(ButtonStylePreferences.CHIP_OUTLINE, R.string.button_fill_outlined)
)

val SwitchStyleOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.SWITCH_CURRENT, R.string.button_switch_current),
    PlayerStyleOption(ButtonStylePreferences.SWITCH_ICONS, R.string.button_switch_icons),
    PlayerStyleOption(ButtonStylePreferences.SWITCH_MINIMAL, R.string.button_switch_minimal),
    PlayerStyleOption(ButtonStylePreferences.SWITCH_CHECKBOX, R.string.button_switch_checkbox)
)

val IconFamilyOptions = listOf(
    PlayerStyleOption(ButtonStylePreferences.FAMILY_AUTO, R.string.button_family_auto),
    PlayerStyleOption(ButtonStylePreferences.FAMILY_FILLED, R.string.button_family_filled),
    PlayerStyleOption(ButtonStylePreferences.FAMILY_OUTLINED, R.string.button_family_outlined),
    PlayerStyleOption(ButtonStylePreferences.FAMILY_ROUNDED, R.string.button_family_rounded),
    PlayerStyleOption(ButtonStylePreferences.FAMILY_SHARP, R.string.button_family_sharp)
)
