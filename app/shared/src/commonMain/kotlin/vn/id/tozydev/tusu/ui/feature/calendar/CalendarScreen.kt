package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.calendar_day_empty
import vn.id.tozydev.tusu.generated.resources.calendar_new_entry_for_day
import vn.id.tozydev.tusu.generated.resources.ic_add_24px
import vn.id.tozydev.tusu.ui.feature.calendar.component.CalendarEntryRow
import vn.id.tozydev.tusu.ui.feature.calendar.component.CalendarMonthGrid
import vn.id.tozydev.tusu.ui.feature.calendar.component.CalendarMonthHeader
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The month grid with the entries of the selected day under it. */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    contentPadding: PaddingValues,
    onMonthChange: (Int) -> Unit,
    onDaySelect: (LocalDate) -> Unit,
    onEntryClick: (Uuid) -> Unit,
    onNewEntryForDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalAppDateTimeFormatter.current
    val clock = remember { Clock.System }
    val timeZone = remember { TimeZone.currentSystemDefault() }
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = contentPadding,
        ) {
            item(key = "header") {
                CalendarMonthHeader(
                    title = formatter.monthTitle(state.month.firstDay),
                    onPrevious = { onMonthChange(-1) },
                    onNext = { onMonthChange(1) },
                )
            }
            item(key = "grid") {
                CalendarMonthGrid(
                    month = state.month,
                    today = state.today,
                    selectedDate = state.selectedDate,
                    days = state.days,
                    onDaySelect = onDaySelect,
                    onMonthChange = onMonthChange,
                )
            }
            item(key = "day-title") {
                Text(
                    text = context(clock, timeZone) { formatter.dayHeader(state.selectedDate) },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                )
            }
            if (state.dayEntries.isEmpty()) {
                item(key = "day-empty") {
                    Text(
                        text = stringResource(Res.string.calendar_day_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
            items(state.dayEntries, key = { it.id.toString() }) { entry ->
                CalendarEntryRow(
                    time = context(entry.timeZone) { formatter.time(entry.occurredAt) },
                    emoji = entry.emoji,
                    text = entry.textPlain,
                    onClick = { onEntryClick(entry.id) },
                )
            }
            item(key = "new-entry") {
                NewEntryForDayButton(
                    onClick = { onNewEntryForDay(state.selectedDate) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun NewEntryForDayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(Res.drawable.ic_add_24px),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(Res.string.calendar_new_entry_for_day),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview
@Composable
private fun CalendarScreenPreview() {
    val day = LocalDate(2026, 11, 5)
    val zone = TimeZone.of("Asia/Ho_Chi_Minh")
    val at = Instant.parse("2026-11-05T01:15:00Z")
    AppTheme {
        CalendarScreen(
            state =
                CalendarUiState(
                    month = YearMonth(2026, 11),
                    today = LocalDate(2026, 11, 23),
                    selectedDate = day,
                    days = mapOf(day to CalendarDay(day, emoji = "☀️", entryCount = 1)),
                    dayEntries =
                        listOf(
                            Entry(
                                id = Uuid.random(),
                                body = "",
                                textPlain = "Buổi sáng đi bộ quanh hồ.",
                                emoji = "☀️",
                                occurredAt = at,
                                timeZone = zone,
                                createdAt = at,
                                updatedAt = at,
                            ),
                        ),
                ),
            contentPadding = PaddingValues(bottom = 104.dp),
            onMonthChange = {},
            onDaySelect = {},
            onEntryClick = {},
            onNewEntryForDay = {},
        )
    }
}
