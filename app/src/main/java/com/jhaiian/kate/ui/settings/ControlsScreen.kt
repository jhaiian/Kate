package com.jhaiian.kate.ui.settings

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.SystemControls
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.theme.AppIcons
import com.jhaiian.kate.ui.theme.AppLanguage
import com.jhaiian.kate.ui.theme.LocalClintColors

private enum class ControlSlot { NOTIFICATION_1, NOTIFICATION_2, SINGLE, DOUBLE, TRIPLE, LONG }

@Composable
private fun controlLabel(action: String): String = stringResource(
    when (action) {
        SystemControls.PLAY_PAUSE -> R.string.action_play_pause
        SystemControls.NEXT -> R.string.player_next
        SystemControls.PREVIOUS -> R.string.player_previous
        SystemControls.REWIND -> R.string.player_rewind_10
        SystemControls.FORWARD -> R.string.player_forward_10
        SystemControls.FAVORITE -> R.string.control_favorite
        SystemControls.STOP -> R.string.control_stop
        else -> R.string.action_none
    }
)

@Composable
fun ControlsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { SystemControls.ensureLoaded(context) }
    var slot by remember { mutableStateOf<ControlSlot?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = stringResource(R.string.controls_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.controls_reset_message),
                onConfirm = {
                    confirmReset = false
                    SystemControls.setNotificationButtons(context, SystemControls.NONE, SystemControls.NONE)
                    SystemControls.setHeadset(context, SystemControls.PLAY_PAUSE, SystemControls.NEXT, SystemControls.PREVIOUS, SystemControls.NONE)
                    SystemControls.setAutoHidden(context, emptySet())
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.controls_notification), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.controls_notification_first),
                subtitle = controlLabel(SystemControls.notificationFirst),
                colors = colors,
                onClick = { slot = ControlSlot.NOTIFICATION_1 }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.controls_notification_second),
                subtitle = controlLabel(SystemControls.notificationSecond),
                colors = colors,
                onClick = { slot = ControlSlot.NOTIFICATION_2 }
            )
            Text(
                text = stringResource(R.string.controls_notification_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.controls_headset), colors.primary)
        SettingsCard {
            val rows = listOf(
                Triple(ControlSlot.SINGLE, R.string.controls_headset_single, SystemControls.headsetSingle),
                Triple(ControlSlot.DOUBLE, R.string.controls_headset_double, SystemControls.headsetDouble),
                Triple(ControlSlot.TRIPLE, R.string.controls_headset_triple, SystemControls.headsetTriple),
                Triple(ControlSlot.LONG, R.string.controls_headset_long, SystemControls.headsetLong)
            )
            rows.forEachIndexed { index, (target, title, value) ->
                if (index > 0) RowDivider(colors.divider)
                SettingsRow(
                    icon = if (target == ControlSlot.LONG) Icons.Outlined.TouchApp else Icons.Outlined.Headset,
                    title = stringResource(title),
                    subtitle = controlLabel(value),
                    colors = colors,
                    onClick = { slot = target }
                )
            }
            Text(
                text = stringResource(R.string.controls_headset_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.controls_auto), colors.primary)
        SettingsCard {
            val sections = listOf(
                SystemControls.AUTO_SHUFFLE to R.string.widget_quick_shuffle,
                SystemControls.AUTO_SONGS to R.string.library_category_songs,
                SystemControls.AUTO_ALBUMS to R.string.library_category_albums,
                SystemControls.AUTO_ARTISTS to R.string.library_category_artists,
                SystemControls.AUTO_PLAYLISTS to R.string.library_category_playlists,
                SystemControls.AUTO_FAVORITES to R.string.library_category_favorites
            )
            sections.forEachIndexed { index, (key, label) ->
                if (index > 0) RowDivider(colors.divider)
                SettingsSwitchRow(
                    icon = Icons.Outlined.DirectionsCar,
                    title = stringResource(label),
                    subtitle = stringResource(R.string.controls_auto_subtitle),
                    checked = SystemControls.autoVisible(key),
                    colors = colors,
                    onCheckedChange = { show ->
                        val hidden = if (show) SystemControls.autoHidden - key else SystemControls.autoHidden + key
                        SystemControls.setAutoHidden(context, hidden)
                    }
                )
            }
        }
    }

    val current = slot
    if (current != null) {
        val notification = current == ControlSlot.NOTIFICATION_1 || current == ControlSlot.NOTIFICATION_2
        val options = if (notification) SystemControls.NotificationActions else SystemControls.HeadsetActions
        val selected = when (current) {
            ControlSlot.NOTIFICATION_1 -> SystemControls.notificationFirst
            ControlSlot.NOTIFICATION_2 -> SystemControls.notificationSecond
            ControlSlot.SINGLE -> SystemControls.headsetSingle
            ControlSlot.DOUBLE -> SystemControls.headsetDouble
            ControlSlot.TRIPLE -> SystemControls.headsetTriple
            ControlSlot.LONG -> SystemControls.headsetLong
        }
        TextOptionSheet(
            title = stringResource(if (notification) R.string.controls_notification else R.string.controls_headset),
            options = options.map { it to controlLabel(it) },
            current = selected,
            onSelect = { value ->
                when (current) {
                    ControlSlot.NOTIFICATION_1 -> SystemControls.setNotificationButtons(context, value, SystemControls.notificationSecond)
                    ControlSlot.NOTIFICATION_2 -> SystemControls.setNotificationButtons(context, SystemControls.notificationFirst, value)
                    ControlSlot.SINGLE -> SystemControls.setHeadset(context, value, SystemControls.headsetDouble, SystemControls.headsetTriple, SystemControls.headsetLong)
                    ControlSlot.DOUBLE -> SystemControls.setHeadset(context, SystemControls.headsetSingle, value, SystemControls.headsetTriple, SystemControls.headsetLong)
                    ControlSlot.TRIPLE -> SystemControls.setHeadset(context, SystemControls.headsetSingle, SystemControls.headsetDouble, value, SystemControls.headsetLong)
                    ControlSlot.LONG -> SystemControls.setHeadset(context, SystemControls.headsetSingle, SystemControls.headsetDouble, SystemControls.headsetTriple, value)
                }
                slot = null
            },
            onDismiss = { slot = null }
        )
    }
}

