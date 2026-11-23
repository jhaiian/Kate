package com.jhaiian.kate.ui.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.MusicFolder
import com.jhaiian.kate.data.SetupState
import com.jhaiian.kate.data.detectStorageVolumes
import com.jhaiian.kate.data.labelForTreeUri
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.theme.AccentOptions
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.ThemeOptions
import com.jhaiian.kate.ui.theme.resolveClintTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.BreakIterator

private enum class SetupStep { WELCOME, LOOK, LIBRARY, NOTIFICATIONS, DONE }

private data class PageMetrics(val wide: Boolean, val heroHeight: Dp) {
    val align: TextAlign get() = if (wide) TextAlign.Start else TextAlign.Center
}

private val AccentPicks = listOf(
    "material_you", "purple", "indigo", "blue", "cyan", "teal", "emerald", "lime",
    "gold", "orange", "coral", "red", "pink", "lavender", "charcoal"
)

private fun notificationsAllowed(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

@Composable
fun SetupScreen(
    look: LookAndFeelSettings,
    onLookChange: (LookAndFeelSettings) -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val steps = remember {
        SetupStep.entries.filter { it != SetupStep.NOTIFICATIONS || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val step = steps[index.coerceIn(0, steps.lastIndex)]
    val go: (Int) -> Unit = { target ->
        val next = target.coerceIn(0, steps.lastIndex)
        if (next != index) {
            haptics.click()
            index = next
        }
    }
    BackHandler(enabled = index > 0) { go(index - 1) }

    var notificationsGranted by remember { mutableStateOf(notificationsAllowed(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        SetupState.markNotificationsAsked(context)
        notificationsGranted = granted
        if (granted) {
            haptics.confirm()
            scope.launch {
                delay(900)
                if (steps[index] == SetupStep.NOTIFICATIONS) go(index + 1)
            }
        }
    }

    val hasFolders = LibraryStore.folders.isNotEmpty()

    val nextLabel = when (step) {
        SetupStep.WELCOME -> stringResource(R.string.setup_get_started)
        SetupStep.LIBRARY -> stringResource(if (hasFolders) R.string.setup_next else R.string.setup_skip)
        SetupStep.NOTIFICATIONS -> stringResource(if (notificationsGranted) R.string.setup_next else R.string.setup_not_now)
        SetupStep.DONE -> stringResource(R.string.setup_start_listening)
        else -> stringResource(R.string.setup_next)
    }
    val nextPrimary = when (step) {
        SetupStep.LIBRARY -> hasFolders
        SetupStep.NOTIFICATIONS -> notificationsGranted
        else -> true
    }
    val onNext: () -> Unit = {
        when (step) {
            SetupStep.DONE -> {
                haptics.confirm()
                onFinish()
            }
            SetupStep.NOTIFICATIONS -> {
                SetupState.markNotificationsAsked(context)
                go(index + 1)
            }
            else -> go(index + 1)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(LocalClintColors.current.background)
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val wide = maxWidth >= 600.dp && maxWidth > maxHeight
            val heroHeight = if (maxHeight < 560.dp) 190.dp else (maxHeight * 0.4f).coerceAtMost(400.dp)
            val metrics = PageMetrics(wide, heroHeight)
            Column(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        val forward = targetState > initialState
                        val enter = slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 220f)) { full -> if (forward) full / 3 else -full / 3 } +
                            fadeIn(tween(420, delayMillis = 80)) +
                            scaleIn(tween(520, easing = FastOutSlowInEasing), initialScale = 0.94f)
                        val exit = slideOutHorizontally(tween(340)) { full -> if (forward) -full / 4 else full / 4 } +
                            fadeOut(tween(220)) +
                            scaleOut(tween(340), targetScale = 1.04f)
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    label = "setup_step"
                ) { target ->
                    when (steps[target]) {
                        SetupStep.WELCOME -> WelcomePage(metrics)
                        SetupStep.LOOK -> LookPage(metrics, look, onLookChange)
                        SetupStep.LIBRARY -> LibraryPage(metrics)
                        SetupStep.NOTIFICATIONS -> NotificationsPage(
                            metrics = metrics,
                            granted = notificationsGranted,
                            onAllow = {
                                haptics.click()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    runCatching { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                                }
                            }
                        )
                        SetupStep.DONE -> DonePage(metrics, hasFolders)
                    }
                }
                SetupBottomBar(
                    step = step,
                    index = index,
                    count = steps.size,
                    nextLabel = nextLabel,
                    nextPrimary = nextPrimary,
                    onBack = { go(index - 1) },
                    onNext = onNext
                )
            }
        }
    }
}

@Composable
private fun Modifier.rise(order: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(140L + order * 75L)
        progress.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 140f))
    }
    return this.graphicsLayer {
        val v = progress.value
        alpha = v.coerceIn(0f, 1f)
        translationY = (1f - v) * 36.dp.toPx()
    }
}

