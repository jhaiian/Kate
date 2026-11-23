package com.jhaiian.kate.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.player.PlayPauseIcon
import com.jhaiian.kate.ui.player.shiftedHue
import com.jhaiian.kate.ui.theme.ButtonStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

object SquircleShape : Shape {
    private const val Steps = 96
    private const val Exponent = 0.5

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        for (step in 0 until Steps) {
            val angle = 2.0 * PI * step / Steps
            val c = cos(angle)
            val s = sin(angle)
            val x = halfWidth + halfWidth * (sign(c) * abs(c).pow(Exponent)).toFloat()
            val y = halfHeight + halfHeight * (sign(s) * abs(s).pow(Exponent)).toFloat()
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

internal fun playButtonShape(key: String): Shape = when (key) {
    ButtonStylePreferences.SHAPE_SQUIRCLE -> SquircleShape
    ButtonStylePreferences.SHAPE_ROUNDED -> RoundedCornerShape(28)
    ButtonStylePreferences.SHAPE_SQUARE -> RoundedCornerShape(10)
    else -> CircleShape
}

internal fun sideButtonShape(key: String): Shape = when (key) {
    ButtonStylePreferences.SHAPE_SQUIRCLE -> SquircleShape
    ButtonStylePreferences.SHAPE_ROUNDED -> RoundedCornerShape(30)
    ButtonStylePreferences.SHAPE_SQUARE -> RoundedCornerShape(14)
    else -> CircleShape
}

internal fun playSizeScale(key: String): Float = when (key) {
    ButtonStylePreferences.SIZE_SMALL -> 0.84f
    ButtonStylePreferences.SIZE_LARGE -> 1.17f
    else -> 1f
}

internal class ControlsSpacing(val arrangement: Arrangement.Horizontal, val horizontalPadding: Dp)

internal fun controlsSpacing(key: String): ControlsSpacing = when (key) {
    ButtonStylePreferences.SPACING_TIGHT -> ControlsSpacing(Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), 20.dp)
    ButtonStylePreferences.SPACING_WIDE -> ControlsSpacing(Arrangement.SpaceBetween, 4.dp)
    else -> ControlsSpacing(Arrangement.SpaceBetween, 20.dp)
}

@Composable
internal fun rememberPressScale(interactionSource: MutableInteractionSource, press: String): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    val target = if (!pressed) {
        1f
    } else {
        when (press) {
            ButtonStylePreferences.PRESS_BOUNCE -> 0.86f
            ButtonStylePreferences.PRESS_SHRINK -> 0.92f
            else -> 1f
        }
    }
    return animateFloatAsState(
        targetValue = target,
        animationSpec = if (press == ButtonStylePreferences.PRESS_BOUNCE) {
            spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMedium)
        } else {
            spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "kate_press_scale"
    )
}

@Composable
internal fun pressIndication(press: String): Indication? =
    if (press == ButtonStylePreferences.PRESS_RIPPLE) LocalIndication.current else null

@Composable
fun buttonTint(base: Color): Color {
    val colors = LocalClintColors.current
    if (LocalLookAndFeel.current.buttonIconColor != ButtonStylePreferences.COLOR_ACCENT) return base
    return when (base) {
        colors.onSurface -> colors.primary
        colors.secondaryText -> colors.primary.copy(alpha = 0.55f)
        else -> base
    }
}

@Composable
fun Modifier.katePressable(interactionSource: MutableInteractionSource): Modifier {
    val scale = rememberPressScale(interactionSource, LocalLookAndFeel.current.buttonPress)
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
fun KateIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val style = look.iconButtonStyle
    val large = look.iconButtonSize == ButtonStylePreferences.ICON_LARGE
    val accent = look.buttonIconColor == ButtonStylePreferences.COLOR_ACCENT
    val shape = if (style == ButtonStylePreferences.ICON_PLAIN) CircleShape else sideButtonShape(look.buttonPlayShape)
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, look.buttonPress)
    val contentTint = buttonTint(colors.onSurface)
    val base = if (accent) colors.primary else colors.onSurface
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(if (large) 56.dp else 48.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (large) 48.dp else 40.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .clip(shape)
                .then(
                    when (style) {
                        ButtonStylePreferences.ICON_SOFT -> Modifier.background(base.copy(alpha = if (accent) 0.13f else 0.08f))
                        ButtonStylePreferences.ICON_OUTLINED -> Modifier.border(
                            BorderStroke(1.5.dp, base.copy(alpha = if (accent) 0.45f else 0.24f)),
                            shape
                        )
                        else -> Modifier
                    }
                )
                .indication(interactionSource, pressIndication(look.buttonPress))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = if (large) {
                    Modifier.graphicsLayer {
                        scaleX = 1.18f
                        scaleY = 1.18f
                    }
                } else {
                    Modifier
                }
            ) {
                CompositionLocalProvider(LocalContentColor provides contentTint) {
                    content()
                }
            }
        }
    }
}

