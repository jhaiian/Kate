package com.jhaiian.kate.ui.settings

import androidx.compose.material.icons.outlined.TravelExplore
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import com.jhaiian.kate.update.UpdateChecker
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.theme.ClintColors
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
fun SettingsScreen(
    onLookAndFeelClick: () -> Unit,
    onAudioClick: () -> Unit,
    onTagsClick: () -> Unit,
    onOnlineClick: () -> Unit,
    onCrashLogsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackupClick: () -> Unit = {},
    onUpdatesClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onOpenRoute: (String) -> Unit = {},
    onControlsClick: () -> Unit = {}
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var showArtistImagesConsent by remember { mutableStateOf(false) }
    var showClearHistory by remember { mutableStateOf(false) }
    val historyClearedMessage = stringResource(R.string.clear_history_done)
    val deletedMessage = stringResource(R.string.artist_images_deleted)
    val versionName = remember { appVersionName(context) }
    var query by rememberSaveable { mutableStateOf("") }
    val searchEngine = rememberSettingsSearchEngine()
    val searchResult = remember(searchEngine, query) { searchEngine.search(query) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var homeTarget by remember { mutableIntStateOf(-1) }
    LaunchedEffect(homeTarget, query) {
        if (homeTarget >= 0 && query.isBlank()) {
            listState.animateScrollToItem(homeTarget)
            homeTarget = -1
        }
    }
    val openHit: (SettingsHit) -> Unit = { hit ->
        keyboard?.hide()
        focusManager.clearFocus()
        SettingsSpotlight.request(hit.route, hit.title)
        query = ""
        if (hit.route == "settings") {
            homeTarget = when (hit.titleRes) {
                R.string.section_artist_images,
                R.string.row_artist_images_download,
                R.string.row_artist_images_wifi_only,
                R.string.row_artist_images_delete -> 2
                R.string.section_listening,
                R.string.row_record_history_title,
                R.string.row_clear_history_title -> 3
                else -> 0
            }
        } else {
            onOpenRoute(hit.route)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp)
                    )
                    SettingsSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        onSubmit = {
                            val top = searchResult.hits.firstOrNull()
                            if (top != null) openHit(top) else keyboard?.hide()
                        }
                    )
                }
            }
            if (query.isNotBlank()) {
                item {
                    SettingsSearchResults(result = searchResult, onOpen = openHit)
                }
            } else {
                item {
                    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                        SupportKateCard(colors = colors, onClick = onSupportClick)
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBackground)
                        ) {
                            SettingsRow(
                                icon = Icons.Outlined.Palette,
                                title = stringResource(R.string.section_look_and_feel),
                                subtitle = stringResource(R.string.look_and_feel_summary),
                                colors = colors,
                                onClick = onLookAndFeelClick
                            )
                            RowDivider(colors.divider)
                            SettingsRow(
                                icon = Icons.Outlined.GraphicEq,
                                title = stringResource(R.string.section_audio),
                                subtitle = stringResource(R.string.audio_summary),
                                colors = colors,
                                onClick = onAudioClick
                            )
                            RowDivider(colors.divider)
                            SettingsRow(
                                icon = Icons.Outlined.SettingsRemote,
                                title = stringResource(R.string.controls_title),
                                subtitle = stringResource(R.string.controls_summary),
                                colors = colors,
                                onClick = onControlsClick
                            )
                            RowDivider(colors.divider)
                            SettingsRow(
                                icon = Icons.Outlined.Sell,
                                title = stringResource(R.string.section_tags),
                                subtitle = stringResource(R.string.tags_summary),
                                colors = colors,
                                onClick = onTagsClick
                            )
                            RowDivider(colors.divider)
                            SettingsRow(
                                icon = Icons.Outlined.TravelExplore,
                                title = stringResource(R.string.online_title),
                                subtitle = stringResource(R.string.online_summary),
                                colors = colors,
                                onClick = onOnlineClick
                            )
                            RowDivider(colors.divider)
                            SettingsRow(
                                icon = Icons.Outlined.SettingsBackupRestore,
                                title = stringResource(R.string.backup_title),
                                subtitle = stringResource(R.string.backup_summary),
                                colors = colors,
                                onClick = onBackupClick
                            )
                            RowDivider(colors.divider)
                            if (UpdateChecker.supported) {
                                SettingsRow(
                                    icon = Icons.Outlined.SystemUpdate,
                                    title = stringResource(R.string.update_settings_title),
                                    subtitle = stringResource(R.string.update_settings_summary),
                                    colors = colors,
                                    onClick = onUpdatesClick
                                )
                                RowDivider(colors.divider)
                            }
                            SettingsRow(
                                icon = Icons.Outlined.BugReport,
                                title = stringResource(R.string.row_crash_logs_title),
                                subtitle = stringResource(R.string.row_crash_logs_summary),
                                colors = colors,
                                onClick = onCrashLogsClick
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                item {
                    ArtistImagesSection(
                        onEnableRequest = { showArtistImagesConsent = true },
                        onDisable = { ArtistImageStore.setEnabled(context, false) },
                        onWifiOnlyChange = { ArtistImageStore.setWifiOnly(context, it) },
                        onDeleteDownloaded = {
                            ArtistImageStore.deleteDownloaded(context)
                            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    ListeningHistorySection(
                        onEnabledChange = { ListeningHistory.setEnabled(context, it) },
                        onClearRequest = { showClearHistory = true }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                    ) {
                        SettingsRow(
                            icon = Icons.Outlined.Info,
                            title = stringResource(R.string.settings_about_title),
                            subtitle = stringResource(R.string.settings_about_summary, versionName),
                            colors = colors,
                            onClick = onAboutClick
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        ClearHistoryDialog(
            visible = showClearHistory,
            onConfirm = {
                showClearHistory = false
                ListeningHistory.clear(context)
                Toast.makeText(context, historyClearedMessage, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showClearHistory = false }
        )
        ArtistImagesConsentDialog(
            visible = showArtistImagesConsent,
            onConfirm = {
                showArtistImagesConsent = false
                ArtistImageStore.setEnabled(context, true)
            },
            onDismiss = { showArtistImagesConsent = false }
        )
    }
}

@Composable
private fun SupportKateCard(colors: ClintColors, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(lerp(colors.cardBackground, colors.primary, 0.16f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.primary)
        ) {
            KateIcon(
                imageVector = Icons.Filled.VolunteerActivism,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)) {
            Text(
                text = stringResource(R.string.support_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = stringResource(R.string.support_summary),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateIcon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
internal fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    colors: ClintColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .settingsSpotlight(title)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateRowArrow()
    }
}

@Composable
internal fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    colors: ClintColors,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    subtitleLines: Int = Int.MAX_VALUE
) {
    val haptics = LocalKateHaptics.current
    val change: (Boolean) -> Unit = {
        haptics.toggle(it)
        onCheckedChange(it)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .settingsSpotlight(title)
            .clickable(enabled = enabled) { change(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(if (enabled) 1f else 0.6f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = subtitleLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        ClintSwitch(
            checked = checked,
            onCheckedChange = change,
            enabled = enabled
        )
    }
}

@Composable
internal fun SectionHeader(text: String, tint: Color) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = tint,
        modifier = Modifier.padding(start = 24.dp, bottom = 10.dp).settingsSpotlight(text)
    )
}

@Composable
internal fun RowDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 68.dp)
            .background(color)
    )
}
