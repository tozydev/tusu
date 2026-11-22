package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import vn.id.tozydev.tusu.domain.repository.EntryRepository

@ContributesIntoMap(AppScope::class)
@ViewModelKey(CalendarViewModel::class)
@Inject
class CalendarViewModel(
    private val entryRepository: EntryRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {
    private val today = clock.todayIn(timeZone)
    private val month = MutableStateFlow(today.yearMonth)
    private val selectedDate = MutableStateFlow(today)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> =
        combine(
                month,
                selectedDate,
                month.flatMapLatest { entryRepository.observeCalendar(it) },
                selectedDate.flatMapLatest { entryRepository.observeDay(it) },
            ) { month, selected, days, entries ->
                CalendarUiState(
                    month = month,
                    today = today,
                    selectedDate = selected,
                    // The month flow can run ahead of its days for a moment; keep them matched.
                    days = days.filter { it.date.yearMonth == month }.associateBy { it.date },
                    dayEntries = entries.filter { !it.isDeleted },
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                CalendarUiState(month = today.yearMonth, today = today, selectedDate = today),
            )

    /** Moves the grid [delta] months forward, or back when negative. */
    fun onMonthChange(delta: Int) {
        month.value = month.value.plus(delta, DateTimeUnit.MONTH)
    }

    /** Selects [date]; a date outside the shown month also moves the grid to it. */
    fun onDaySelect(date: LocalDate) {
        selectedDate.value = date
        month.value = date.yearMonth
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
