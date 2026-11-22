package vn.id.tozydev.tusu.ui.feature.composer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection

private val at = Instant.parse("2026-11-11T03:00:00Z")

private fun group(selection: LabelSelection) =
    LabelGroup(
        id = Uuid.random(),
        name = "g",
        color = 0,
        selection = selection,
        sortOrder = 0,
        updatedAt = at,
    )

private fun label(group: LabelGroup?) =
    Label(id = Uuid.random(), groupId = group?.id, name = "l", sortOrder = 0, updatedAt = at)

class LabelSelectionTest :
    FunSpec(
        {
            test("a multi group keeps every chosen label, and a second tap removes one") {
                val multi = group(LabelSelection.MULTI)
                val a = label(multi)
                val b = label(multi)
                val labels = listOf(a, b)

                val both = toggledLabels(setOf(a.id), b, listOf(multi), labels)
                both shouldBe setOf(a.id, b.id)
                toggledLabels(both, a, listOf(multi), labels) shouldBe setOf(b.id)
            }

            test("a single group replaces its old label and leaves other groups alone") {
                val single = group(LabelSelection.SINGLE)
                val multi = group(LabelSelection.MULTI)
                val old = label(single)
                val new = label(single)
                val other = label(multi)
                val loose = label(null)

                toggledLabels(
                    setOf(old.id, other.id, loose.id),
                    new,
                    listOf(single, multi),
                    listOf(old, new, other, loose),
                ) shouldBe setOf(new.id, other.id, loose.id)
            }
        },
    )