internal fun tileShape(key: String): Shape = when (key) {
    ButtonStylePreferences.SHAPE_CIRCLE -> CircleShape
    ButtonStylePreferences.SHAPE_SQUIRCLE -> SquircleShape
    ButtonStylePreferences.SHAPE_SQUARE -> RoundedCornerShape(12)
    else -> RoundedCornerShape(30)
}

@Composable
fun Modifier.kateIconTile(size: Dp, highlighted: Boolean = false): Modifier {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val shape = tileShape(look.tileShape)
    val accent = look.buttonIconColor == ButtonStylePreferences.COLOR_ACCENT
    val base = if (accent) colors.primary else colors.onSurface
    val style = if (highlighted) ButtonStylePreferences.TILE_SOLID else look.tileStyle
    return this
        .size(size)
        .clip(shape)
        .then(
            when (style) {
                ButtonStylePreferences.TILE_SOLID -> Modifier.background(colors.primary)
                ButtonStylePreferences.TILE_OUTLINED -> Modifier.border(
                    BorderStroke(1.5.dp, base.copy(alpha = if (accent) 0.5f else 0.28f)),
                    shape
                )
                ButtonStylePreferences.TILE_NONE -> Modifier
                else -> Modifier.background(base.copy(alpha = if (accent) 0.14f else 0.08f))
            }
        )
}

@Composable
fun kateTileTint(highlighted: Boolean = false): Color {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    if (highlighted || look.tileStyle == ButtonStylePreferences.TILE_SOLID) return colors.onPrimary
    return if (look.buttonIconColor == ButtonStylePreferences.COLOR_ACCENT) colors.primary else colors.onSurface
}

@Composable
fun KateRowArrow(modifier: Modifier = Modifier) {
    if (!LocalLookAndFeel.current.rowArrows) return
    KateIcon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = buttonTint(LocalClintColors.current.secondaryText),
        modifier = modifier
    )
}

