package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry

@Immutable
data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val selectedDate: LocalDate,
    /** Only days with entries have a value. */
    val days: Map<LocalDate, CalendarDay> = emptyMap(),
    /** Entries of [selectedDate], oldest first. */
    val dayEntries: List<Entry> = emptyList(),
)
