package com.jhaiian.kate.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.update.UpdateChecker
import java.text.Normalizer
import java.util.Locale

class SettingsHit(
    val title: String,
    val breadcrumb: String,
    val description: String,
    val route: String,
    @StringRes val titleRes: Int
)

class SettingsSearchResult(val hits: List<SettingsHit>, val partial: Boolean, val words: List<String>) {
    companion object {
        val Empty = SettingsSearchResult(emptyList(), false, emptyList())
    }
}

internal class SearchFolded(val text: String, val map: IntArray)

internal class SearchField(val folded: String, val tokens: List<String>, val compact: String, val weight: Float)

internal class SearchDoc(val hit: SettingsHit, val title: String, val subtitle: String, val fields: List<SearchField>)

private val combiningMarks = Regex("\\p{Mn}+")
private val formatArgs = Regex("%(\\d+\\$)?[sdf]")
private val spaces = Regex(" +")

private fun fold(source: String): SearchFolded {
    val out = StringBuilder(source.length)
    val map = ArrayList<Int>(source.length)
    var index = 0
    while (index < source.length) {
        val codePoint = source.codePointAt(index)
        val count = Character.charCount(codePoint)
        val piece = Normalizer.normalize(source.substring(index, index + count), Normalizer.Form.NFD)
            .replace(combiningMarks, "")
            .lowercase(Locale.ROOT)
        for (ch in piece) {
            out.append(if (ch.isLetterOrDigit()) ch else ' ')
            map.add(index)
        }
        index += count
    }
    return SearchFolded(out.toString(), map.toIntArray())
}

private fun foldText(source: String): String = fold(source).text.replace(spaces, " ").trim()

private fun isCjk(ch: Char): Boolean = Character.isIdeographic(ch.code) ||
    Character.UnicodeScript.of(ch.code).let {
        it == Character.UnicodeScript.HIRAGANA || it == Character.UnicodeScript.KATAKANA || it == Character.UnicodeScript.HANGUL ||
            it == Character.UnicodeScript.THAI
    }

private fun field(text: String, weight: Float): SearchField? {
    val folded = foldText(text.replace(formatArgs, " "))
    if (folded.isEmpty()) return null
    val tokens = folded.split(' ').filter { it.isNotEmpty() }
    return SearchField(folded, tokens, folded.replace(" ", ""), weight)
}

private fun editDistance(a: String, b: String, limit: Int): Int {
    if (kotlin.math.abs(a.length - b.length) > limit) return limit + 1
    var previous2 = IntArray(b.length + 1)
    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)
    for (i in 1..a.length) {
        current[0] = i
        var rowMin = current[0]
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            var value = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
            if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                value = minOf(value, previous2[j - 2] + 1)
            }
            current[j] = value
            if (value < rowMin) rowMin = value
        }
        if (rowMin > limit) return limit + 1
        val recycled = previous2
        previous2 = previous
        previous = current
        current = recycled
    }
    return previous[b.length]
}

private fun typoMatch(word: String, token: String): Boolean {
    if (word.length < 4 || token.length < word.length - 1) return false
    val limit = if (word.length <= 5) 1 else 2
    if (word[0] != token[0] && word.length < 6) return false
    if (editDistance(word, token, limit) <= limit) return true
    return token.length > word.length && editDistance(word, token.take(word.length), limit) <= limit
}

private fun quality(word: String, field: SearchField, allowTypos: Boolean): Float {
    var best = 0f
    for (token in field.tokens) {
        val value = when {
            token == word -> 1f
            token.startsWith(word) -> if (word.length >= 2 || token.length <= 2) 0.82f else 0.45f
            word.length >= 3 && token.contains(word) -> 0.58f
            allowTypos && typoMatch(word, token) -> 0.46f
            else -> 0f
        }
        if (value > best) {
            best = value
            if (best == 1f) break
        }
    }
    if (best < 0.55f && word.length >= 4 && field.compact.contains(word)) best = 0.55f
    if (best < 0.75f && word.any(::isCjk) && field.folded.contains(word)) best = 0.75f
    return best
}

