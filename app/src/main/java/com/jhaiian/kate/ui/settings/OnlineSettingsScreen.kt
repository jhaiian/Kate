package com.jhaiian.kate.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.TravelExplore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LrcLib
import com.jhaiian.kate.data.LyricsSource
import com.jhaiian.kate.data.TagLookup
import com.jhaiian.kate.data.TagSource
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.player.LrcLibConsentDialog
import com.jhaiian.kate.ui.tags.TagLookupConsentDialog
import com.jhaiian.kate.ui.theme.LocalClintColors

@Composable
fun OnlineSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var showLrcLibConsent by remember { mutableStateOf(false) }
    var showTagLookupConsent by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = stringResource(R.string.online_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    OnlineLyricsSection(
                        enabled = LrcLib.isEnabled(context),
                        onEnableRequest = { showLrcLibConsent = true },
                        onDisable = { LrcLib.setEnabled(context, false) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    OnlineTagsSection(
                        enabled = TagLookup.isEnabled(context),
                        onEnableRequest = { showTagLookupConsent = true },
                        onDisable = { TagLookup.setEnabled(context, false) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        LrcLibConsentDialog(
            visible = showLrcLibConsent,
            onConfirm = {
                showLrcLibConsent = false
                LrcLib.setEnabled(context, true)
            },
            onDismiss = { showLrcLibConsent = false }
        )
        TagLookupConsentDialog(
            visible = showTagLookupConsent,
            onConfirm = {
                showTagLookupConsent = false
                TagLookup.setEnabled(context, true)
            },
            onDismiss = { showTagLookupConsent = false }
        )
    }
}

@Composable
private fun OnlineLyricsSection(enabled: Boolean, onEnableRequest: () -> Unit, onDisable: () -> Unit) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
        SectionHeader(text = stringResource(R.string.lrclib_section), tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            SettingsSwitchRow(
                icon = Icons.Outlined.TravelExplore,
                title = stringResource(R.string.lrclib_setting_title),
                subtitle = stringResource(R.string.lrclib_setting_summary),
                checked = enabled,
                colors = colors,
                onCheckedChange = { checked -> if (checked) onEnableRequest() else onDisable() }
            )
            if (enabled) {
                val context = LocalContext.current
                listOf(
                    Triple(LyricsSource.AMLL, R.string.online_source_amll_title, R.string.online_source_amll_summary),
                    Triple(LyricsSource.LRCRED, R.string.online_source_lrcred_title, R.string.online_source_lrcred_summary),
                    Triple(LyricsSource.LYRICSPLUS, R.string.online_source_lyricsplus_title, R.string.online_source_lyricsplus_summary),
                    Triple(LyricsSource.NETEASE, R.string.online_source_netease_title, R.string.online_source_netease_summary),
                    Triple(LyricsSource.KUGOU, R.string.online_source_kugou_title, R.string.online_source_kugou_summary),
                    Triple(LyricsSource.LRCLIB, R.string.online_source_lrclib_title, R.string.online_source_lrclib_summary)
                ).forEach { (source, title, summary) ->
                    var on by remember(source) { mutableStateOf(LrcLib.isSourceEnabled(context, source)) }
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.LibraryMusic,
                        title = stringResource(title),
                        subtitle = stringResource(summary),
                        checked = on,
                        colors = colors,
                        onCheckedChange = {
                            on = it
                            LrcLib.setSourceEnabled(context, source, it)
                        }
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.lrclib_setting_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp)
        )
    }
}

@Composable
private fun OnlineTagsSection(enabled: Boolean, onEnableRequest: () -> Unit, onDisable: () -> Unit) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
        SectionHeader(text = stringResource(R.string.tag_lookup_section), tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            SettingsSwitchRow(
                icon = Icons.Outlined.Sell,
                title = stringResource(R.string.tag_lookup_setting_title),
                subtitle = stringResource(R.string.tag_lookup_setting_summary),
                checked = enabled,
                colors = colors,
                onCheckedChange = { checked -> if (checked) onEnableRequest() else onDisable() }
            )
            if (enabled) {
                val context = LocalContext.current
                listOf(
                    Triple(TagSource.MUSICBRAINZ, R.string.tag_source_musicbrainz, R.string.tag_source_musicbrainz_summary),
                    Triple(TagSource.DEEZER, R.string.tag_source_deezer, R.string.tag_source_deezer_summary),
                    Triple(TagSource.ITUNES, R.string.tag_source_itunes, R.string.tag_source_itunes_summary)
                ).forEach { (source, title, summary) ->
                    var on by remember(source) { mutableStateOf(TagLookup.isSourceEnabled(context, source)) }
                    RowDivider(colors.divider)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.TravelExplore,
                        title = stringResource(title),
                        subtitle = stringResource(summary),
                        checked = on,
                        colors = colors,
                        onCheckedChange = {
                            on = it
                            TagLookup.setSourceEnabled(context, source, it)
                        }
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.tag_lookup_setting_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp)
        )
    }
}