@Composable
internal fun AppIdentitySection() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var language by remember { mutableStateOf(AppLanguage.get(context)) }
    var icon by remember { mutableStateOf(AppIcons.current(context)) }
    var languageSheet by remember { mutableStateOf(false) }
    val languageLabels = mapOf(
        AppLanguage.SYSTEM to stringResource(R.string.language_system),
        "en" to "English",
        "zh-CN" to "简体中文",
        "fil" to "Filipino",
        "hi" to "हिन्दी",
        "id" to "Bahasa Indonesia",
        "pt-BR" to "Português (Brasil)",
        "ru" to "Русский",
        "es" to "Español",
        "vi" to "Tiếng Việt",
        "fr" to "Français",
        "de" to "Deutsch",
        "ar" to "العربية"
    )
    val iconLabels = mapOf(
        AppIcons.DEFAULT to stringResource(R.string.app_icon_default),
        "midnight" to stringResource(R.string.app_icon_midnight),
        "sunset" to stringResource(R.string.app_icon_sunset),
        "ocean" to stringResource(R.string.app_icon_ocean),
        "mint" to stringResource(R.string.app_icon_mint),
        "light" to stringResource(R.string.app_icon_light)
    )
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.app_identity_title), colors.primary)
    SettingsCard {
        SettingsRow(
            icon = Icons.Outlined.Language,
            title = stringResource(R.string.language_title),
            subtitle = languageLabels[language].orEmpty(),
            colors = colors,
            onClick = { languageSheet = true }
        )
        RowDivider(colors.divider)
        Text(
            text = stringResource(R.string.app_icon_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AppIcons.Variants.forEach { variant ->
                val selected = variant == icon
                val (background, mark) = AppIcons.Swatches[variant] ?: (0xFF1A1226 to 0xFF8B5CF6)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clickable {
                            if (!selected) {
                                AppIcons.apply(context, variant)
                                icon = variant
                            }
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(background))
                            .border(BorderStroke(if (selected) 3.dp else 1.dp, if (selected) colors.primary else colors.divider), CircleShape)
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(mark)))
                    }
                    Text(
                        text = iconLabels[variant].orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = kateChoiceText(selected),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.app_icon_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
    if (languageSheet) {
        TextOptionSheet(
            title = stringResource(R.string.language_title),
            options = AppLanguage.Tags.map { it to languageLabels[it].orEmpty() },
            current = language,
            onSelect = { tag ->
                languageSheet = false
                language = tag
                (context as? Activity)?.let { AppLanguage.set(it, tag) }
            },
            onDismiss = { languageSheet = false }
        )
    }
}
