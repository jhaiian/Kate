package com.jhaiian.kate.data

object Lyricsfile {
    private const val MIN_PAUSE_MS = 200L

    private class Line(val indent: Int, val text: String)

    private class Cursor(val lines: List<Line>) {
        var index = 0
        val current: Line? get() = lines.getOrNull(index)
    }

    fun toLrc(document: String): String? {
        val root = runCatching { parse(document) }.getOrNull() as? Map<*, *> ?: return null
        val lines = root["lines"] as? List<*> ?: return null
        val timed = lines.mapNotNull { it as? Map<*, *> }.mapNotNull { line ->
            val start = number(line["start_ms"]) ?: return@mapNotNull null
            Triple(start, line, number(line["end_ms"]))
        }.sortedBy { it.first }
        if (timed.isEmpty()) return null
        val output = timed.mapIndexed { index, (start, line, lineEnd) ->
            val nextLine = timed.getOrNull(index + 1)?.first
            val words = (line["words"] as? List<*>).orEmpty().mapNotNull { it as? Map<*, *> }
            val body = if (words.isEmpty()) {
                (line["text"] as? String).orEmpty().trim()
            } else {
                val builder = StringBuilder()
                words.forEachIndexed { position, word ->
                    val text = word["text"] as? String ?: ""
                    val wordStart = number(word["start_ms"])
                    val wordEnd = number(word["end_ms"]) ?: if (position == words.lastIndex) lineEnd else null
                    val nextStart = words.drop(position + 1).firstNotNullOfOrNull { number(it["start_ms"]) } ?: nextLine
                    if (wordStart != null) builder.append('<').append(tag(wordStart)).append('>')
                    builder.append(text)
                    if (wordStart != null && wordEnd != null && wordEnd > wordStart &&
                        (nextStart == null || nextStart - wordEnd >= MIN_PAUSE_MS)
                    ) {
                        val trimmed = builder.length - (text.length - text.trimEnd().length)
                        val spaces = builder.substring(trimmed)
                        builder.setLength(trimmed)
                        builder.append('<').append(tag(wordEnd)).append('>').append(spaces)
                    }
                }
                builder.toString().trim()
            }
            "[${tag(start)}]$body"
        }
        return output.joinToString("\n").takeIf { it.isNotBlank() }
    }

    fun hasWords(document: String): Boolean {
        val root = runCatching { parse(document) }.getOrNull() as? Map<*, *> ?: return false
        return (root["lines"] as? List<*>).orEmpty().any { line ->
            ((line as? Map<*, *>)?.get("words") as? List<*>).orEmpty().isNotEmpty()
        }
    }

    private fun tag(timeMs: Long): String {
        val safe = timeMs.coerceAtLeast(0L)
        return "%02d:%02d.%02d".format(java.util.Locale.US, safe / 60_000L, (safe / 1_000L) % 60L, (safe % 1_000L) / 10L)
    }

    private fun number(value: Any?): Long? = when (value) {
        is String -> value.trim().toDoubleOrNull()?.toLong()
        else -> null
    }

