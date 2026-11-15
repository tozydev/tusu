package vn.id.tozydev.tusu.ui.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class DatePickerMillisTest :
    FunSpec(
        {
            test("epoch day two is 86_400_000 millis at UTC midnight") {
                LocalDate(1970, 1, 2).toDatePickerMillis() shouldBe 86_400_000L
                86_400_000L.toDatePickerLocalDate() shouldBe LocalDate(1970, 1, 2)
            }

            test("the day before the epoch is negative millis") {
                LocalDate(1969, 12, 31).toDatePickerMillis() shouldBe -86_400_000L
            }

            test("dates survive a round trip through picker millis") {
                listOf(
                        LocalDate(2024, 2, 29),
                        LocalDate(2026, 11, 15),
                        LocalDate(1999, 12, 31),
                        LocalDate(1970, 1, 1),
                    )
                    .forEach { date ->
                        date.toDatePickerMillis().toDatePickerLocalDate() shouldBe date
                    }
            }

            test("picker millis are always a whole UTC day") {
                LocalDate(2026, 11, 15).toDatePickerMillis() % 86_400_000L shouldBe 0L
            }
        },
    )