@Composable
fun KateNavIcon(selected: Boolean, icon: ImageVector, contentDescription: String?) {
    if (LocalLookAndFeel.current.navIndicator == ButtonStylePreferences.NAV_DOT) {
        Box(contentAlignment = Alignment.Center) {
            KateIcon(imageVector = icon, contentDescription = contentDescription)
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(LocalContentColor.current)
                )
            }
        }
    } else {
        KateIcon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
fun KatePlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseSize: Dp = 72.dp,
    followSize: Boolean = true,
    allowGlow: Boolean = true,
    enabled: Boolean = true
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val shapeKey = look.buttonPlayShape
    val fill = look.buttonPlayFill
    val bare = shapeKey == ButtonStylePreferences.SHAPE_NONE
    val shape = playButtonShape(shapeKey)
    val buttonSize = if (followSize) baseSize * playSizeScale(look.buttonPlaySize) else baseSize
    val solid = fill == ButtonStylePreferences.FILL_SOLID || fill == ButtonStylePreferences.FILL_GRADIENT
    val glow = allowGlow && look.buttonPlayGlow && !bare
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, look.buttonPress)
    val gradientEnd = remember(colors.primary) { shiftedHue(colors.primary) }
    val glowColor = colors.primary
    val iconTint = if (solid && !bare) colors.onPrimary else colors.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .then(
                if (glow && !solid) {
                    Modifier.drawBehind {
                        val radius = size.minDimension * 0.85f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.34f), Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            radius = radius
                        )
                    }
                } else {
                    Modifier
                }
            )
            .then(
                if (glow && solid) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = glowColor.copy(alpha = 0.5f),
                        spotColor = glowColor.copy(alpha = 0.5f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .then(
                when {
                    bare -> Modifier
                    fill == ButtonStylePreferences.FILL_GRADIENT ->
                        Modifier.background(Brush.linearGradient(listOf(colors.primary, gradientEnd)))
                    fill == ButtonStylePreferences.FILL_SOLID -> Modifier.background(colors.primary)
                    fill == ButtonStylePreferences.FILL_SOFT -> Modifier.background(colors.primary.copy(alpha = 0.16f))
                    else -> Modifier.border(BorderStroke(2.dp, colors.primary), shape)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = pressIndication(look.buttonPress),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        PlayPauseIcon(
            isPlaying = isPlaying,
            tint = iconTint,
            iconSize = if (bare) buttonSize * 0.7f else buttonSize * 0.445f
        )
    }
}

@Composable
fun KateControlButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalClintColors.current.onSurface,
    iconSize: Dp = 34.dp,
    slotSize: Dp = 48.dp,
    highlighted: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    enabled: Boolean = true,
    extra: @Composable BoxScope.() -> Unit = {}
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val style = look.buttonSideStyle
    val styled = style != ButtonStylePreferences.SIDE_PLAIN
    val shape = if (styled) sideButtonShape(look.buttonPlayShape) else CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, look.buttonPress)
    val accent = colors.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(slotSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .then(
                when (style) {
                    ButtonStylePreferences.SIDE_SOFT -> Modifier.background(
                        if (highlighted) accent.copy(alpha = 0.16f) else colors.onSurface.copy(alpha = 0.08f)
                    )
                    ButtonStylePreferences.SIDE_OUTLINED -> Modifier.border(
                        BorderStroke(
                            1.5.dp,
                            if (highlighted) accent.copy(alpha = 0.7f) else colors.onSurface.copy(alpha = 0.24f)
                        ),
                        shape
                    )
                    else -> Modifier
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
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = buttonTint(tint),
            modifier = Modifier.size(if (styled) minOf(iconSize, slotSize * 0.6f) else iconSize)
        )
        extra()
    }
}

@Composable
fun KateMiniPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ringed: Boolean = false,
    enabled: Boolean = true
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val style = look.miniButtonStyle
    val plain = style == ButtonStylePreferences.MINI_PLAIN
    val shape = sideButtonShape(look.buttonPlayShape)
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, look.buttonPress)
    val inner = when {
        plain -> 48.dp
        ringed -> 32.dp
        else -> 38.dp
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = pressIndication(look.buttonPress),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(inner)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .then(
                    when (style) {
                        ButtonStylePreferences.MINI_FILLED -> Modifier.clip(shape).background(colors.primary)
                        ButtonStylePreferences.MINI_OUTLINED -> Modifier.clip(shape).border(BorderStroke(1.5.dp, colors.primary), shape)
                        else -> Modifier
                    }
                )
        ) {
            PlayPauseIcon(
                isPlaying = isPlaying,
                tint = when (style) {
                    ButtonStylePreferences.MINI_FILLED -> colors.onPrimary
                    ButtonStylePreferences.MINI_OUTLINED -> colors.primary
                    else -> buttonTint(colors.onSurface)
                },
                iconSize = if (plain) 26.dp else inner * 0.62f
            )
        }
    }
}

@Composable
fun KateMiniIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, look.buttonPress)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = pressIndication(look.buttonPress),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = buttonTint(colors.onSurface),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun KateMiniControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    playModifier: Modifier = Modifier,
    ringed: Boolean = false,
    enabled: Boolean = true
) {
    val buttons = LocalLookAndFeel.current.miniButtons
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (buttons == ButtonStylePreferences.MINI_BUTTONS_ALL) {
            KateMiniIconButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.player_previous),
                onClick = onPrevious,
                enabled = enabled
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = playModifier) {
            KateMiniPlayButton(isPlaying = isPlaying, onClick = onPlayPause, ringed = ringed, enabled = enabled)
        }
        if (buttons != ButtonStylePreferences.MINI_BUTTONS_PLAY) {
            KateMiniIconButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.player_next),
                onClick = onNext,
                enabled = enabled
            )
        }
    }
}
