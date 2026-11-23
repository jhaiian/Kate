package com.jhaiian.kate.ui.settings

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DocumentViewer
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.SquircleShape
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.theme.LocalClintColors

private const val AUTHOR_URL = "https://github.com/jhaiian"
private const val GITHUB_URL = "https://github.com/jhaiian/Kate"
private const val CLINT_GECKO_URL = "https://github.com/jhaiian/ClintGecko"
private const val LUCKY_URL = "https://github.com/jhaiian/Lucky"
private const val CLINT_BROWSER_URL = "https://github.com/jhaiian/ClintBrowser"
private const val GPL_URL = "https://www.gnu.org/licenses/gpl-3.0.html"
private const val KEEP_ANDROID_OPEN_URL = "https://keepandroidopen.org"
private const val CONTACT_EMAIL_URL = "mailto:jhaiian@proton.me"
private const val DISCORD_URL = "https://discord.gg/cNZgT2AEQR"
private const val REDDIT_URL = "https://www.reddit.com/r/KateMusic"
private const val APACHE_URL = "https://www.apache.org/licenses/LICENSE-2.0"
private const val MIT_URL = "https://opensource.org/licenses/MIT"
private const val LGPL_URL = "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"
private const val PATREON_URL = "https://patreon.com/Jhaiian"
private const val KOFI_URL = "https://ko-fi.com/jhaiian"
private const val PAYPAL_URL = "https://paypal.me/jhaiian"
private const val INSTAPAY_URL = "https://github.com/jhaiian/jhaiian/blob/main/docs/Insta%20Pay.jpg"

private class UsedLibrary(
    @StringRes val label: Int,
    @StringRes val usage: Int,
    val url: String,
    @StringRes val license: Int,
    val licenseUrl: String
)

private val UsedLibraries = listOf(
    UsedLibrary(R.string.about_library_androidx, R.string.about_library_androidx_usage, "https://developer.android.com/jetpack/androidx", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_compose, R.string.about_library_compose_usage, "https://developer.android.com/jetpack/compose", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_media3, R.string.about_library_media3_usage, "https://developer.android.com/guide/topics/media/media3", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_glance, R.string.about_library_glance_usage, "https://developer.android.com/jetpack/compose/glance", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_oboe, R.string.about_library_oboe_usage, "https://github.com/google/oboe", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_material, R.string.about_library_material_usage, "https://github.com/material-components/material-components-android", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_taglib, R.string.about_library_taglib_usage, "https://taglib.org", R.string.about_license_lgpl, LGPL_URL),
    UsedLibrary(R.string.about_library_taglib_android, R.string.about_library_taglib_android_usage, "https://github.com/Kyant0/taglib", R.string.about_license_view, "https://github.com/Kyant0/taglib/blob/main/LICENSE"),
    UsedLibrary(R.string.about_library_coil, R.string.about_library_coil_usage, "https://github.com/coil-kt/coil", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_materialkolor, R.string.about_library_materialkolor_usage, "https://github.com/jordond/materialkolor", R.string.about_license_mit, MIT_URL),
    UsedLibrary(R.string.about_library_coroutines, R.string.about_library_coroutines_usage, "https://github.com/Kotlin/kotlinx.coroutines", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_markwon, R.string.about_library_markwon_usage, "https://github.com/noties/Markwon", R.string.about_license_apache, APACHE_URL),
    UsedLibrary(R.string.about_library_okhttp, R.string.about_library_okhttp_usage, "https://github.com/square/okhttp", R.string.about_license_apache, APACHE_URL)
)

private class MoreApp(
    @StringRes val name: Int,
    @StringRes val summary: Int,
    val url: String
)

private val MoreApps = listOf(
    MoreApp(R.string.about_app_clint_gecko, R.string.about_app_clint_gecko_summary, CLINT_GECKO_URL),
    MoreApp(R.string.about_app_lucky, R.string.about_app_lucky_summary, LUCKY_URL),
    MoreApp(R.string.about_app_clint_browser, R.string.about_app_clint_browser_summary, CLINT_BROWSER_URL)
)

private fun packageInfo(context: Context): PackageInfo? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
}.getOrNull()

fun appVersionName(context: Context): String = packageInfo(context)?.versionName.orEmpty()

@Composable
private fun rememberOpenUrl(): (String) -> Unit {
    val uriHandler = LocalUriHandler.current
    return remember<(String) -> Unit>(uriHandler) { { url -> runCatching { uriHandler.openUri(url) } } }
}

@Composable
internal fun InfoPage(title: String, onBack: () -> Unit, content: LazyListScope.() -> Unit) {
    val colors = LocalClintColors.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current + 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.library_back),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        content()
    }
}

private fun LazyListScope.infoCard(@StringRes header: Int, content: @Composable ColumnScope.() -> Unit) {
    item {
        val colors = LocalClintColors.current
        Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(top = 16.dp)) {
            SectionHeader(stringResource(header), colors.primary)
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier, monospace: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = LocalClintColors.current.onSurface,
        fontFamily = if (monospace) FontFamily.Monospace else null,
        modifier = modifier
    )
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = LocalClintColors.current.primary,
        modifier = modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(4.dp)
    )
}

@Composable
private fun LabeledLink(label: String, link: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalClintColors.current.secondaryText,
            modifier = Modifier.weight(1f)
        )
        LinkText(link, onClick, Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun CardDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(1.dp)
            .background(LocalClintColors.current.divider)
    )
}

