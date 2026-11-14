package vn.id.tozydev.tusu.data.db.entry

import kotlinx.datetime.LocalDate

/** One day of the calendar grid, aggregated in SQL. */
data class CalendarRow(val date: LocalDate, val entryCount: Int, val emoji: String?)
