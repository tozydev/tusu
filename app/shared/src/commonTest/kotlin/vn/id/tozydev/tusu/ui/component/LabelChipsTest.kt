package vn.id.tozydev.tusu.ui.component

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup

private val at = Instant.parse("2026-11-11T03:00:00Z")

private fun group(name: String, color: Long, sortOrder: Int, deleted: Boolean = false) =
    LabelGroup(
        id = Uuid.random(),
        name = name,
        color = color.toInt(),
        sortOrder = sortOrder,
        updatedAt = at,
        deletedAt = if (deleted) at else null,
    )

private fun label(name: String, group: LabelGroup?, sortOrder: Int) =
    Label(
        id = Uuid.random(),
        groupId = group?.id,
        name = name,
        sortOrder = sortOrder,
        updatedAt = at,
    )

class LabelChipsTest :
    FunSpec(
        {
            test("chips follow the group order, then the label order, with the group colour") {
                val people = group("People", 0xFFE53935, sortOrder = 1)
                val kind = group("Kind", 0xFF68A500, sortOrder = 0)
                val labels =
                    listOf(
                        label("Mom", people, sortOrder = 0),
                        label("Lesson", kind, sortOrder = 1),
                        label("Memory", kind, sortOrder = 0),
                    )

                labelChipsOf(labels, listOf(people, kind), otherColor = Color.Gray) shouldBe
                    listOf(
                        LabelChipData("Memory", Color(0xFF68A500)),
                        LabelChipData("Lesson", Color(0xFF68A500)),
                        LabelChipData("Mom", Color(0xFFE53935)),
                    )
            }

            test("labels in Khác, or in a deleted group, come last in the Other colour") {
                val kind = group("Kind", 0xFF68A500, sortOrder = 0)
                val gone = group("Gone", 0xFF1E88E5, sortOrder = 1, deleted = true)
                val labels =
                    listOf(
                        label("Loose", null, sortOrder = 0),
                        label("Orphan", gone, sortOrder = 0),
                        label("Memory", kind, sortOrder = 0),
                    )

                labelChipsOf(labels, listOf(kind, gone), otherColor = Color.Gray).map {
                    it.name to it.color
                } shouldBe
                    listOf(
                        "Memory" to Color(0xFF68A500),
                        "Loose" to Color.Gray,
                        "Orphan" to Color.Gray,
                    )
            }
        },
    )
