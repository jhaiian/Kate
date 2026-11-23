package com.jhaiian.kate.data

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class TimedWord(val text: String, val startMs: Long, val endMs: Long?, val spaceAfter: Boolean)

class TimedLine(val startMs: Long, val words: List<TimedWord>, val prefix: String)

object WordLyrics {
    private const val MIN_PAUSE_MS = 200L
    private const val TT_NS = "http://www.w3.org/ns/ttml"
    private const val TTM_NS = "http://www.w3.org/ns/ttml#metadata"
    private val TimePattern = Regex("""^(?:(?:(\d+):)?(\d+):)?(\d+(?:\.\d+)?)s?$""")
    private val YrcLine = Regex("""^\[(\d+),(\d+)]""")
    private val YrcWord = Regex("""^(.*?)\((\d+),(\d+),\d+\)""")
    private val KrcLine = Regex("""^\[(\d+),(\d+)]""")
    private val KrcWord = Regex("""<(\d+),(\d+),-?\d+>([^<]*)""")

    fun toLrc(lines: List<TimedLine>): String? {
        val sorted = lines.filter { it.words.isNotEmpty() }.sortedBy { it.startMs }
        if (sorted.isEmpty()) return null
        return sorted.mapIndexed { index, line ->
            val nextLine = sorted.getOrNull(index + 1)?.startMs
            val body = StringBuilder()
            line.words.forEachIndexed { position, word ->
                val text = word.text.trim()
                if (text.isEmpty()) return@forEachIndexed
                val nextStart = line.words.getOrNull(position + 1)?.startMs ?: nextLine
                body.append('<').append(tag(word.startMs)).append('>').append(text)
                val end = word.endMs
                if (end != null && end > word.startMs && (nextStart == null || nextStart - end >= MIN_PAUSE_MS)) {
                    body.append('<').append(tag(end)).append('>')
                }
                if (word.spaceAfter && position < line.words.lastIndex) body.append(' ')
            }
            "[${tag(line.startMs)}]${line.prefix}${body.toString().trim()}"
        }.joinToString("\n")
    }

    fun ttmlToLrc(document: String): String? = runCatching { toLrc(parseTtml(document)) }.getOrNull()

    fun yrcToLrc(document: String): String? = runCatching { toLrc(parseYrc(document)) }.getOrNull()

    fun krcToLrc(document: String): String? = runCatching { toLrc(parseKrc(document)) }.getOrNull()

    fun parseKrc(document: String): List<TimedLine> {
        val result = ArrayList<TimedLine>()
        for (raw in document.replace("\r\n", "\n").split('\n')) {
            val line = raw.trim().trimStart('\uFEFF')
            val head = KrcLine.find(line) ?: continue
            val lineStart = head.groupValues[1].toLongOrNull() ?: continue
            val words = ArrayList<TimedWord>()
            for (match in KrcWord.findAll(line, head.range.last + 1)) {
                val text = match.groupValues[3]
                if (text.isBlank()) {
                    if (text.isNotEmpty() && words.isNotEmpty()) {
                        val last = words.removeAt(words.lastIndex)
                        words.add(TimedWord(last.text, last.startMs, last.endMs, true))
                    }
                    continue
                }
                val start = lineStart + (match.groupValues[1].toLongOrNull() ?: continue)
                val end = start + (match.groupValues[2].toLongOrNull() ?: 0L)
                words.add(TimedWord(text, start, end, text.endsWith(" ")))
            }
            if (words.isEmpty()) continue
            val background = isBackground(words)
            result.add(TimedLine(lineStart, if (background) trimParentheses(words) else words, if (background) "bg: " else ""))
        }
        return result
    }

    fun parseYrc(document: String): List<TimedLine> {
        val result = ArrayList<TimedLine>()
        for (raw in document.replace("\r\n", "\n").split('\n')) {
            val line = raw.trim()
            val head = YrcLine.find(line) ?: continue
            val lineStart = head.groupValues[1].toLongOrNull() ?: continue
            var rest = line.substring(head.range.last + 1)
            val words = ArrayList<TimedWord>()
            var pendingStart = -1L
            var pendingEnd = -1L
            while (true) {
                val match = YrcWord.find(rest) ?: break
                val text = match.groupValues[1]
                if (pendingStart >= 0 && text.isNotEmpty()) {
                    words.add(TimedWord(text, pendingStart, pendingEnd, text.endsWith(" ")))
                }
                val start = match.groupValues[2].toLongOrNull() ?: break
                val duration = match.groupValues[3].toLongOrNull() ?: 0L
                pendingStart = start
                pendingEnd = start + duration
                rest = rest.substring(match.range.last + 1)
            }
            if (pendingStart >= 0 && rest.isNotEmpty()) words.add(TimedWord(rest, pendingStart, pendingEnd, false))
            if (words.isEmpty()) continue
            val background = isBackground(words)
            result.add(TimedLine(lineStart, if (background) trimParentheses(words) else words, if (background) "bg: " else ""))
        }
        return result
    }