@Composable
private fun SetupPage(
    metrics: PageMetrics,
    hero: @Composable (Modifier) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val scroll = rememberScrollState()
    if (metrics.wide) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                hero(Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(24.dp))
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .verticalScroll(scroll)
                        .padding(vertical = 24.dp),
                    content = content
                )
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            hero(
                Modifier
                    .fillMaxWidth()
                    .height(metrics.heroHeight)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun PageHeader(title: String, body: String, metrics: PageMetrics) {
    val colors = LocalClintColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = colors.onSurface,
        textAlign = metrics.align,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() }
            .rise(0)
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        color = colors.secondaryText,
        textAlign = metrics.align,
        modifier = Modifier
            .fillMaxWidth()
            .rise(1)
    )
}

private fun graphemes(text: String): List<String> {
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(text)
    val parts = ArrayList<String>()
    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        parts.add(text.substring(start, end))
        start = end
        end = iterator.next()
    }
    return parts
}

@Composable
private fun RevealTitle(text: String, centered: Boolean) {
    val colors = LocalClintColors.current
    val glyphs = remember(text) { graphemes(text) }
    Row(
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = text
                heading()
            }
    ) {
        glyphs.forEachIndexed { i, glyph ->
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(260L + i * 95L)
                progress.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 170f))
            }
            Text(
                text = glyph,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.onSurface,
                modifier = Modifier.graphicsLayer {
                    val v = progress.value
                    alpha = v.coerceIn(0f, 1f)
                    translationY = (1f - v) * 48.dp.toPx()
                }
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colors.primary.copy(alpha = 0.16f))
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText
            )
        }
    }
}

@Composable
private fun AcronymLine(text: String, metrics: PageMetrics, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val styled = remember(text, colors.primary, colors.onSurface) {
        buildAnnotatedString {
            text.split(' ').filter { it.isNotEmpty() }.forEachIndexed { i, word ->
                if (i > 0) append(' ')
                withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.ExtraBold)) {
                    append(word.substring(0, 1))
                }
                withStyle(SpanStyle(color = colors.onSurface)) {
                    append(word.substring(1))
                }
            }
        }
    }
    Text(
        text = styled,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        textAlign = metrics.align,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun WelcomePage(metrics: PageMetrics) {
    val colors = LocalClintColors.current
    SetupPage(metrics, hero = { VinylHero(it) }) {
        Text(
            text = stringResource(R.string.setup_welcome_eyebrow),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            textAlign = metrics.align,
            modifier = Modifier
                .fillMaxWidth()
                .rise(0)
        )
        RevealTitle(stringResource(R.string.app_name), centered = !metrics.wide)
        Spacer(Modifier.height(4.dp))
        AcronymLine(stringResource(R.string.about_tagline), metrics, Modifier.rise(3))
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.setup_welcome_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.secondaryText,
            textAlign = metrics.align,
            modifier = Modifier
                .fillMaxWidth()
                .rise(4)
        )
        Spacer(Modifier.height(22.dp))
        FeatureRow(Icons.Outlined.GraphicEq, stringResource(R.string.setup_feature_sound_title), stringResource(R.string.setup_feature_sound), Modifier.rise(5))
        FeatureRow(Icons.Outlined.Lyrics, stringResource(R.string.setup_feature_lyrics_title), stringResource(R.string.setup_feature_lyrics), Modifier.rise(6))
        FeatureRow(Icons.Outlined.Palette, stringResource(R.string.setup_feature_style_title), stringResource(R.string.setup_feature_style), Modifier.rise(7))
    }
}