@Composable
private fun AppLogo() {
    Box(modifier = Modifier.size(80.dp).clip(SquircleShape)) {
        Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize().scale(1.5f))
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize().scale(1.5f))
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val openUrl = rememberOpenUrl()
    val info = remember { packageInfo(context) }
    val versionInfo = stringResource(
        R.string.about_version_info,
        info?.versionName.orEmpty(),
        info?.let { PackageInfoCompat.getLongVersionCode(it) } ?: 0L,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
    )

    Box(modifier = modifier.fillMaxSize()) {
        InfoPage(title = stringResource(R.string.settings_about_title), onBack = onBack) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    AppLogo()
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = stringResource(R.string.about_tagline),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            infoCard(R.string.about_section_about) {
                BodyText(stringResource(R.string.about_description))
                CardDivider()
                LabeledLink(stringResource(R.string.about_created_by), stringResource(R.string.about_author_link)) { openUrl(AUTHOR_URL) }
            }
            infoCard(R.string.about_section_version) {
                BodyText(versionInfo, monospace = true)
            }
            infoCard(R.string.about_section_license) {
                BodyText(stringResource(R.string.about_license_text), Modifier.padding(bottom = 8.dp))
                LinkText(stringResource(R.string.about_license_link), { openUrl(GPL_URL) })
            }
            infoCard(R.string.about_section_source) {
                BodyText(stringResource(R.string.about_source_text), Modifier.padding(bottom = 8.dp))
                LinkText(stringResource(R.string.about_source_link), { openUrl(GITHUB_URL) })
            }
            infoCard(R.string.about_section_keep_android_open) {
                BodyText(stringResource(R.string.about_keep_android_open_text), Modifier.padding(bottom = 8.dp))
                LinkText(stringResource(R.string.about_keep_android_open_link), { openUrl(KEEP_ANDROID_OPEN_URL) })
            }
            infoCard(R.string.about_section_community) {
                BodyText(stringResource(R.string.about_community_text), Modifier.padding(bottom = 8.dp))
                LabeledLink(stringResource(R.string.about_community_discord_label), stringResource(R.string.about_community_discord_url)) { openUrl(DISCORD_URL) }
                CardDivider()
                LabeledLink(stringResource(R.string.about_community_reddit_label), stringResource(R.string.about_community_reddit_url)) { openUrl(REDDIT_URL) }
            }
            infoCard(R.string.about_section_contact) {
                LabeledLink(stringResource(R.string.about_contact_email_label), stringResource(R.string.about_contact_email)) { openUrl(CONTACT_EMAIL_URL) }
            }
            infoCard(R.string.about_section_more_apps) {
                MoreApps.forEachIndexed { index, app ->
                    if (index > 0) CardDivider()
                    LinkText(stringResource(app.name), { openUrl(app.url) })
                    Text(
                        text = stringResource(app.summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            infoCard(R.string.about_section_libraries) {
                UsedLibraries.forEachIndexed { index, library ->
                    if (index > 0) CardDivider()
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        LinkText(stringResource(library.label), { openUrl(library.url) }, Modifier.weight(1f))
                        Text(
                            text = stringResource(library.license),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openUrl(library.licenseUrl) }
                                .padding(4.dp)
                        )
                    }
                    Text(
                        text = stringResource(library.usage),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            infoCard(R.string.about_section_attribution) {
                LabeledLink(stringResource(R.string.about_attribution_text), stringResource(R.string.about_legal_view)) {
                    DocumentViewer.show(R.string.about_section_attribution, DocumentViewer.ATTRIBUTION_URL)
                }
            }
        }
    }
}

@Composable
private fun DonateButton(icon: ImageVector, label: String, primary: Boolean, onClick: () -> Unit) {
    KateActionButton(
        label = label,
        icon = icon,
        primary = primary,
        allowIconOnly = false,
        minHeight = 52.dp,
        onClick = onClick,
        modifier = Modifier.padding(top = 12.dp).fillMaxWidth()
    )
}

@Composable
fun SupportScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val openUrl = rememberOpenUrl()

    Box(modifier = modifier.fillMaxSize()) {
        InfoPage(title = stringResource(R.string.support_title), onBack = onBack) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.support_heading),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        BodyText(stringResource(R.string.support_body))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DonateButton(Icons.Outlined.FavoriteBorder, stringResource(R.string.support_patreon), true) { openUrl(PATREON_URL) }
                    DonateButton(Icons.Outlined.LocalCafe, stringResource(R.string.support_kofi), false) { openUrl(KOFI_URL) }
                    DonateButton(Icons.Outlined.Payments, stringResource(R.string.support_paypal), false) { openUrl(PAYPAL_URL) }
                    DonateButton(Icons.Outlined.QrCode2, stringResource(R.string.support_instapay), false) { openUrl(INSTAPAY_URL) }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                    ) {
                        SettingsRow(
                            icon = Icons.Outlined.Groups,
                            title = stringResource(R.string.support_supporters_title),
                            subtitle = stringResource(R.string.support_supporters_summary),
                            colors = colors,
                            onClick = { DocumentViewer.show(R.string.support_supporters_title, DocumentViewer.SUPPORTERS_URL) }
                        )
                    }
                }
            }
        }
    }
}
