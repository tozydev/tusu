package vn.id.tozydev.tusu.text

/**
 * Lower-cases this string and removes Latin diacritics, so `Bài học` and `bai hoc` compare equal.
 * Digits, punctuation, emoji and non-Latin letters are kept.
 */
fun String.foldDiacritics(): String =
    buildString(length) {
        for (ch in this@foldDiacritics) foldDiacriticChar(ch)?.let { append(it) }
    }

/**
 * Folds one char, or returns null when the char is a combining mark that folds to nothing. A
 * char-level fold keeps the mapping of each source index, which the hashtag matcher relies on.
 */
internal fun foldDiacriticChar(ch: Char): Char? {
    // Combining diacritical marks (NFD input such as a + U+0306 + U+0300) carry no letter.
    if (ch.code in 0x0300..0x036F) return null
    val lower = ch.lowercaseChar()
    return BASE_LETTERS[lower] ?: lower
}

// Vietnamese is written as precomposed letters, so every tone and shape mark needs an entry.
// The Latin-1 and Latin Extended-A letters cover the other common European diacritics.
private val BASE_LETTERS: Map<Char, Char> = buildMap {
    fun map(variants: String, base: Char) {
        for (ch in variants) put(ch, base)
    }
    map("àáảãạăằắẳẵặâầấẩẫậäåāą", 'a')
    map("çćč", 'c')
    map("đ", 'd')
    map("èéẻẽẹêềếểễệëēėę", 'e')
    map("ìíỉĩịïīį", 'i')
    map("ł", 'l')
    map("ñń", 'n')
    map("òóỏõọôồốổỗộơờớởỡợöøō", 'o')
    map("śš", 's')
    map("ùúủũụưừứửữựüū", 'u')
    map("ỳýỷỹỵÿ", 'y')
    map("žźż", 'z')
    map("ğ", 'g')
}
