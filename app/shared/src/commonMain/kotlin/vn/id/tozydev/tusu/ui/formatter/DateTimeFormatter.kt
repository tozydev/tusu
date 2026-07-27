package vn.id.tozydev.tusu.ui.formatter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.datetime_full
import vn.id.tozydev.tusu.generated.resources.datetime_today
import vn.id.tozydev.tusu.generated.resources.datetime_yesterday
import vn.id.tozydev.tusu.generated.resources.month_apr
import vn.id.tozydev.tusu.generated.resources.month_aug
import vn.id.tozydev.tusu.generated.resources.month_dec
import vn.id.tozydev.tusu.generated.resources.month_feb
import vn.id.tozydev.tusu.generated.resources.month_jan
import vn.id.tozydev.tusu.generated.resources.month_jul
import vn.id.tozydev.tusu.generated.resources.month_jun
import vn.id.tozydev.tusu.generated.resources.month_mar
import vn.id.tozydev.tusu.generated.resources.month_may
import vn.id.tozydev.tusu.generated.resources.month_nov
import vn.id.tozydev.tusu.generated.resources.month_oct
import vn.id.tozydev.tusu.generated.resources.month_sep
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

        override fun formatRelativeDate(date: LocalDate): UiText {
            return when (date.daysUntil(clock.todayIn(timeZone))) {
                TODAY -> UiText(Res.string.datetime_today)
                YESTERDAY -> UiText(Res.string.datetime_yesterday)
                else ->
                    UiText(
                        Res.string.datetime_full,
                        UiText(getShortMonthResource(date.month)),
                        date.day,
                        date.year,
                    )
            }
        }

        override fun formatShortMonth(date: LocalDate): UiText =
            UiText(getShortMonthResource(date.month))

        private fun getShortMonthResource(month: Month): StringResource =
            when (month) {
                Month.JANUARY -> Res.string.month_jan
                Month.FEBRUARY -> Res.string.month_feb
                Month.MARCH -> Res.string.month_mar
                Month.APRIL -> Res.string.month_apr
                Month.MAY -> Res.string.month_may
                Month.JUNE -> Res.string.month_jun
                Month.JULY -> Res.string.month_jul
                Month.AUGUST -> Res.string.month_aug
                Month.SEPTEMBER -> Res.string.month_sep
                Month.OCTOBER -> Res.string.month_oct
                Month.NOVEMBER -> Res.string.month_nov
                Month.DECEMBER -> Res.string.month_dec
            }

        companion object {
            private const val TODAY = 0
            private const val YESTERDAY = 1
        }
    }
}
