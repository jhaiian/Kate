package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.jhaiian.kate.R

object ButtonStylePreferences {
    private const val KEY_PLAY_SHAPE = "button_play_shape"
    private const val KEY_PLAY_SIZE = "button_play_size"
    private const val KEY_PLAY_FILL = "button_play_fill"
    private const val KEY_PLAY_GLOW = "button_play_glow"
    private const val KEY_SIDE_STYLE = "button_side_style"
    private const val KEY_PRESS = "button_press_animation"
    private const val KEY_SPACING = "button_spacing"
    private const val KEY_MINI_STYLE = "button_mini_style"
    private const val KEY_MINI_BUTTONS = "button_mini_buttons"
    private const val KEY_ICON_STYLE = "button_icon_style"
    private const val KEY_ICON_SIZE = "button_icon_size"
    private const val KEY_ICON_COLOR = "button_icon_color"
    private const val KEY_TILE_SHAPE = "button_tile_shape"
    private const val KEY_TILE_STYLE = "button_tile_style"
    private const val KEY_ROW_ARROWS = "button_row_arrows"
    private const val KEY_NAV_INDICATOR = "button_nav_indicator"
    private const val KEY_NAV_BAR_COLOR = "button_nav_bar_color"
    private const val KEY_ACTION_SHAPE = "button_action_shape"
    private const val KEY_ACTION_STYLE = "button_action_style"
    private const val KEY_ACTION_LABELS = "button_action_labels"
    private const val KEY_DIALOG_STYLE = "button_dialog_style"
    private const val KEY_DIALOG_LAYOUT = "button_dialog_layout"
    private const val KEY_CHIP_SHAPE = "button_chip_shape"
    private const val KEY_CHIP_SELECTED = "button_chip_selected"
    private const val KEY_SWITCH_STYLE = "button_switch_style"
    private const val KEY_ICON_FAMILY = "button_icon_family"

    const val PRESET_ROUNDED = "rounded"
    const val PRESET_PILL = "pill"
    const val PRESET_SQUIRCLE = "squircle"
    const val PRESET_SHARP = "sharp"
    const val PRESET_MINIMAL = "minimal"
    const val PRESET_BOLD = "bold"
    const val PRESET_CUSTOM = "custom"

    const val SHAPE_CIRCLE = "circle"
    const val SHAPE_SQUIRCLE = "squircle"
    const val SHAPE_ROUNDED = "rounded"
    const val SHAPE_SQUARE = "square"
    const val SHAPE_NONE = "none"

    const val SIZE_SMALL = "small"
    const val SIZE_MEDIUM = "medium"
    const val SIZE_LARGE = "large"

    const val FILL_SOLID = "solid"
    const val FILL_GRADIENT = "gradient"
    const val FILL_OUTLINED = "outlined"
    const val FILL_SOFT = "soft"

    const val SIDE_PLAIN = "plain"
    const val SIDE_SOFT = "soft"
    const val SIDE_OUTLINED = "outlined"

    const val PRESS_BOUNCE = "bounce"
    const val PRESS_SHRINK = "shrink"
    const val PRESS_RIPPLE = "ripple"
    const val PRESS_NONE = "none"

    const val SPACING_TIGHT = "tight"
    const val SPACING_NORMAL = "normal"
    const val SPACING_WIDE = "wide"

    const val MINI_PLAIN = "plain"
    const val MINI_FILLED = "filled"
    const val MINI_OUTLINED = "outlined"

    const val MINI_BUTTONS_PLAY = "play"
    const val MINI_BUTTONS_PLAY_NEXT = "play_next"
    const val MINI_BUTTONS_ALL = "all"

    const val ICON_PLAIN = "plain"
    const val ICON_SOFT = "soft"
    const val ICON_OUTLINED = "outlined"

    const val ICON_NORMAL = "normal"
    const val ICON_LARGE = "large"

    const val COLOR_ACCENT = "accent"
    const val COLOR_NEUTRAL = "neutral"

    const val TILE_SOFT = "soft"
    const val TILE_SOLID = "solid"
    const val TILE_OUTLINED = "outlined"
    const val TILE_NONE = "none"

    const val NAV_PILL = "pill"
    const val NAV_DOT = "dot"
    const val NAV_NONE = "none"