    fun parseTtml(document: String): List<TimedLine> {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        runCatching { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        runCatching { factory.isExpandEntityReferences = false }
        val doc = factory.newDocumentBuilder().parse(ByteArrayInputStream(document.toByteArray(Charsets.UTF_8)))
        val paragraphs = doc.getElementsByTagNameNS(TT_NS, "p").let { list ->
            if (list.length > 0) list else doc.getElementsByTagName("p")
        }
        val result = ArrayList<TimedLine>()
        for (index in 0 until paragraphs.length) {
            val p = paragraphs.item(index) as? Element ?: continue
            val agent = attribute(p, TTM_NS, "agent")
            val prefix = if (agent == "v2") "v2: " else ""
            val main = ArrayList<TimedWord>()
            val background = ArrayList<TimedWord>()
            collectWords(p, main, background, inBackground = false)
            val lineStart = parseTime(attribute(p, null, "begin"))
            if (main.isNotEmpty()) {
                result.add(TimedLine(main.first().startMs, main, prefix))
            } else if (lineStart != null) {
                val text = p.textContent?.trim().orEmpty()
                if (text.isNotEmpty()) result.add(TimedLine(lineStart, listOf(TimedWord(text, lineStart, parseTime(attribute(p, null, "end")), false)), prefix))
            }
            if (background.isNotEmpty()) {
                result.add(TimedLine(background.first().startMs, trimParentheses(background), "bg: "))
            }
        }
        return result
    }

    private fun collectWords(parent: Element, main: MutableList<TimedWord>, background: MutableList<TimedWord>, inBackground: Boolean) {
        val children = parent.childNodes
        for (index in 0 until children.length) {
            val node = children.item(index)
            val target = if (inBackground) background else main
            when (node.nodeType) {
                Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> {
                    val text = node.nodeValue.orEmpty()
                    if (text.isNotEmpty() && text.isBlank() && !text.contains('\n') && target.isNotEmpty()) {
                        val last = target.removeAt(target.size - 1)
                        target.add(TimedWord(last.text, last.startMs, last.endMs, true))
                    }
                }
                Node.ELEMENT_NODE -> {
                    val element = node as Element
                    val role = attribute(element, TTM_NS, "role")
                    when {
                        role == "x-translation" || role == "x-roman" -> Unit
                        role == "x-bg" -> collectWords(element, main, background, inBackground = true)
                        attribute(element, null, "ruby") == "container" || attribute(element, "http://www.w3.org/ns/ttml#styling", "ruby") == "container" -> {
                            rubyWord(element)?.let { target.add(it) }
                        }
                        hasElementChildren(element) -> collectWords(element, main, background, inBackground)
                        else -> {
                            val start = parseTime(attribute(element, null, "begin"))
                            val text = element.textContent.orEmpty()
                            if (start != null && text.isNotBlank()) {
                                target.add(TimedWord(text, start, parseTime(attribute(element, null, "end")), text.endsWith(" ")))
                            } else if (text.isNotBlank() && target.isNotEmpty()) {
                                val last = target.removeAt(target.size - 1)
                                target.add(TimedWord(last.text + text, last.startMs, last.endMs, last.spaceAfter))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun rubyWord(container: Element): TimedWord? {
        var base = ""
        var start: Long? = parseTime(attribute(container, null, "begin"))
        var end: Long? = parseTime(attribute(container, null, "end"))
        val all = container.getElementsByTagName("*")
        for (index in 0 until all.length) {
            val element = all.item(index) as? Element ?: continue
            val ruby = attribute(element, "http://www.w3.org/ns/ttml#styling", "ruby") ?: attribute(element, null, "ruby")
            if (ruby == "base") base = element.textContent.orEmpty()
            val begin = parseTime(attribute(element, null, "begin"))
            val finish = parseTime(attribute(element, null, "end"))
            if (begin != null && (start == null || begin < start)) start = begin
            if (finish != null && (end == null || finish > end)) end = finish
        }
        val first = start ?: return null
        return base.takeIf { it.isNotBlank() }?.let { TimedWord(it, first, end, false) }
    }

    private fun hasElementChildren(element: Element): Boolean {
        val children = element.childNodes
        for (index in 0 until children.length) if (children.item(index).nodeType == Node.ELEMENT_NODE) return true
        return false
    }

    private fun attribute(element: Element, namespace: String?, name: String): String? {
        if (namespace != null) {
            val value = element.getAttributeNS(namespace, name)
            if (!value.isNullOrEmpty()) return value
        }
        val attributes = element.attributes
        for (index in 0 until attributes.length) {
            val attr = attributes.item(index)
            val local = attr.localName ?: attr.nodeName.substringAfter(':')
            if (local == name && (namespace == null || attr.namespaceURI == null || attr.namespaceURI == namespace)) {
                return attr.nodeValue
            }
        }
        return null
    }

    fun parseTime(value: String?): Long? {
        val match = TimePattern.matchEntire(value?.trim() ?: return null) ?: return null
        val hours = match.groupValues[1].toLongOrNull() ?: 0L
        val minutes = match.groupValues[2].toLongOrNull() ?: 0L
        val seconds = match.groupValues[3].toDoubleOrNull() ?: return null
        return hours * 3_600_000L + minutes * 60_000L + (seconds * 1_000.0).toLong()
    }

    private fun isBackground(words: List<TimedWord>): Boolean {
        val first = words.first().text.trimStart()
        val last = words.last().text.trimEnd()
        return (first.startsWith("(") || first.startsWith("（")) && (last.endsWith(")") || last.endsWith("）"))
    }

    fun trimParentheses(words: List<TimedWord>): List<TimedWord> {
        if (words.isEmpty()) return words
        val result = words.toMutableList()
        val first = result.first()
        val firstText = first.text.trimStart().removePrefix("(").removePrefix("（")
        result[0] = TimedWord(firstText, first.startMs, first.endMs, first.spaceAfter)
        val lastIndex = result.lastIndex
        val last = result[lastIndex]
        val lastText = last.text.trimEnd().removeSuffix(")").removeSuffix("）")
        result[lastIndex] = TimedWord(lastText, last.startMs, last.endMs, last.spaceAfter)
        return result.filter { it.text.isNotBlank() }
    }

    private fun tag(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        return "%02d:%02d.%02d".format(java.util.Locale.US, safe / 60_000L, (safe / 1_000L) % 60L, (safe % 1_000L) / 10L)
    }
}
