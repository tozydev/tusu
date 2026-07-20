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
            // todo support i18n for month names
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(", ")
            year()
        }

        override fun formatRelativeDate(date: LocalDate): UiText {
            return when (val daysBetween = date.daysUntil(clock.todayIn(timeZone))) {
                TODAY -> UiText(Res.string.format_date_today)
                YESTERDAY -> UiText(Res.string.format_date_yesterday)
                in MIN_DAYS_AGO..MAX_DAYS_AGO ->
                    UiText(Res.string.format_date_days_ago, daysBetween)
                in MIN_WEEKS_AGO..MAX_WEEKS_AGO -> {
                    val weeks = daysBetween / DAYS_IN_WEEK
                    UiText(Res.string.format_date_weeks_ago, weeks)
                }
                else -> UiText(relativeDateFormat.format(date))
            }
        }

        private val shortMonthFormat = LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }

        override fun formatShortMonth(date: LocalDate): UiText =
            UiText(shortMonthFormat.format(date))

        companion object {
            private const val TODAY = 0
            private const val YESTERDAY = 1
            private const val MIN_DAYS_AGO = 2
            private const val MAX_DAYS_AGO = 6
            private const val MIN_WEEKS_AGO = 7
            private const val MAX_WEEKS_AGO = 29
            private const val DAYS_IN_WEEK = 7
        }
    }
}