    const val NAV_BAR_CARD = "card"
    const val NAV_BAR_BACKGROUND = "background"
    const val NAV_BAR_TINTED = "tinted"

    const val ACTION_PILL = "pill"
    const val ACTION_ROUNDED = "rounded"
    const val ACTION_SQUARE = "square"

    const val ACTION_FILLED_SOFT = "filled_soft"
    const val ACTION_FILLED = "filled"
    const val ACTION_OUTLINED = "outlined"
    const val ACTION_SOFT = "soft"

    const val LABELS_ICON_TEXT = "icon_text"
    const val LABELS_ICON = "icon"

    const val DIALOG_TEXT = "text"
    const val DIALOG_FILLED_MAIN = "filled_main"
    const val DIALOG_FILLED = "filled"
    const val DIALOG_OUTLINED = "outlined"

    const val DIALOG_SIDE = "side"
    const val DIALOG_FULL = "full"
    const val DIALOG_STACKED = "stacked"

    const val CHIP_ROUNDED = "rounded"
    const val CHIP_PILL = "pill"
    const val CHIP_SQUARE = "square"

    const val CHIP_SOFT = "soft"
    const val CHIP_SOLID = "solid"
    const val CHIP_OUTLINE = "outline"

    const val SWITCH_CURRENT = "current"
    const val SWITCH_ICONS = "icons"
    const val SWITCH_MINIMAL = "minimal"
    const val SWITCH_CHECKBOX = "checkbox"

    const val FAMILY_AUTO = "auto"
    const val FAMILY_FILLED = "filled"
    const val FAMILY_OUTLINED = "outlined"
    const val FAMILY_ROUNDED = "rounded"
    const val FAMILY_SHARP = "sharp"

    const val DEFAULT_PLAY_SHAPE = SHAPE_CIRCLE
    const val DEFAULT_PLAY_SIZE = SIZE_MEDIUM
    const val DEFAULT_PLAY_FILL = FILL_SOLID
    const val DEFAULT_PLAY_GLOW = true
    const val DEFAULT_SIDE_STYLE = SIDE_PLAIN
    const val DEFAULT_PRESS = PRESS_BOUNCE
    const val DEFAULT_SPACING = SPACING_NORMAL
    const val DEFAULT_MINI_STYLE = MINI_PLAIN
    const val DEFAULT_MINI_BUTTONS = MINI_BUTTONS_PLAY_NEXT
    const val DEFAULT_ICON_STYLE = ICON_PLAIN
    const val DEFAULT_ICON_SIZE = ICON_NORMAL
    const val DEFAULT_ICON_COLOR = COLOR_ACCENT
    const val DEFAULT_TILE_SHAPE = SHAPE_ROUNDED
    const val DEFAULT_TILE_STYLE = TILE_SOFT
    const val DEFAULT_ROW_ARROWS = true
    const val DEFAULT_NAV_INDICATOR = NAV_PILL
    const val DEFAULT_NAV_BAR_COLOR = NAV_BAR_CARD
    const val DEFAULT_ACTION_SHAPE = ACTION_PILL
    const val DEFAULT_ACTION_STYLE = ACTION_FILLED_SOFT
    const val DEFAULT_ACTION_LABELS = LABELS_ICON_TEXT
    const val DEFAULT_DIALOG_STYLE = DIALOG_TEXT
    const val DEFAULT_DIALOG_LAYOUT = DIALOG_SIDE
    const val DEFAULT_CHIP_SHAPE = CHIP_ROUNDED
    const val DEFAULT_CHIP_SELECTED = CHIP_SOFT
    const val DEFAULT_SWITCH_STYLE = SWITCH_CURRENT
    const val DEFAULT_ICON_FAMILY = FAMILY_AUTO

    fun getPlayShape(context: Context): String =
        getChoice(context, KEY_PLAY_SHAPE, DEFAULT_PLAY_SHAPE, PlayShapeOptions.map { it.key })

    fun getPlaySize(context: Context): String =
        getChoice(context, KEY_PLAY_SIZE, DEFAULT_PLAY_SIZE, PlaySizeOptions.map { it.key })

    fun getPlayFill(context: Context): String =
        getChoice(context, KEY_PLAY_FILL, DEFAULT_PLAY_FILL, PlayFillOptions.map { it.key })

