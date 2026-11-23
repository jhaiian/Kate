package com.jhaiian.kate.data

object ArtistNames {
    private const val TOKEN_MARK = '\u0001'
    private val TokenPattern = Regex("$TOKEN_MARK(\\d+)$TOKEN_MARK")
    private const val TRIM_CHARS = "()[] "

    fun split(raw: String, config: TagConfig = TagSettings.config): List<String> {
        if (raw.isBlank()) return emptyList()
        var text = raw
        val kept = ArrayList<String>()
        for (name in config.unsplitByLength) {
            var start = text.indexOf(name, ignoreCase = true)
            while (start >= 0) {
                val token = "$TOKEN_MARK${kept.size}$TOKEN_MARK"
                kept.add(text.substring(start, start + name.length))
                text = text.substring(0, start) + token + text.substring(start + name.length)
                start = text.indexOf(name, startIndex = start + token.length, ignoreCase = true)
            }
        }
        return config.artistPattern.split(text)
            .map { part -> TokenPattern.replace(part) { match -> kept[match.groupValues[1].toInt()] } }
            .map { it.trim { char -> char.isWhitespace() || char in TRIM_CHARS } }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }

    fun splitGenres(raw: String, config: TagConfig = TagSettings.config): List<String> {
        if (raw.isBlank()) return emptyList()
        return config.genrePattern.split(raw)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }

    fun key(name: String): String = name.trim().lowercase()
}
