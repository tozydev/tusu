package vn.id.tozydev.tusu.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class EntryTest :
    FunSpec(
        {
            fun entry(occurredAt: String, zone: String, deletedAt: Instant? = null): Entry {
                val instant = Instant.parse(occurredAt)
                return Entry(
                    id = Uuid.random(),
                    body = "",
                    textPlain = "",
                    occurredAt = instant,
                    timeZone = TimeZone.of(zone),
                    createdAt = instant,
                    updatedAt = instant,
                    deletedAt = deletedAt,
                )
            }

            test("localDate is the calendar day in the entry's own time zone") {
                // 18:30 UTC is already the next day in Ho Chi Minh City and still the same day in
                // New York, wherever the device is now.
                entry("2026-11-11T18:30:00Z", "Asia/Ho_Chi_Minh").localDate shouldBe
                    LocalDate(2026, 11, 12)
                entry("2026-11-11T18:30:00Z", "America/New_York").localDate shouldBe
                    LocalDate(2026, 11, 11)
            }

            test("isDeleted follows deletedAt") {
                entry("2026-11-11T18:30:00Z", "UTC").isDeleted shouldBe false
                entry(
                        "2026-11-11T18:30:00Z",
                        "UTC",
                        deletedAt = Instant.parse("2026-11-12T00:00:00Z"),
                    )
                    .isDeleted shouldBe true
            }
        },
    )
