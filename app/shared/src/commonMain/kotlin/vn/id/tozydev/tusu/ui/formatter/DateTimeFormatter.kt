package vn.id.tozydev.tusu.ui.formatter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.format_date_days_ago
import vn.id.tozydev.tusu.generated.resources.format_date_today
import vn.id.tozydev.tusu.generated.resources.format_date_weeks_ago
import vn.id.tozydev.tusu.generated.resources.format_date_yesterday
import vn.id.tozydev.tusu.ui.model.UiText

interface DateTimeFormatter {
    fun formatTime(instant: Instant): String

    fun formatRelativeDate(date: LocalDate): UiText

    fun formatDate(date: LocalDate): UiText

    fun formatShortMonth(date: LocalDate): UiText

    @Inject
    @ContributesBinding(AppScope::class, binding = binding<DateTimeFormatter>())
    class Impl(private val timeZone: TimeZone, private val clock: Clock) : DateTimeFormatter {
        private val localTimeFormat = LocalDateTime.Format {
            hour()
            chars(":")
            minute()
        }

        override fun formatTime(instant: Instant): String =
            instant.toLocalDateTime(timeZone).format(localTimeFormat)

        private val relativeDateFormat = LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(", ")
            year()
        }

        override fun formatRelativeDate(date: LocalDate): UiText {
            // todo magic constants
            return when (val daysBetween = date.daysUntil(clock.todayIn(timeZone))) {
                0 -> UiText(Res.string.format_date_today)
                1 -> UiText(Res.string.format_date_yesterday)
                in 2..6 -> UiText(Res.string.format_date_days_ago, daysBetween)
                in 7..29 -> {
                    val weeks = daysBetween / 7
                    UiText(Res.string.format_date_weeks_ago, weeks)
                }
                else -> UiText(relativeDateFormat.format(date))
            }
        }

        private val dateFormat = LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(", ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(", ")
            year()
        }

        override fun formatDate(date: LocalDate): UiText = UiText(dateFormat.format(date))

        private val shortMonthFormat = LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }

        override fun formatShortMonth(date: LocalDate): UiText =
            UiText(shortMonthFormat.format(date))
    }
}