    fun getPlayGlow(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_PLAY_GLOW, DEFAULT_PLAY_GLOW)

    fun getSideStyle(context: Context): String =
        getChoice(context, KEY_SIDE_STYLE, DEFAULT_SIDE_STYLE, SideButtonOptions.map { it.key })

    fun getPress(context: Context): String =
        getChoice(context, KEY_PRESS, DEFAULT_PRESS, PressAnimationOptions.map { it.key })

    fun getSpacing(context: Context): String =
        getChoice(context, KEY_SPACING, DEFAULT_SPACING, ControlSpacingOptions.map { it.key })

    fun getMiniStyle(context: Context): String =
        getChoice(context, KEY_MINI_STYLE, DEFAULT_MINI_STYLE, MiniButtonStyleOptions.map { it.key })

    fun getMiniButtons(context: Context): String =
        getChoice(context, KEY_MINI_BUTTONS, DEFAULT_MINI_BUTTONS, MiniButtonsOptions.map { it.key })

    fun getIconStyle(context: Context): String =
        getChoice(context, KEY_ICON_STYLE, DEFAULT_ICON_STYLE, IconButtonStyleOptions.map { it.key })

    fun getIconSize(context: Context): String =
        getChoice(context, KEY_ICON_SIZE, DEFAULT_ICON_SIZE, IconButtonSizeOptions.map { it.key })

    fun getIconColor(context: Context): String =
        getChoice(context, KEY_ICON_COLOR, DEFAULT_ICON_COLOR, ButtonIconColorOptions.map { it.key })

    fun getTileShape(context: Context): String =
        getChoice(context, KEY_TILE_SHAPE, DEFAULT_TILE_SHAPE, TileShapeOptions.map { it.key })

    fun getTileStyle(context: Context): String =
        getChoice(context, KEY_TILE_STYLE, DEFAULT_TILE_STYLE, TileStyleOptions.map { it.key })

    fun getRowArrows(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ROW_ARROWS, DEFAULT_ROW_ARROWS)

    fun getNavIndicator(context: Context): String =
        getChoice(context, KEY_NAV_INDICATOR, DEFAULT_NAV_INDICATOR, NavIndicatorOptions.map { it.key })

    fun getNavBarColor(context: Context): String =
        getChoice(context, KEY_NAV_BAR_COLOR, DEFAULT_NAV_BAR_COLOR, NavBarColorOptions.map { it.key })

    fun getActionShape(context: Context): String =
        getChoice(context, KEY_ACTION_SHAPE, DEFAULT_ACTION_SHAPE, ActionShapeOptions.map { it.key })

    fun getActionStyle(context: Context): String =
        getChoice(context, KEY_ACTION_STYLE, DEFAULT_ACTION_STYLE, ActionStyleOptions.map { it.key })

    fun getActionLabels(context: Context): String =
        getChoice(context, KEY_ACTION_LABELS, DEFAULT_ACTION_LABELS, ActionLabelOptions.map { it.key })

    fun getDialogStyle(context: Context): String =
        getChoice(context, KEY_DIALOG_STYLE, DEFAULT_DIALOG_STYLE, DialogStyleOptions.map { it.key })

    fun getDialogLayout(context: Context): String =
        getChoice(context, KEY_DIALOG_LAYOUT, DEFAULT_DIALOG_LAYOUT, DialogLayoutOptions.map { it.key })

    fun getChipShape(context: Context): String =
        getChoice(context, KEY_CHIP_SHAPE, DEFAULT_CHIP_SHAPE, ChipShapeOptions.map { it.key })

    fun getChipSelected(context: Context): String =
        getChoice(context, KEY_CHIP_SELECTED, DEFAULT_CHIP_SELECTED, ChipSelectedOptions.map { it.key })

    fun getSwitchStyle(context: Context): String =
        getChoice(context, KEY_SWITCH_STYLE, DEFAULT_SWITCH_STYLE, SwitchStyleOptions.map { it.key })

    fun getIconFamily(context: Context): String =
        getChoice(context, KEY_ICON_FAMILY, DEFAULT_ICON_FAMILY, IconFamilyOptions.map { it.key })

