package com.jhaiian.kate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.ui.theme.ButtonStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

enum class DialogButtonRole { MAIN, SECONDARY, DESTRUCTIVE, LINK }

private enum class ButtonFill { FILLED, SOFT, OUTLINED, TEXT }

internal fun actionShape(key: String): Shape = when (key) {
    ButtonStylePreferences.ACTION_ROUNDED -> RoundedCornerShape(14.dp)
    ButtonStylePreferences.ACTION_SQUARE -> RoundedCornerShape(6.dp)
    else -> RoundedCornerShape(50)
}

private fun actionFill(style: String, primary: Boolean): ButtonFill = when (style) {
    ButtonStylePreferences.ACTION_FILLED -> ButtonFill.FILLED
    ButtonStylePreferences.ACTION_OUTLINED -> ButtonFill.OUTLINED
    ButtonStylePreferences.ACTION_SOFT -> ButtonFill.SOFT
    else -> if (primary) ButtonFill.FILLED else ButtonFill.SOFT
}

private fun dialogFill(style: String, role: DialogButtonRole): ButtonFill {
    if (role == DialogButtonRole.LINK) return ButtonFill.TEXT
    val main = role != DialogButtonRole.SECONDARY
    return when (style) {
        ButtonStylePreferences.DIALOG_FILLED_MAIN -> if (main) ButtonFill.FILLED else ButtonFill.TEXT
        ButtonStylePreferences.DIALOG_FILLED -> if (main) ButtonFill.FILLED else ButtonFill.SOFT
        ButtonStylePreferences.DIALOG_OUTLINED -> ButtonFill.OUTLINED
        else -> ButtonFill.TEXT
    }
}

@Composable
private fun KateButtonSurface(
    fill: ButtonFill,
    accent: Color,
    onAccent: Color,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    onLongClick: (() -> Unit)?,
    onClickLabel: String?,
    onLongClickLabel: String?,
    minHeight: Dp,
    horizontalPadding: Dp,
    content: @Composable (Color) -> Unit
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = when {
        !enabled && fill != ButtonFill.TEXT && fill != ButtonFill.OUTLINED -> colors.secondaryText
        fill == ButtonFill.FILLED -> onAccent
        else -> accent
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .katePressable(interactionSource)
            .alpha(if (enabled || fill == ButtonFill.FILLED || fill == ButtonFill.SOFT) 1f else 0.45f)
            .defaultMinSize(minHeight = minHeight)
            .clip(shape)
            .then(
                when (fill) {
                    ButtonFill.FILLED -> Modifier.background(if (enabled) accent else colors.surfaceVariant)
                    ButtonFill.SOFT -> Modifier.background(if (enabled) accent.copy(alpha = 0.14f) else colors.surfaceVariant)
                    ButtonFill.OUTLINED -> Modifier.border(BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)), shape)
                    ButtonFill.TEXT -> Modifier
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = pressIndication(look.buttonPress),
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding)
    ) {
        content(contentColor)
    }
}

@Composable
fun KateActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true,
    enabled: Boolean = true,
    compact: Boolean = false,
    allowIconOnly: Boolean = true,
    minHeight: Dp = 48.dp,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val iconOnly = allowIconOnly && icon != null && look.actionLabels == ButtonStylePreferences.LABELS_ICON
    KateButtonSurface(
        fill = actionFill(look.actionStyle, primary),
        accent = colors.primary,
        onAccent = colors.onPrimary,
        shape = actionShape(look.actionShape),
        enabled = enabled,
        onClick = onClick,
        modifier = if (iconOnly) modifier.semantics { contentDescription = label } else modifier,
        onLongClick = onLongClick,
        onClickLabel = label,
        onLongClickLabel = onLongClickLabel,
        minHeight = minHeight,
        horizontalPadding = if (compact) 14.dp else 18.dp
    ) { contentColor ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                KateIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                )
            }
            if (!iconOnly) {
                if (icon != null) Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun KateTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: DialogButtonRole = DialogButtonRole.SECONDARY,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val fill = dialogFill(look.dialogStyle, role)
    val accent = when (role) {
        DialogButtonRole.DESTRUCTIVE -> colors.colorError
        DialogButtonRole.SECONDARY -> if (fill == ButtonFill.TEXT) buttonTint(colors.secondaryText) else colors.primary
        else -> colors.primary
    }
    KateButtonSurface(
        fill = fill,
        accent = accent,
        onAccent = if (role == DialogButtonRole.DESTRUCTIVE) Color.White else colors.onPrimary,
        shape = actionShape(look.actionShape),
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        onLongClick = null,
        onClickLabel = null,
        onLongClickLabel = null,
        minHeight = 40.dp,
        horizontalPadding = if (fill == ButtonFill.TEXT) 12.dp else 18.dp
    ) { contentColor ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                KateIcon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (role == DialogButtonRole.SECONDARY) FontWeight.Medium else FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KateDialogActions(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val layoutKey = LocalLookAndFeel.current.dialogLayout
    Layout(
        content = content,
        modifier = if (layoutKey == ButtonStylePreferences.DIALOG_SIDE) modifier else modifier.padding(start = 12.dp)
    ) { measurables, constraints ->
        val gap = 8.dp.roundToPx()
        val count = measurables.size
        when (layoutKey) {
            ButtonStylePreferences.DIALOG_STACKED -> {
                val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
                val placeables = measurables.map { it.measure(Constraints.fixedWidth(width)) }.reversed()
                val height = placeables.sumOf { it.height } + gap * (placeables.size - 1).coerceAtLeast(0)
                layout(width, height) {
                    var y = 0
                    placeables.forEach {
                        it.placeRelative(0, y)
                        y += it.height + gap
                    }
                }
            }
            ButtonStylePreferences.DIALOG_FULL -> {
                val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
                val each = if (count > 0) ((width - gap * (count - 1)) / count).coerceAtLeast(0) else 0
                val placeables = measurables.map { it.measure(Constraints.fixedWidth(each)) }
                val height = placeables.maxOfOrNull { it.height } ?: 0
                layout(width, height) {
                    var x = 0
                    placeables.forEach {
                        it.placeRelative(x, (height - it.height) / 2)
                        x += each + gap
                    }
                }
            }
            else -> {
                val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
                val total = placeables.sumOf { it.width } + gap * (count - 1).coerceAtLeast(0)
                val width = if (constraints.hasBoundedWidth) constraints.maxWidth else total
                val height = placeables.maxOfOrNull { it.height } ?: 0
                layout(width, height) {
                    var x = width - total
                    placeables.forEach {
                        it.placeRelative(x, (height - it.height) / 2)
                        x += it.width + gap
                    }
                }
            }
        }
    }
}
