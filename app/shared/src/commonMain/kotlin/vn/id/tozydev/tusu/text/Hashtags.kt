package vn.id.tozydev.tusu.text

private val WHITESPACE_RUN = Regex("\\s+")

// Characters that may sit right before a `#` and still start a tag. `/` and `&` are left out so
// that URL fragments such as `site.com/#top` are not read as tags.
private const val TAG_PREFIX_PUNCTUATION = "([{\"'“‘«¡¿,.;:!?-–—…"

/**
 * Returns the `#tags` in [text], without the hash, in order of first appearance. Tags that differ
 * only by case or diacritics count as one, and the first spelling is kept.
 */
fun parseHashtags(text: String): List<String> {
    val firstSpelling = LinkedHashMap<String, String>()
    var i = 0
    while (i < text.length) {
        if (text[i] == '#' && startsTag(text, i)) {
            var end = i + 1
            while (end < text.length && isTagChar(text[end])) end++
            val token = text.substring(i + 1, end)
            // A token with no letter (`#1`, `#2026`) is a number, not a tag.
            if (token.any { it.isLetter() }) {
                firstSpelling.getOrPut(token.foldDiacritics()) { token }
            }
            i = end
        } else {
            i++
        }
    }
    return firstSpelling.values.toList()
}

/**
 * Returns the label names that [text] mentions as `#name`. A label may contain spaces, so each `#`
 * uses the longest label that follows it. Comparison ignores case and diacritics, and `_` may stand
 * in for a space. A match must end at a word boundary.
 */
fun matchLabelHashtags(text: String, labelNames: Collection<String>): Set<String> {
    val labels = labelNames.mapNotNull { name -> labelKey(name)?.let { key -> name to key } }
    if (labels.isEmpty()) return emptySet()

    val (folded, foldedIndexOf) = foldWithIndex(text)
    val matched = LinkedHashSet<String>()
    for (hash in text.indices) {
        if (text[hash] != '#' || !startsTag(text, hash)) continue
        val start = foldedIndexOf[hash + 1]

        var bestLength = 0
        val best = mutableListOf<String>()
        for ((name, key) in labels) {
            if (matchEnd(folded, start, key) == -1) continue
            when {
                key.length > bestLength -> {
                    bestLength = key.length
                    best.clear()
                    best.add(name)
                }
                key.length == bestLength -> best.add(name)
            }
        }
        matched.addAll(best)
    }
    return matched
}

private fun startsTag(text: String, hash: Int): Boolean {
    if (hash == 0) return true
    val prev = text[hash - 1]
    return prev.isWhitespace() || prev in TAG_PREFIX_PUNCTUATION
}

private fun isTagChar(ch: Char): Boolean =
    ch.isLetterOrDigit() || ch == '_' || ch.code in 0x0300..0x036F

/** Folded form of a label, with `_` and runs of whitespace turned into single spaces. */
private fun labelKey(name: String): String? {
    val key = name.foldDiacritics().replace('_', ' ').replace(WHITESPACE_RUN, " ").trim()
    return key.ifEmpty { null }
}

/**
 * Returns the folded index just past a match of [key] at [start] in [folded], or -1. A space in
 * [key] also matches `_`, and the match must not be followed by a tag character.
 */
private fun matchEnd(folded: String, start: Int, key: String): Int {
    val end = start + key.length
    if (end > folded.length) return -1
    for (j in key.indices) {
        val actual = folded[start + j]
        val expected = key[j]
        val same = if (expected == ' ') actual == ' ' || actual == '_' else actual == expected
        if (!same) return -1
    }
    if (end < folded.length && isTagChar(folded[end])) return -1
    return end
}

/**
 * Folds [text] char by char and records where each original index starts in the folded output. The
 * matcher walks the folded text, then maps a position in the original `#` back through it.
 */
private fun foldWithIndex(text: String): Pair<String, IntArray> {
    val folded = StringBuilder(text.length)
    val firstIndexOf = IntArray(text.length + 1)
    for (i in text.indices) {
        firstIndexOf[i] = folded.length
        foldDiacriticChar(text[i])?.let { folded.append(it) }
    }
    firstIndexOf[text.length] = folded.length
    return folded.toString() to firstIndexOf
}
