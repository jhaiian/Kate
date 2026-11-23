package com.jhaiian.kate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.ui.theme.ButtonStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

internal fun familyIcon(vector: ImageVector, family: String): ImageVector {
    val index = when (family) {
        ButtonStylePreferences.FAMILY_FILLED -> 0
        ButtonStylePreferences.FAMILY_OUTLINED -> 1
        ButtonStylePreferences.FAMILY_ROUNDED -> 2
        ButtonStylePreferences.FAMILY_SHARP -> 3
        else -> return vector
    }
    val parts = vector.name.split('.')
    val name = parts.lastOrNull() ?: return vector
    val variants = iconVariants(parts.firstOrNull() == "AutoMirrored", name) ?: return vector
    return variants[index]
}

@Composable
fun styledIcon(vector: ImageVector): ImageVector {
    val family = LocalLookAndFeel.current.iconFamily
    if (family == ButtonStylePreferences.FAMILY_AUTO) return vector
    return remember(vector, family) { familyIcon(vector, family) }
}

@Composable
fun KateIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = styledIcon(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

internal fun choiceShape(key: String, chip: Boolean): Shape = when (key) {
    ButtonStylePreferences.CHIP_PILL -> if (chip) RoundedCornerShape(50) else RoundedCornerShape(28.dp)
    ButtonStylePreferences.CHIP_SQUARE -> RoundedCornerShape(if (chip) 4.dp else 6.dp)
    else -> RoundedCornerShape(if (chip) 12.dp else 16.dp)
}

@Composable
fun Modifier.kateChoice(
    selected: Boolean,
    chip: Boolean = false,
    unselected: Color = LocalClintColors.current.surfaceVariant
): Modifier {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val shape = choiceShape(look.chipShape, chip)
    val clipped = this.clip(shape)
    if (!selected) return clipped.background(unselected)
    return when (look.chipSelected) {
        ButtonStylePreferences.CHIP_SOLID -> clipped.background(colors.primary)
        ButtonStylePreferences.CHIP_OUTLINE -> clipped
            .background(colors.primary.copy(alpha = 0.08f))
            .border(BorderStroke(2.dp, colors.primary), shape)
        else -> clipped.background(colors.primary.copy(alpha = 0.14f))
    }
}

@Composable
fun kateChoiceTint(selected: Boolean): Color {
    val colors = LocalClintColors.current
    if (!selected) return colors.secondaryText
    return if (LocalLookAndFeel.current.chipSelected == ButtonStylePreferences.CHIP_SOLID) colors.onPrimary else colors.primary
}

@Composable
fun kateChoiceText(selected: Boolean): Color {
    val colors = LocalClintColors.current
    if (!selected) return colors.onSurface
    return if (LocalLookAndFeel.current.chipSelected == ButtonStylePreferences.CHIP_SOLID) colors.onPrimary else colors.primary
}

@Composable
fun KateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentOutline: Boolean = false
) {
    val colors = LocalClintColors.current
    val shape = choiceShape(LocalLookAndFeel.current.chipShape, true)
    val content = when {
        accentOutline -> colors.primary
        else -> kateChoiceText(selected)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .then(
                if (accentOutline) {
                    Modifier
                        .clip(shape)
                        .border(BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)), shape)
                } else {
                    Modifier.kateChoice(selected, chip = true)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            KateIcon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.padding(end = 6.dp).size(18.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1
        )
    }
}

@Composable
fun KateSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    when (LocalLookAndFeel.current.switchStyle) {
        ButtonStylePreferences.SWITCH_MINIMAL -> MinimalSwitch(checked, onCheckedChange, modifier, enabled)
        ButtonStylePreferences.SWITCH_CHECKBOX -> CheckboxSwitch(checked, onCheckedChange, modifier, enabled)
        else -> MaterialKateSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            icons = LocalLookAndFeel.current.switchStyle == ButtonStylePreferences.SWITCH_ICONS
        )
    }
}

@Composable
private fun MaterialKateSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    icons: Boolean
) {
    val colors = LocalClintColors.current
    val interactionSource = remember { MutableInteractionSource() }
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumbContent = if (icons) {
            {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            checkedIconColor = colors.primary,
            uncheckedThumbColor = colors.secondaryText,
            uncheckedTrackColor = colors.surfaceVariant,
            uncheckedBorderColor = colors.divider,
            uncheckedIconColor = colors.surfaceVariant,
            disabledCheckedThumbColor = colors.secondaryText,
            disabledCheckedTrackColor = colors.surfaceVariant,
            disabledCheckedBorderColor = colors.divider,
            disabledUncheckedThumbColor = colors.secondaryText,
            disabledUncheckedTrackColor = colors.surfaceVariant,
            disabledUncheckedBorderColor = colors.divider
        )
    )
}

private fun Modifier.toggleIfInteractive(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean
): Modifier = if (onCheckedChange != null) {
    toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
} else {
    this
}

@Composable
private fun MinimalSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean
) {
    val colors = LocalClintColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "minimal_switch_thumb"
    )
    val track by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.surfaceVariant,
        label = "minimal_switch_track"
    )
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(width = 48.dp, height = 48.dp)
            .toggleIfInteractive(checked, onCheckedChange, enabled)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 40.dp, height = 22.dp)
                .clip(CircleShape)
                .background(track)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 3.dp + thumbOffset)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (checked) Color.White else colors.secondaryText)
            )
        }
    }
}

@Composable
private fun CheckboxSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean
) {
    val colors = LocalClintColors.current
    val fill by animateColorAsState(
        targetValue = if (checked) colors.primary else Color.Transparent,
        label = "checkbox_switch_fill"
    )
    val shape = RoundedCornerShape(6.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .toggleIfInteractive(checked, onCheckedChange, enabled)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(shape)
                .background(fill)
                .border(BorderStroke(2.dp, if (checked) colors.primary else colors.secondaryText), shape)
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
