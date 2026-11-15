package vn.id.tozydev.tusu.ui.format

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class AppDateTimeFormatter(private val names: DateTimeNames) {

    /** 24-hour time of day, for example `17:40`. */
    context(timeZone: TimeZone)
    fun time(instant: Instant): String =
        instant.toLocalDateTime(timeZone).let { time(it.hour, it.minute) }

    /** Timeline header: "Today", "Yesterday", or the date, with the year only outside this year. */
    context(clock: Clock, timeZone: TimeZone)
    fun dayHeader(date: LocalDate): String {
        val today = clock.now().toLocalDateTime(timeZone).date
        return when {
            date == today -> names.today
            date == today.minus(DatePeriod(days = 1)) -> names.yesterday
            date.year == today.year -> render(names.dayHeaderPattern, date)
            else -> render(names.dayHeaderWithYearPattern, date)
        }
    }

    fun monthTitle(date: LocalDate): String = render(names.monthTitlePattern, date)

    context(timeZone: TimeZone)
    fun fullDateTime(instant: Instant): String {
        val local = instant.toLocalDateTime(timeZone)
        return render(names.fullDateTimePattern, local.date, time(local.hour, local.minute))
    }

    /** Media length as `m:ss`, or `h:mm:ss` from one hour; fractions are dropped. */
    fun duration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "$hours:${pad(minutes)}:${pad(seconds)}"
        else "$minutes:${pad(seconds)}"
    }

    private fun time(hour: Int, minute: Int) = "${pad(hour.toLong())}:${pad(minute.toLong())}"

    private fun pad(value: Long) = value.toString().padStart(2, '0')

    private fun render(pattern: String, date: LocalDate, time: String = ""): String {
        val monthIndex = date.month.ordinal
        return pattern
            .replace("{weekday}", names.weekdays[date.dayOfWeek.ordinal])
            .replace("{day}", date.day.toString())
            .replace("{monthShort}", names.monthsShort[monthIndex])
            .replace("{monthName}", names.monthsFull[monthIndex])
            .replace("{month}", (monthIndex + 1).toString())
            .replace("{year}", date.year.toString())
            .replace("{time}", time)
    }
}
