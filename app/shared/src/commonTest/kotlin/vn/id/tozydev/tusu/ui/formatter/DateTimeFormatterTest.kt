package vn.id.tozydev.tusu.ui.formatter

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.format_date_today
import vn.id.tozydev.tusu.generated.resources.format_date_yesterday
import vn.id.tozydev.tusu.ui.model.UiText

class DateTimeFormatterTest : ShouldSpec() {
    private val timeZone = TimeZone.UTC
    // Fixed clock: 2026-07-27T12:00:00Z
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2026-07-27T12:00:00Z")
        }
    private val formatter = DateTimeFormatter.Impl(timeZone, fixedClock)

    init {
        should("format today correctly") {
            val date = LocalDate(2026, 7, 27)
            formatter.formatRelativeDate(date) shouldBe UiText(Res.string.format_date_today)
        }

        should("format yesterday correctly") {
            val date = LocalDate(2026, 7, 26)
            formatter.formatRelativeDate(date) shouldBe UiText(Res.string.format_date_yesterday)
        }

        should("format 2 days ago or older as explicit date string") {
            val date2Days = LocalDate(2026, 7, 25)
            formatter.formatRelativeDate(date2Days) shouldBe UiText("Jul 25, 2026")

            val date7Days = LocalDate(2026, 7, 20)
            formatter.formatRelativeDate(date7Days) shouldBe UiText("Jul 20, 2026")

            val date30Days = LocalDate(2026, 6, 27)
            formatter.formatRelativeDate(date30Days) shouldBe UiText("Jun 27, 2026")
        }
    }
}