@Composable
private fun SectionLabel(text: String, metrics: PageMetrics, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = colors.primary,
        textAlign = metrics.align,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LookPage(metrics: PageMetrics, look: LookAndFeelSettings, onLookChange: (LookAndFeelSettings) -> Unit) {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val systemDark = isSystemInDarkTheme()
    val resolvedTheme = when (look.theme) {
        "light", "dark" -> look.theme
        else -> if (systemDark) "dark" else "light"
    }
    val swatches = remember(resolvedTheme, look.intensity, look.themeTuning) {
        AccentPicks.associateWith { key ->
            runCatching { resolveClintTheme(context, resolvedTheme, key, look.intensity, tuning = look.themeTuning).primary }
                .getOrDefault(Color.Gray)
        }
    }
    SetupPage(metrics, hero = { ThemePreviewHero(it) }) {
        PageHeader(stringResource(R.string.setup_look_title), stringResource(R.string.setup_look_body), metrics)
        Spacer(Modifier.height(24.dp))
        SectionLabel(stringResource(R.string.setup_look_theme), metrics, Modifier.rise(2))
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .rise(3)
        ) {
            ThemeOptions.forEach { option ->
                val icon = when (option.key) {
                    "light" -> Icons.Outlined.LightMode
                    "dark" -> Icons.Outlined.DarkMode
                    else -> Icons.Outlined.BrightnessAuto
                }
                ThemeCard(
                    icon = icon,
                    label = stringResource(option.labelRes),
                    selected = look.theme == option.key,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (look.theme != option.key) {
                            haptics.tick()
                            onLookChange(look.copy(theme = option.key))
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        SectionLabel(stringResource(R.string.setup_look_accent), metrics, Modifier.rise(4))
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, if (metrics.wide) Alignment.Start else Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .rise(5)
        ) {
            AccentPicks.forEach { key ->
                val labelRes = AccentOptions.firstOrNull { it.key == key }?.labelRes ?: return@forEach
                AccentSwatch(
                    color = swatches[key] ?: Color.Gray,
                    selected = look.accent == key,
                    wallpaper = key == "material_you",
                    label = stringResource(labelRes),
                    onClick = {
                        if (look.accent != key) {
                            haptics.tick()
                            onLookChange(look.copy(accent = key))
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        SectionLabel(stringResource(R.string.look_section_album_art), metrics, Modifier.rise(6))
        Spacer(Modifier.height(10.dp))
        ArtworkColorsCard(
            checked = look.appArtworkColors,
            onCheckedChange = { enabled ->
                haptics.toggle(enabled)
                onLookChange(look.copy(appArtworkColors = enabled))
            },
            modifier = Modifier.rise(7)
        )
    }
}

@Composable
private fun ArtworkColorsCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val container by animateColorAsState(
        if (checked) colors.primary.copy(alpha = 0.16f) else colors.cardBackground,
        label = "artwork_card_bg"
    )
    val stroke by animateColorAsState(
        if (checked) colors.primary else colors.divider,
        label = "artwork_card_stroke"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(if (checked) 2.dp else 1.dp, stroke, RoundedCornerShape(20.dp))
            .background(container)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (checked) colors.primary else colors.primary.copy(alpha = 0.16f))
        ) {
            KateIcon(
                imageVector = Icons.Outlined.ColorLens,
                contentDescription = null,
                tint = if (checked) colors.onPrimary else colors.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.row_app_artwork_colors_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = stringResource(R.string.row_app_artwork_colors_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText
            )
        }
        Spacer(Modifier.width(10.dp))
        ClintSwitch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ThemeCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val container by animateColorAsState(
        if (selected) colors.primary.copy(alpha = 0.16f) else colors.cardBackground,
        label = "theme_card_bg"
    )
    val stroke by animateColorAsState(
        if (selected) colors.primary else colors.divider,
        label = "theme_card_stroke"
    )
    val lift by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 300f), label = "theme_card_lift")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = 1f + 0.03f * lift
                scaleY = 1f + 0.03f * lift
            }
            .clip(RoundedCornerShape(20.dp))
            .border(if (selected) 2.dp else 1.dp, stroke, RoundedCornerShape(20.dp))
            .background(container)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp)
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.secondaryText,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer { rotationZ = -20f * (1f - lift) }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.onSurface else colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    wallpaper: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val ring by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 320f), label = "swatch_ring")
    val onColor = if (color.luminance() > 0.5f) Color.Black else Color.White
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .drawBehind {
                val outer = size.minDimension / 2f
                drawCircle(
                    color = color.copy(alpha = ring),
                    radius = outer - 1.5.dp.toPx(),
                    style = Stroke(2.5.dp.toPx())
                )
                drawCircle(
                    color = color,
                    radius = outer * (0.86f - 0.14f * ring)
                )
            }
    ) {
        if (wallpaper) {
            KateIcon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = onColor.copy(alpha = 0.85f * (1f - ring)),
                modifier = Modifier.size(18.dp)
            )
        }
        KateIcon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = onColor,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = ring
                    scaleY = ring
                    alpha = ring.coerceIn(0f, 1f)
                    rotationZ = -90f * (1f - ring)
                }
        )
    }
}

