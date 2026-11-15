package vn.id.tozydev.tusu.ui.format

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

private val vietnamese =
    DateTimeNames(
        weekdays =
            listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"),
        monthsShort = (1..12).map { "Th$it" },
        monthsFull = (1..12).map { "Tháng $it" },
        today = "Hôm nay",
        yesterday = "Hôm qua",
        dayHeaderPattern = "{weekday}, {day}/{month}",
        dayHeaderWithYearPattern = "{weekday}, {day}/{month}/{year}",
        monthTitlePattern = "Tháng {month}, {year}",
        fullDateTimePattern = "{weekday}, {day}/{month}/{year} · {time}",
    )

private val english =
    DateTimeNames(
        weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        monthsShort =
            listOf(
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec",
            ),
        monthsFull =
            listOf(
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December",
            ),
        today = "Today",
        yesterday = "Yesterday",
        dayHeaderPattern = "{weekday}, {monthShort} {day}",
        dayHeaderWithYearPattern = "{weekday}, {monthShort} {day}, {year}",
        monthTitlePattern = "{monthName} {year}",
        fullDateTimePattern = "{weekday}, {monthShort} {day}, {year} · {time}",
    )

private val zone = TimeZone.of("Asia/Ho_Chi_Minh")

private fun fixedClock(instant: String) =
    object : Clock {
        override fun now(): Instant = Instant.parse(instant)
    }

class AppDateTimeFormatterTest :
    FunSpec(
        {
            val vi = AppDateTimeFormatter(vietnamese)
            val en = AppDateTimeFormatter(english)
            // 2026-11-15 in Ho Chi Minh (UTC+7).
            val clock = fixedClock("2026-11-15T03:00:00Z")

            test("time uses 24 hour clock in the given zone") {
                context(zone) {
                    vi.time(Instant.parse("2026-11-11T10:40:00Z")) shouldBe "17:40"
                    en.time(Instant.parse("2026-11-11T10:40:00Z")) shouldBe "17:40"
                }
            }

            test("time pads midnight and early minutes") {
                context(zone) { en.time(Instant.parse("2026-11-10T17:05:00Z")) shouldBe "00:05" }
            }

            test("day header shows weekday and date without the year in the current year") {
                context(clock, zone) {
                    vi.dayHeader(LocalDate(2026, 11, 11)) shouldBe "Thứ Tư, 11/11"
                    en.dayHeader(LocalDate(2026, 11, 11)) shouldBe "Wed, Nov 11"
                }
            }

            test("day header adds the year for other years") {
                context(clock, zone) {
                    vi.dayHeader(LocalDate(2025, 12, 31)) shouldBe "Thứ Tư, 31/12/2025"
                    en.dayHeader(LocalDate(2025, 12, 31)) shouldBe "Wed, Dec 31, 2025"
                    en.dayHeader(LocalDate(2027, 1, 1)) shouldBe "Fri, Jan 1, 2027"
                }
            }

            test("day header says today and yesterday") {
                context(clock, zone) {
                    vi.dayHeader(LocalDate(2026, 11, 15)) shouldBe "Hôm nay"
                    vi.dayHeader(LocalDate(2026, 11, 14)) shouldBe "Hôm qua"
                    en.dayHeader(LocalDate(2026, 11, 15)) shouldBe "Today"
                    en.dayHeader(LocalDate(2026, 11, 14)) shouldBe "Yesterday"
                }
            }

            test("today follows the time zone, not UTC") {
                // 18:00 UTC on the 14th is already the 15th in Ho Chi Minh.
                val lateClock = fixedClock("2026-11-14T18:00:00Z")
                context(lateClock, zone) {
                    en.dayHeader(LocalDate(2026, 11, 15)) shouldBe "Today"
                    en.dayHeader(LocalDate(2026, 11, 14)) shouldBe "Yesterday"
                }
            }

            test("yesterday works across a year boundary") {
                val newYear = fixedClock("2026-01-01T03:00:00Z")
                context(newYear, zone) {
                    en.dayHeader(LocalDate(2025, 12, 31)) shouldBe "Yesterday"
                }
            }

            test("month title") {
                vi.monthTitle(LocalDate(2026, 11, 3)) shouldBe "Tháng 11, 2026"
                en.monthTitle(LocalDate(2026, 11, 3)) shouldBe "November 2026"
                en.monthTitle(LocalDate(2027, 1, 31)) shouldBe "January 2027"
            }

            test("full date time always includes the year") {
                context(zone) {
                    val instant = Instant.parse("2026-11-11T10:40:00Z")
                    vi.fullDateTime(instant) shouldBe "Thứ Tư, 11/11/2026 · 17:40"
                    en.fullDateTime(instant) shouldBe "Wed, Nov 11, 2026 · 17:40"
                }
            }

            test("full date time uses the local date near midnight") {
                context(zone) {
                    // 17:30 UTC on Saturday the 14th is 00:30 on Sunday the 15th locally.
                    en.fullDateTime(Instant.parse("2026-11-14T17:30:00Z")) shouldBe
                        "Sun, Nov 15, 2026 · 00:30"
                }
            }

            test("duration under an hour is m:ss") {
                vi.duration(0.seconds) shouldBe "0:00"
                vi.duration(48.seconds) shouldBe "0:48"
                vi.duration(2.minutes + 14.seconds) shouldBe "2:14"
                vi.duration(59.minutes + 59.seconds) shouldBe "59:59"
            }

            test("duration from an hour is h:mm:ss") {
                en.duration(1.hours) shouldBe "1:00:00"
                en.duration(1.hours + 2.minutes + 14.seconds) shouldBe "1:02:14"
                en.duration(12.hours + 5.seconds) shouldBe "12:00:05"
            }

            test("duration drops fractions and clamps negatives") {
                en.duration(48.9.seconds) shouldBe "0:48"
                en.duration((-5).seconds) shouldBe "0:00"
            }
        },
    )
