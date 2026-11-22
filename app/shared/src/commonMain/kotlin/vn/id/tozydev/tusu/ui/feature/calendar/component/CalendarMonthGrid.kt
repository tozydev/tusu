package vn.id.tozydev.tusu.ui.feature.calendar.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.stringArrayResource
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.calendar_weekdays_narrow
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val SwipeThreshold = 48.dp

/**
 * The days of [month] in weeks starting on Monday. A horizontal swipe calls [onMonthChange] with +1
 * (to the left, next month) or -1 (to the right, previous month).
 */
@Composable
fun CalendarMonthGrid(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    days: Map<LocalDate, CalendarDay>,
    onDaySelect: (LocalDate) -> Unit,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val threshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    Column(
        modifier =
            modifier.fillMaxWidth().padding(horizontal = 12.dp).pointerInput(Unit) {
                var dragged = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragEnd = {
                        when {
                            dragged < -threshold -> onMonthChange(1)
                            dragged > threshold -> onMonthChange(-1)
                        }
                    },
                ) { _, amount ->
                    dragged += amount
                }
            },
    ) {
        WeekdayLabels()
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val forward = targetState > initialState
                slideInHorizontally { if (forward) it else -it } togetherWith
                    slideOutHorizontally { if (forward) -it else it }
            },
            label = "month",
        ) { shown ->
            MonthWeeks(
                month = shown,
                today = today,
                selectedDate = selectedDate,
                days = days,
                onDaySelect = onDaySelect,
            )
        }
    }
}

@Composable
private fun WeekdayLabels(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        stringArrayResource(Res.array.calendar_weekdays_narrow).forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthWeeks(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    days: Map<LocalDate, CalendarDay>,
    onDaySelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val leadingBlanks = month.firstDay.dayOfWeek.isoDayNumber - 1
    val dayCount = month.lastDay.day
    val weekCount = (leadingBlanks + dayCount + 6) / 7
    Column(modifier = modifier.fillMaxWidth()) {
        for (week in 0 until weekCount) {
            Row(Modifier.fillMaxWidth()) {
                for (column in 0 until 7) {
                    val day = week * 7 + column - leadingBlanks + 1
                    if (day !in 1..dayCount) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {}
                        continue
                    }
                    val date = LocalDate(month.year, month.month, day)
                    val summary = days[date]
                    CalendarDayCell(
                        day = day,
                        emoji = summary?.emoji,
                        entryCount = summary?.entryCount ?: 0,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        onClick = { onDaySelect(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CalendarMonthGridPreview() {
    val selected = LocalDate(2026, 11, 5)
    AppTheme {
        CalendarMonthGrid(
            month = YearMonth(2026, 11),
            today = LocalDate(2026, 11, 23),
            selectedDate = selected,
            days =
                mapOf(
                    selected to CalendarDay(selected, emoji = "🌙", entryCount = 2),
                    LocalDate(2026, 11, 11) to
                        CalendarDay(LocalDate(2026, 11, 11), emoji = null, entryCount = 1),
                ),
            onDaySelect = {},
            onMonthChange = {},
        )
    }
}
