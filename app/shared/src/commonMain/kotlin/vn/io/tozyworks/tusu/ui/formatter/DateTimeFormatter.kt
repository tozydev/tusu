package vn.io.tozyworks.tusu.ui.formatter

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
import vn.io.tozyworks.tusu.generated.resources.Res
import vn.io.tozyworks.tusu.generated.resources.format_date_days_ago
import vn.io.tozyworks.tusu.generated.resources.format_date_today
import vn.io.tozyworks.tusu.generated.resources.format_date_weeks_ago
import vn.io.tozyworks.tusu.generated.resources.format_date_yesterday
import vn.io.tozyworks.tusu.ui.model.UiText

interface DateTimeFormatter {
    fun formatTime(instant: Instant): String

    fun formatDate(date: LocalDate): UiText

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

        private val localDateFormat = LocalDate.Format {
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(", ")
            year()
        }

        override fun formatDate(date: LocalDate): UiText {
            // todo magic constants
            return when (val daysBetween = date.daysUntil(clock.todayIn(timeZone))) {
                0 -> UiText(Res.string.format_date_today)
                1 -> UiText(Res.string.format_date_yesterday)
                in 2..6 -> UiText(Res.string.format_date_days_ago, daysBetween)
                in 7..29 -> {
                    val weeks = daysBetween / 7
                    UiText(Res.string.format_date_weeks_ago, weeks)
                }
                else -> UiText(localDateFormat.format(date))
            }
        }
    }
}