class SettingsSearchEngine private constructor(
    private val docs: List<SearchDoc>,
    private val aliases: List<Set<String>>
) {
    private fun expansions(word: String): Set<String> {
        val out = HashSet<String>()
        val cjk = word.any(::isCjk)
        for (group in aliases) {
            val hit = group.any { term ->
                term == word || (word.length >= 3 && term.startsWith(word)) || (cjk && term.length >= 2 && word.contains(term))
            }
            if (hit) out.addAll(group)
        }
        out.remove(word)
        return out
    }

    private fun wordScore(doc: SearchDoc, word: String, alternates: Set<String>): Float {
        var best = 0f
        for (field in doc.fields) {
            val direct = quality(word, field, allowTypos = true) * field.weight
            if (direct > best) best = direct
        }
        if (alternates.isNotEmpty()) {
            for (field in doc.fields) {
                for (term in alternates) {
                    val viaAlias = quality(term, field, allowTypos = false) * field.weight * 0.7f
                    if (viaAlias > best) best = viaAlias
                }
            }
        }
        return best
    }

    fun search(query: String): SettingsSearchResult {
        val phrase = foldText(query)
        if (phrase.isEmpty()) return SettingsSearchResult.Empty
        val words = phrase.split(' ').filter { it.isNotEmpty() }.distinct()
        val alternates = words.associateWith { expansions(it) }
        val full = ArrayList<Pair<SearchDoc, Float>>()
        val near = ArrayList<Pair<SearchDoc, Float>>()
        for (doc in docs) {
            var total = 0f
            var matched = 0
            for (word in words) {
                val score = wordScore(doc, word, alternates.getValue(word))
                if (score > 0f) {
                    matched++
                    total += score
                }
            }
            if (matched == 0) continue
            total += when {
                doc.title == phrase -> 8f
                doc.title.startsWith(phrase) -> 4f
                doc.title.contains(phrase) -> 2f
                doc.subtitle.contains(phrase) -> 1f
                else -> 0f
            }
            total -= doc.title.length * 0.004f
            if (matched == words.size) full.add(doc to total)
            else if (words.size >= 2 && matched >= words.size - 1) near.add(doc to total * 0.6f)
        }
        val partial = full.isEmpty()
        val chosen = if (partial) near else full
        val hits = chosen.sortedByDescending { it.second }.take(50).map { it.first.hit }
        return SettingsSearchResult(hits, partial && hits.isNotEmpty(), words)
    }

    companion object {
        private fun parentOf(route: String): Int = when {
            route.startsWith("settings/look_and_feel/") -> R.string.section_look_and_feel
            route.startsWith("settings/audio/") || route.startsWith("settings/equalizer") -> R.string.section_audio
            else -> 0
        }

        private fun aliasGroups(context: Context): List<Set<String>> =
            context.resources.getStringArray(R.array.settings_search_aliases).map { line ->
                line.split(',').flatMap { foldText(it).split(' ') }.filter { it.isNotEmpty() }.toSet()
            }.filter { it.size > 1 }

        fun build(context: Context): SettingsSearchEngine {
            val english = context.createConfigurationContext(
                Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) }
            )
            val englishUi = context.resources.configuration.locales[0].language == Locale.ENGLISH.language
            val docs = SettingsIndex
                .filter { UpdateChecker.supported || it.route != "settings/updates" }
                .map { entry ->
                    val title = context.getString(entry.titleRes)
                    val subtitle = if (entry.subtitleRes != 0) context.getString(entry.subtitleRes).replace(formatArgs, "").trim() else ""
                    val parentRes = parentOf(entry.route)
                    val crumbs = listOfNotNull(
                        if (parentRes != 0) context.getString(parentRes) else null,
                        context.getString(entry.pageRes)
                    ).distinct()
                    val fields = mutableListOf<SearchField?>(
                        field(title, 3f),
                        field(subtitle, 1.3f),
                        field(crumbs.joinToString(" "), 0.9f)
                    )
                    if (!englishUi) {
                        fields += field(english.getString(entry.titleRes), 2.2f)
                        if (entry.subtitleRes != 0) fields += field(english.getString(entry.subtitleRes), 0.9f)
                        fields += field(english.getString(entry.pageRes), 0.7f)
                    }
                    SearchDoc(
                        hit = SettingsHit(title, crumbs.joinToString(" › "), subtitle, entry.route, entry.titleRes),
                        title = foldText(title),
                        subtitle = foldText(subtitle),
                        fields = fields.filterNotNull()
                    )
                }
                .distinctBy { it.hit.title to it.hit.route }
            val aliases = if (englishUi) aliasGroups(context) else aliasGroups(context) + aliasGroups(english)
            return SettingsSearchEngine(docs, aliases)
        }
    }
}

@Composable
internal fun rememberSettingsSearchEngine(): SettingsSearchEngine {
    val context = LocalContext.current
    val locales = LocalConfiguration.current.locales.toLanguageTags()
    return remember(context, locales) { SettingsSearchEngine.build(context) }
}