    fun save(
        context: Context,
        playShape: String,
        playSize: String,
        playFill: String,
        playGlow: Boolean,
        sideStyle: String,
        press: String,
        spacing: String,
        miniStyle: String,
        miniButtons: String,
        iconStyle: String,
        iconSize: String,
        iconColor: String,
        tileShape: String,
        tileStyle: String,
        rowArrows: Boolean,
        navIndicator: String,
        navBarColor: String,
        actionShape: String,
        actionStyle: String,
        actionLabels: String,
        dialogStyle: String,
        dialogLayout: String,
        chipShape: String,
        chipSelected: String,
        switchStyle: String,
        iconFamily: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_PLAY_SHAPE, playShape)
            .putString(KEY_PLAY_SIZE, playSize)
            .putString(KEY_PLAY_FILL, playFill)
            .putBoolean(KEY_PLAY_GLOW, playGlow)
            .putString(KEY_SIDE_STYLE, sideStyle)
            .putString(KEY_PRESS, press)
            .putString(KEY_SPACING, spacing)
            .putString(KEY_MINI_STYLE, miniStyle)
            .putString(KEY_MINI_BUTTONS, miniButtons)
            .putString(KEY_ICON_STYLE, iconStyle)
            .putString(KEY_ICON_SIZE, iconSize)
            .putString(KEY_ICON_COLOR, iconColor)
            .putString(KEY_TILE_SHAPE, tileShape)
            .putString(KEY_TILE_STYLE, tileStyle)
            .putBoolean(KEY_ROW_ARROWS, rowArrows)
            .putString(KEY_NAV_INDICATOR, navIndicator)
            .putString(KEY_NAV_BAR_COLOR, navBarColor)
            .putString(KEY_ACTION_SHAPE, actionShape)
            .putString(KEY_ACTION_STYLE, actionStyle)
            .putString(KEY_ACTION_LABELS, actionLabels)
            .putString(KEY_DIALOG_STYLE, dialogStyle)
            .putString(KEY_DIALOG_LAYOUT, dialogLayout)
            .putString(KEY_CHIP_SHAPE, chipShape)
            .putString(KEY_CHIP_SELECTED, chipSelected)
            .putString(KEY_SWITCH_STYLE, switchStyle)
            .putString(KEY_ICON_FAMILY, iconFamily)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }
}

data class ButtonPreset(
    val key: String,
    @StringRes val labelRes: Int,
    val playShape: String,
    val playSize: String,
    val playFill: String,
    val playGlow: Boolean,
    val sideStyle: String,
    val press: String,
    val spacing: String,
    val miniStyle: String,
    val iconStyle: String,
    val iconSize: String,
    val tileShape: String,
    val tileStyle: String,
    val navIndicator: String,
    val actionShape: String,
    val actionStyle: String,
    val dialogStyle: String,
    val chipShape: String,
    val chipSelected: String,
    val switchStyle: String,
    val iconFamily: String
) {
    fun matches(settings: LookAndFeelSettings): Boolean =
        settings.buttonPlayShape == playShape &&
            settings.buttonPlaySize == playSize &&
            settings.buttonPlayFill == playFill &&
            settings.buttonPlayGlow == playGlow &&
            settings.buttonSideStyle == sideStyle &&
            settings.buttonPress == press &&
            settings.buttonSpacing == spacing &&
            settings.miniButtonStyle == miniStyle &&
            settings.iconButtonStyle == iconStyle &&
            settings.iconButtonSize == iconSize &&
            settings.tileShape == tileShape &&
            settings.tileStyle == tileStyle &&
            settings.navIndicator == navIndicator &&
            settings.actionShape == actionShape &&
            settings.actionStyle == actionStyle &&
            settings.dialogStyle == dialogStyle &&
            settings.chipShape == chipShape &&
            settings.chipSelected == chipSelected &&
            settings.switchStyle == switchStyle &&
            settings.iconFamily == iconFamily
}

