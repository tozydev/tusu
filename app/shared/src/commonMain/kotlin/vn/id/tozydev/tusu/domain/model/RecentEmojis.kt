package vn.id.tozydev.tusu.domain.model

/** How many emojis the "Recent" group of the emoji sheet remembers. */
const val MAX_RECENT_EMOJIS = 24

/** Moves [emoji] to the front of the recent emojis, dropping the oldest past the limit. */
fun AppSettings.withRecentEmoji(emoji: String): AppSettings =
    copy(recentEmojis = (listOf(emoji) + (recentEmojis - emoji)).take(MAX_RECENT_EMOJIS))