    fun parse(document: String): Any? {
        val lines = document.replace("\r\n", "\n").split('\n').mapNotNull { raw ->
            val content = raw.trimEnd()
            val trimmed = content.trimStart()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed == "---" || trimmed == "...") {
                if (trimmed.isEmpty()) Line(-1, "") else null
            } else {
                Line(content.length - trimmed.length, trimmed)
            }
        }
        val cursor = Cursor(lines)
        skipBlank(cursor)
        val first = cursor.current ?: return null
        return parseNode(cursor, first.indent)
    }

    private fun skipBlank(cursor: Cursor) {
        while (cursor.current?.indent == -1) cursor.index++
    }

    private fun parseNode(cursor: Cursor, indent: Int): Any? {
        skipBlank(cursor)
        val line = cursor.current ?: return null
        return if (isItem(line.text)) parseSequence(cursor, line.indent) else parseMapping(cursor, indent)
    }

    private fun isItem(text: String): Boolean = text == "-" || text.startsWith("- ")

    private fun parseSequence(cursor: Cursor, indent: Int): List<Any?> {
        val result = ArrayList<Any?>()
        while (true) {
            skipBlank(cursor)
            val line = cursor.current ?: break
            if (line.indent != indent || !isItem(line.text)) break
            val rest = if (line.text == "-") "" else line.text.substring(2)
            val contentIndent = indent + 2 + (rest.length - rest.trimStart().length)
            val content = rest.trimStart()
            if (content.isEmpty()) {
                cursor.index++
                skipBlank(cursor)
                val next = cursor.current
                result.add(if (next != null && next.indent > indent) parseNode(cursor, next.indent) else null)
            } else if (keyOf(content) != null || isItem(content)) {
                val mutable = cursor.lines.toMutableList()
                mutable[cursor.index] = Line(contentIndent, content)
                val nested = Cursor(mutable)
                nested.index = cursor.index
                result.add(parseNode(nested, contentIndent))
                cursor.index = nested.index
            } else {
                cursor.index++
                result.add(scalar(content, cursor, indent))
            }
        }
        return result
    }

    private fun keyOf(text: String): Pair<String, String>? {
        if (text.startsWith("'") || text.startsWith("\"")) {
            val quote = text[0]
            var position = 1
            while (position < text.length) {
                if (text[position] == quote) {
                    if (quote == '\'' && position + 1 < text.length && text[position + 1] == '\'') {
                        position += 2
                        continue
                    }
                    break
                }
                if (quote == '"' && text[position] == '\\') position++
                position++
            }
            if (position >= text.length) return null
            val after = text.substring(position + 1)
            if (!(after.startsWith(":") && (after.length == 1 || after[1] == ' '))) return null
            return unquote(text.substring(0, position + 1)) to after.substring(1).trim()
        }
        var position = 0
        while (position < text.length) {
            if (text[position] == ':' && (position + 1 == text.length || text[position + 1] == ' ')) {
                return text.substring(0, position).trim() to text.substring(position + 1).trim()
            }
            if (text[position] == ' ' && position + 1 < text.length && text[position + 1] == '#') return null
            position++
        }
        return null
    }

    private fun parseMapping(cursor: Cursor, indent: Int): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        while (true) {
            skipBlank(cursor)
            val line = cursor.current ?: break
            if (line.indent != indent || isItem(line.text)) break
            val (key, rest) = keyOf(line.text) ?: break
            cursor.index++
            result[key] = when {
                rest.isEmpty() -> {
                    skipBlank(cursor)
                    val next = cursor.current
                    when {
                        next == null -> null
                        next.indent > indent -> parseNode(cursor, next.indent)
                        next.indent == indent && isItem(next.text) -> parseSequence(cursor, indent)
                        else -> null
                    }
                }
                rest.startsWith("|") || rest.startsWith(">") -> blockScalar(cursor, indent, rest.startsWith(">"))
                else -> scalar(rest, cursor, indent)
            }
        }
        return result
    }

    private fun blockScalar(cursor: Cursor, parentIndent: Int, folded: Boolean): String {
        val collected = ArrayList<Line>()
        while (true) {
            val line = cursor.current ?: break
            if (line.indent != -1 && line.indent <= parentIndent) break
            collected.add(line)
            cursor.index++
        }
        while (collected.isNotEmpty() && collected.last().indent == -1) collected.removeAt(collected.size - 1)
        val base = collected.filter { it.indent >= 0 }.minOfOrNull { it.indent } ?: return ""
        val texts = collected.map { if (it.indent < 0) "" else " ".repeat(it.indent - base) + it.text }
        return if (folded) texts.joinToString(" ") else texts.joinToString("\n")
    }

    private fun scalar(first: String, cursor: Cursor, parentIndent: Int): Any? {
        val trimmed = first.trim()
        if (trimmed == "[]") return emptyList<Any?>()
        if (trimmed == "{}") return emptyMap<String, Any?>()
        if (trimmed.startsWith("'") || trimmed.startsWith("\"")) {
            var text = trimmed
            while (!closed(text) && cursor.current != null) {
                val next = cursor.current ?: break
                if (next.indent != -1 && next.indent <= parentIndent) break
                text = if (next.indent == -1) text + "\n" else (if (text.endsWith("\n")) text + next.text else text + " " + next.text)
                cursor.index++
            }
            return unquote(text)
        }
        val plain = stripComment(trimmed)
        val builder = StringBuilder(plain)
        while (true) {
            val next = cursor.current ?: break
            if (next.indent == -1 || next.indent <= parentIndent || isItem(next.text) || keyOf(next.text) != null) break
            builder.append(' ').append(stripComment(next.text))
            cursor.index++
        }
        val value = builder.toString()
        return if (value == "null" || value == "~") null else value
    }

    private fun stripComment(text: String): String {
        val index = text.indexOf(" #")
        return if (index >= 0) text.substring(0, index).trimEnd() else text
    }

    private fun closed(text: String): Boolean {
        if (text.length < 2) return false
        val quote = text[0]
        var position = 1
        while (position < text.length) {
            val char = text[position]
            if (quote == '"' && char == '\\') {
                position += 2
                continue
            }
            if (char == quote) {
                if (quote == '\'' && position + 1 < text.length && text[position + 1] == '\'') {
                    position += 2
                    continue
                }
                return true
            }
            position++
        }
        return false
    }

    private fun unquote(text: String): String {
        if (text.isEmpty()) return text
        val quote = text[0]
        if (quote != '\'' && quote != '"') return text
        val body = StringBuilder()
        var position = 1
        while (position < text.length) {
            val char = text[position]
            if (char == quote) {
                if (quote == '\'' && position + 1 < text.length && text[position + 1] == '\'') {
                    body.append('\'')
                    position += 2
                    continue
                }
                break
            }
            if (quote == '"' && char == '\\' && position + 1 < text.length) {
                val escape = text[position + 1]
                when (escape) {
                    'n' -> body.append('\n')
                    't' -> body.append('\t')
                    '"' -> body.append('"')
                    '\\' -> body.append('\\')
                    '/' -> body.append('/')
                    '0' -> body.append('\u0000')
                    ' ' -> body.append(' ')
                    '\n' -> Unit
                    'u' -> {
                        val hex = text.substring(position + 2, (position + 6).coerceAtMost(text.length))
                        hex.toIntOrNull(16)?.let { body.append(it.toChar()) }
                        position += 4
                    }
                    'x' -> {
                        val hex = text.substring(position + 2, (position + 4).coerceAtMost(text.length))
                        hex.toIntOrNull(16)?.let { body.append(it.toChar()) }
                        position += 2
                    }
                    else -> body.append(escape)
                }
                position += 2
                continue
            }
            body.append(char)
            position++
        }
        return body.toString()
    }
}
