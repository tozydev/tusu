package vn.id.tozydev.tusu.domain.model

import kotlinx.datetime.LocalDate

/** What a day cell of the calendar shows; days without entries have no instance. */
data class CalendarDay(
    val date: LocalDate,
    /** Emoji of the most recent entry of the day that has one. */
    val emoji: String?,
    val entryCount: Int,
)
