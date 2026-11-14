package vn.id.tozydev.tusu.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

class EntryFilterTest :
    FunSpec(
        {
            test("the default filter is empty") { EntryFilter().isEmpty shouldBe true }

            test("a blank query does not count as a criterion") {
                EntryFilter(query = "  ").isEmpty shouldBe true
            }

            test("label groups with no selected label do not count as a criterion") {
                EntryFilter(labelIdsByGroup = mapOf(Uuid.random() to emptySet())).isEmpty shouldBe
                    true
            }

            test("any single criterion makes the filter non-empty") {
                val day = LocalDate(2026, 11, 11)

                EntryFilter(query = "cà phê").isEmpty shouldBe false
                EntryFilter(mediaTypes = setOf(MediaType.PHOTO)).isEmpty shouldBe false
                EntryFilter(hasLocation = true).isEmpty shouldBe false
                EntryFilter(emojis = setOf("🙂")).isEmpty shouldBe false
                EntryFilter(labelIdsByGroup = mapOf(null to setOf(Uuid.random()))).isEmpty shouldBe
                    false
                EntryFilter(dateRange = day..day).isEmpty shouldBe false
            }
        },
    )
