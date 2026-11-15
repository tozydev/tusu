package vn.id.tozydev.tusu.data.db.entry

import kotlinx.datetime.LocalDate

/** One live entry of a day that has an emoji, in the order the timeline shows them. */
data class DayEmojiRow(val date: LocalDate, val emoji: String)