private fun persistTree(context: Context, uri: Uri): String {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
    return runCatching { DocumentFile.fromTreeUri(context, uri)?.name }.getOrNull()
        ?: uri.lastPathSegment
        ?: context.getString(R.string.setup_library_folder_fallback)
}

@Composable
private fun LibraryPage(metrics: PageMetrics) {
    val context = LocalContext.current
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val folders = LibraryStore.folders
    val volumes = remember { runCatching { detectStorageVolumes(context) }.getOrDefault(emptyList()) }
    var pendingLabel by rememberSaveable { mutableStateOf("") }

    val treeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val name = persistTree(context, uri)
            LibraryStore.addFolder(context, uri, name, labelForTreeUri(uri))
            haptics.confirm()
        }
    }
    val volumeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val name = persistTree(context, uri)
            LibraryStore.addFolder(context, uri, name, pendingLabel.ifBlank { labelForTreeUri(uri) })
            haptics.confirm()
        }
    }

    SetupPage(metrics, hero = { FloatingNotesHero(Icons.Outlined.LibraryMusic, it) }) {
        PageHeader(stringResource(R.string.setup_library_title), stringResource(R.string.setup_library_body), metrics)
        Spacer(Modifier.height(24.dp))
        KateActionButton(
            label = stringResource(R.string.setup_library_choose),
            icon = Icons.Outlined.CreateNewFolder,
            allowIconOnly = false,
            minHeight = 52.dp,
            onClick = {
                haptics.click()
                runCatching { treeLauncher.launch(null) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .rise(2)
        )
        val available = volumes.filter { volume -> folders.none { it.locationLabel == volume.label } }
        available.forEachIndexed { i, volume ->
            key(volume.id) {
                Spacer(Modifier.height(10.dp))
                KateActionButton(
                    label = stringResource(R.string.setup_library_use_volume, volume.label),
                    icon = Icons.Outlined.SdCard,
                    primary = false,
                    allowIconOnly = false,
                    minHeight = 52.dp,
                    onClick = {
                        haptics.click()
                        pendingLabel = volume.label
                        runCatching { volumeLauncher.launch(volume.intent) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .rise(3 + i)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        AnimatedContent(
            targetState = folders.size,
            transitionSpec = {
                (fadeIn(tween(240)) + scaleIn(spring(dampingRatio = 0.5f, stiffness = 300f), initialScale = 0.8f)) togetherWith fadeOut(tween(140))
            },
            modifier = Modifier
                .fillMaxWidth()
                .rise(4),
            label = "folder_count"
        ) { count ->
            Text(
                text = if (count == 0) {
                    stringResource(R.string.setup_library_empty)
                } else {
                    pluralStringResource(R.plurals.setup_library_ready, count, count)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (count == 0) colors.secondaryText else colors.primary,
                textAlign = metrics.align,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(10.dp))
        folders.forEach { folder ->
            key(folder.uri.toString()) {
                FolderCard(
                    folder = folder,
                    onRemove = {
                        haptics.tick()
                        LibraryStore.removeFolder(context, folder.uri)
                    },
                    modifier = Modifier.rise(0)
                )
            }
        }
    }
}

@Composable
private fun FolderCard(folder: MusicFolder, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary.copy(alpha = 0.16f))
        ) {
            KateIcon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = folder.locationLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        KateIconButton(onClick = onRemove) {
            KateIcon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.folder_picker_remove_content_desc),
                tint = buttonTint(colors.secondaryText)
            )
        }
    }
}

@Composable
private fun NotificationsPage(metrics: PageMetrics, granted: Boolean, onAllow: () -> Unit) {
    val colors = LocalClintColors.current
    SetupPage(metrics, hero = { NotificationHero(it) }) {
        PageHeader(stringResource(R.string.setup_notifications_title), stringResource(R.string.setup_notifications_body), metrics)
        Spacer(Modifier.height(24.dp))
        AnimatedContent(
            targetState = granted,
            transitionSpec = {
                (fadeIn(tween(260)) + scaleIn(spring(dampingRatio = 0.5f, stiffness = 260f), initialScale = 0.85f)) togetherWith
                    (fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.9f))
            },
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .rise(2),
            label = "notification_state"
        ) { on ->
            if (on) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary.copy(alpha = 0.16f))
                        .padding(16.dp)
                ) {
                    KateIcon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.setup_notifications_on),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            } else {
                KateActionButton(
                    label = stringResource(R.string.setup_notifications_allow),
                    icon = Icons.Outlined.NotificationsActive,
                    allowIconOnly = false,
                    minHeight = 52.dp,
                    onClick = onAllow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DonePage(metrics: PageMetrics, hasFolders: Boolean) {
    SetupPage(metrics, hero = { CelebrationHero(it) }) {
        PageHeader(
            stringResource(R.string.setup_done_title),
            stringResource(if (hasFolders) R.string.setup_done_body_ready else R.string.setup_done_body_empty),
            metrics
        )
        Spacer(Modifier.height(22.dp))
        EqualizerBars(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .rise(2)
        )
    }
}

@Composable
private fun StepDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val description = stringResource(R.string.setup_step_counter, current + 1, count)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clearAndSetSemantics { contentDescription = description }
    ) {
        repeat(count) { i ->
            val width by animateDpAsState(
                if (i == current) 26.dp else 8.dp,
                spring(dampingRatio = 0.6f, stiffness = 320f),
                label = "dot_width"
            )
            val tint by animateColorAsState(
                when {
                    i == current -> colors.primary
                    i < current -> colors.primary.copy(alpha = 0.45f)
                    else -> colors.secondaryText.copy(alpha = 0.3f)
                },
                label = "dot_color"
            )
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(tint)
            )
        }
    }
}

@Composable
private fun SetupBottomBar(
    step: SetupStep,
    index: Int,
    count: Int,
    nextLabel: String,
    nextPrimary: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val stacked = step == SetupStep.WELCOME || step == SetupStep.DONE
    val nextIcon = if (step == SetupStep.DONE) Icons.Outlined.PlayArrow else Icons.AutoMirrored.Outlined.ArrowForward
    AnimatedContent(
        targetState = stacked,
        transitionSpec = { fadeIn(tween(300, delayMillis = 120)) togetherWith fadeOut(tween(160)) },
        modifier = Modifier.fillMaxWidth(),
        label = "setup_bar"
    ) { isStacked ->
        if (isStacked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                StepDots(count, index)
                Spacer(Modifier.height(16.dp))
                KateActionButton(
                    label = nextLabel,
                    icon = nextIcon,
                    allowIconOnly = false,
                    minHeight = 56.dp,
                    onClick = onNext,
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    KateActionButton(
                        label = stringResource(R.string.setup_back),
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        primary = false,
                        allowIconOnly = false,
                        onClick = onBack
                    )
                }
                StepDots(count, index, Modifier.padding(horizontal = 12.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    AnimatedContent(
                        targetState = nextLabel to nextPrimary,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(spring(dampingRatio = 0.6f, stiffness = 320f), initialScale = 0.88f)) togetherWith fadeOut(tween(120))
                        },
                        contentAlignment = Alignment.CenterEnd,
                        label = "setup_next"
                    ) { (label, primary) ->
                        KateActionButton(
                            label = label,
                            icon = nextIcon,
                            primary = primary,
                            allowIconOnly = false,
                            onClick = onNext
                        )
                    }
                }
            }
        }
    }
}