private fun highlighted(text: String, words: List<String>, style: SpanStyle): AnnotatedString {
    val folded = fold(text)
    val marks = BooleanArray(text.length)
    for (word in words) {
        if (word.isEmpty()) continue
        var from = folded.text.indexOf(word)
        while (from >= 0) {
            val atStart = from == 0 || folded.text[from - 1] == ' '
            if (atStart || word.length >= 3 || word.any(::isCjk)) {
                val start = folded.map[from]
                val lastSource = folded.map[from + word.length - 1]
                val end = lastSource + Character.charCount(text.codePointAt(lastSource))
                for (i in start until end.coerceAtMost(text.length)) marks[i] = true
            }
            from = folded.text.indexOf(word, from + 1)
        }
    }
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val on = marks[i]
            var j = i
            while (j < text.length && marks[j] == on) j++
            if (on) withStyle(style) { append(text.substring(i, j)) } else append(text.substring(i, j))
            i = j
        }
    }
}

private fun iconFor(route: String): ImageVector = when {
    route.startsWith("settings/look_and_feel/theme") || route.startsWith("settings/look_and_feel/custom_themes") -> Icons.Outlined.Palette
    route.startsWith("settings/look_and_feel/player") -> Icons.Outlined.PlayCircle
    route.startsWith("settings/look_and_feel/buttons") -> Icons.Outlined.TouchApp
    route.startsWith("settings/look_and_feel/library") -> Icons.Outlined.LibraryMusic
    route.startsWith("settings/look_and_feel/navigation") -> Icons.Outlined.Navigation
    route.startsWith("settings/look_and_feel/display") -> Icons.Outlined.Smartphone
    route.startsWith("settings/look_and_feel/motion") -> Icons.Outlined.Animation
    route.startsWith("settings/look_and_feel/styles") -> Icons.Outlined.Style
    route.startsWith("settings/look_and_feel/visualizer") -> Icons.Outlined.Equalizer
    route.startsWith("settings/look_and_feel") -> Icons.Outlined.Palette
    route.startsWith("settings/equalizer") -> Icons.Outlined.Tune
    route.startsWith("settings/audio") -> Icons.Outlined.GraphicEq
    route.startsWith("settings/controls") -> Icons.Outlined.SettingsRemote
    route.startsWith("settings/tags") -> Icons.Outlined.Sell
    route.startsWith("settings/online") -> Icons.Outlined.TravelExplore
    route.startsWith("settings/backup") -> Icons.Outlined.SettingsBackupRestore
    route.startsWith("settings/updates") -> Icons.Outlined.SystemUpdate
    route.startsWith("settings/crash_logs") -> Icons.Outlined.BugReport
    route.startsWith("settings/about") -> Icons.Outlined.Info
    route.startsWith("settings/support") -> Icons.Outlined.VolunteerActivism
    else -> Icons.Outlined.Settings
}

@Composable
internal fun SettingsSearchField(query: String, onQueryChange: (String) -> Unit, onSubmit: () -> Unit) {
    val colors = LocalClintColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.settings_search_hint), color = colors.secondaryText) },
        leadingIcon = { KateIcon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = colors.secondaryText) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                KateIconButton(onClick = { onQueryChange("") }) {
                    KateIcon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.search_clear), tint = colors.secondaryText)
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.divider,
            focusedContainerColor = colors.cardBackground,
            unfocusedContainerColor = colors.cardBackground,
            cursorColor = colors.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
    )
}

@Composable
internal fun SettingsSearchResults(result: SettingsSearchResult, onOpen: (SettingsHit) -> Unit) {
    val colors = LocalClintColors.current
    val matchStyle = SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)
    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
        if (result.hits.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Box(modifier = Modifier.kateIconTile(56.dp), contentAlignment = Alignment.Center) {
                    KateIcon(imageVector = Icons.Outlined.SearchOff, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(28.dp))
                }
                Text(
                    text = stringResource(R.string.settings_search_empty),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(R.string.settings_search_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            Text(
                text = if (result.partial) {
                    stringResource(R.string.settings_search_closest)
                } else {
                    pluralStringResource(R.plurals.settings_search_results, result.hits.size, result.hits.size)
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp)
            )
            SettingsCard {
                result.hits.forEachIndexed { index, hit ->
                    if (index > 0) RowDivider(colors.divider)
                    val title = remember(hit, result.words, matchStyle) { highlighted(hit.title, result.words, matchStyle) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(hit) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Box(modifier = Modifier.kateIconTile(40.dp), contentAlignment = Alignment.Center) {
                            KateIcon(
                                imageVector = iconFor(hit.route),
                                contentDescription = null,
                                tint = kateTileTint(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = hit.breadcrumb,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (hit.description.isNotEmpty()) {
                                Text(
                                    text = hit.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.secondaryText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        KateRowArrow()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