val ButtonPresets = listOf(
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_ROUNDED,
        labelRes = R.string.button_preset_rounded,
        playShape = ButtonStylePreferences.SHAPE_CIRCLE,
        playSize = ButtonStylePreferences.SIZE_MEDIUM,
        playFill = ButtonStylePreferences.FILL_SOLID,
        playGlow = true,
        sideStyle = ButtonStylePreferences.SIDE_PLAIN,
        press = ButtonStylePreferences.PRESS_BOUNCE,
        spacing = ButtonStylePreferences.SPACING_NORMAL,
        miniStyle = ButtonStylePreferences.MINI_PLAIN,
        iconStyle = ButtonStylePreferences.ICON_PLAIN,
        iconSize = ButtonStylePreferences.ICON_NORMAL,
        tileShape = ButtonStylePreferences.SHAPE_ROUNDED,
        tileStyle = ButtonStylePreferences.TILE_SOFT,
        navIndicator = ButtonStylePreferences.NAV_PILL,
        actionShape = ButtonStylePreferences.ACTION_PILL,
        actionStyle = ButtonStylePreferences.ACTION_FILLED_SOFT,
        dialogStyle = ButtonStylePreferences.DIALOG_TEXT,
        chipShape = ButtonStylePreferences.CHIP_ROUNDED,
        chipSelected = ButtonStylePreferences.CHIP_SOFT,
        switchStyle = ButtonStylePreferences.SWITCH_CURRENT,
        iconFamily = ButtonStylePreferences.FAMILY_AUTO
    ),
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_PILL,
        labelRes = R.string.button_preset_pill,
        playShape = ButtonStylePreferences.SHAPE_CIRCLE,
        playSize = ButtonStylePreferences.SIZE_LARGE,
        playFill = ButtonStylePreferences.FILL_GRADIENT,
        playGlow = true,
        sideStyle = ButtonStylePreferences.SIDE_SOFT,
        press = ButtonStylePreferences.PRESS_SHRINK,
        spacing = ButtonStylePreferences.SPACING_NORMAL,
        miniStyle = ButtonStylePreferences.MINI_FILLED,
        iconStyle = ButtonStylePreferences.ICON_SOFT,
        iconSize = ButtonStylePreferences.ICON_NORMAL,
        tileShape = ButtonStylePreferences.SHAPE_CIRCLE,
        tileStyle = ButtonStylePreferences.TILE_SOFT,
        navIndicator = ButtonStylePreferences.NAV_PILL,
        actionShape = ButtonStylePreferences.ACTION_PILL,
        actionStyle = ButtonStylePreferences.ACTION_FILLED,
        dialogStyle = ButtonStylePreferences.DIALOG_FILLED_MAIN,
        chipShape = ButtonStylePreferences.CHIP_PILL,
        chipSelected = ButtonStylePreferences.CHIP_SOLID,
        switchStyle = ButtonStylePreferences.SWITCH_ICONS,
        iconFamily = ButtonStylePreferences.FAMILY_ROUNDED
    ),
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_SQUIRCLE,
        labelRes = R.string.button_preset_squircle,
        playShape = ButtonStylePreferences.SHAPE_SQUIRCLE,
        playSize = ButtonStylePreferences.SIZE_MEDIUM,
        playFill = ButtonStylePreferences.FILL_SOLID,
        playGlow = true,
        sideStyle = ButtonStylePreferences.SIDE_SOFT,
        press = ButtonStylePreferences.PRESS_BOUNCE,
        spacing = ButtonStylePreferences.SPACING_NORMAL,
        miniStyle = ButtonStylePreferences.MINI_FILLED,
        iconStyle = ButtonStylePreferences.ICON_SOFT,
        iconSize = ButtonStylePreferences.ICON_NORMAL,
        tileShape = ButtonStylePreferences.SHAPE_SQUIRCLE,
        tileStyle = ButtonStylePreferences.TILE_SOFT,
        navIndicator = ButtonStylePreferences.NAV_PILL,
        actionShape = ButtonStylePreferences.ACTION_ROUNDED,
        actionStyle = ButtonStylePreferences.ACTION_FILLED_SOFT,
        dialogStyle = ButtonStylePreferences.DIALOG_FILLED_MAIN,
        chipShape = ButtonStylePreferences.CHIP_ROUNDED,
        chipSelected = ButtonStylePreferences.CHIP_SOFT,
        switchStyle = ButtonStylePreferences.SWITCH_ICONS,
        iconFamily = ButtonStylePreferences.FAMILY_ROUNDED
    ),
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_SHARP,
        labelRes = R.string.button_preset_sharp,
        playShape = ButtonStylePreferences.SHAPE_SQUARE,
        playSize = ButtonStylePreferences.SIZE_MEDIUM,
        playFill = ButtonStylePreferences.FILL_SOLID,
        playGlow = false,
        sideStyle = ButtonStylePreferences.SIDE_OUTLINED,
        press = ButtonStylePreferences.PRESS_SHRINK,
        spacing = ButtonStylePreferences.SPACING_TIGHT,
        miniStyle = ButtonStylePreferences.MINI_OUTLINED,
        iconStyle = ButtonStylePreferences.ICON_OUTLINED,
        iconSize = ButtonStylePreferences.ICON_NORMAL,
        tileShape = ButtonStylePreferences.SHAPE_SQUARE,
        tileStyle = ButtonStylePreferences.TILE_OUTLINED,
        navIndicator = ButtonStylePreferences.NAV_PILL,
        actionShape = ButtonStylePreferences.ACTION_SQUARE,
        actionStyle = ButtonStylePreferences.ACTION_OUTLINED,
        dialogStyle = ButtonStylePreferences.DIALOG_OUTLINED,
        chipShape = ButtonStylePreferences.CHIP_SQUARE,
        chipSelected = ButtonStylePreferences.CHIP_OUTLINE,
        switchStyle = ButtonStylePreferences.SWITCH_CHECKBOX,
        iconFamily = ButtonStylePreferences.FAMILY_SHARP
    ),
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_MINIMAL,
        labelRes = R.string.button_preset_minimal,
        playShape = ButtonStylePreferences.SHAPE_NONE,
        playSize = ButtonStylePreferences.SIZE_MEDIUM,
        playFill = ButtonStylePreferences.FILL_SOLID,
        playGlow = false,
        sideStyle = ButtonStylePreferences.SIDE_PLAIN,
        press = ButtonStylePreferences.PRESS_RIPPLE,
        spacing = ButtonStylePreferences.SPACING_NORMAL,
        miniStyle = ButtonStylePreferences.MINI_PLAIN,
        iconStyle = ButtonStylePreferences.ICON_PLAIN,
        iconSize = ButtonStylePreferences.ICON_NORMAL,
        tileShape = ButtonStylePreferences.SHAPE_ROUNDED,
        tileStyle = ButtonStylePreferences.TILE_NONE,
        navIndicator = ButtonStylePreferences.NAV_DOT,
        actionShape = ButtonStylePreferences.ACTION_PILL,
        actionStyle = ButtonStylePreferences.ACTION_SOFT,
        dialogStyle = ButtonStylePreferences.DIALOG_TEXT,
        chipShape = ButtonStylePreferences.CHIP_PILL,
        chipSelected = ButtonStylePreferences.CHIP_OUTLINE,
        switchStyle = ButtonStylePreferences.SWITCH_MINIMAL,
        iconFamily = ButtonStylePreferences.FAMILY_OUTLINED
    ),
    ButtonPreset(
        key = ButtonStylePreferences.PRESET_BOLD,
        labelRes = R.string.button_preset_bold,
        playShape = ButtonStylePreferences.SHAPE_ROUNDED,
        playSize = ButtonStylePreferences.SIZE_LARGE,
        playFill = ButtonStylePreferences.FILL_GRADIENT,
        playGlow = true,
        sideStyle = ButtonStylePreferences.SIDE_SOFT,
        press = ButtonStylePreferences.PRESS_BOUNCE,
        spacing = ButtonStylePreferences.SPACING_WIDE,
        miniStyle = ButtonStylePreferences.MINI_FILLED,
        iconStyle = ButtonStylePreferences.ICON_SOFT,
        iconSize = ButtonStylePreferences.ICON_LARGE,
        tileShape = ButtonStylePreferences.SHAPE_ROUNDED,
        tileStyle = ButtonStylePreferences.TILE_SOLID,
        navIndicator = ButtonStylePreferences.NAV_PILL,
        actionShape = ButtonStylePreferences.ACTION_ROUNDED,
        actionStyle = ButtonStylePreferences.ACTION_FILLED,
        dialogStyle = ButtonStylePreferences.DIALOG_FILLED,
        chipShape = ButtonStylePreferences.CHIP_ROUNDED,
        chipSelected = ButtonStylePreferences.CHIP_SOLID,
        switchStyle = ButtonStylePreferences.SWITCH_CURRENT,
        iconFamily = ButtonStylePreferences.FAMILY_FILLED
    )
)
