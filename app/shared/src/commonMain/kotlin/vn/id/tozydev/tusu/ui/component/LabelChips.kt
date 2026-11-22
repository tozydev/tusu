package vn.id.tozydev.tusu.ui.component

import androidx.compose.ui.graphics.Color
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup

/**
 * Chips for an entry's [labels], in group order and then label order. Labels in "Khác" or in a
 * group that is gone come last, in [otherColor].
 */
fun labelChipsOf(
    labels: List<Label>,
    groups: List<LabelGroup>,
    otherColor: Color,
): List<LabelChipData> {
    val liveGroups = groups.filter { it.deletedAt == null }.associateBy { it.id }
    return labels
        .sortedWith(
            compareBy<Label> { label ->
                    label.groupId?.let { liveGroups[it]?.sortOrder } ?: Int.MAX_VALUE
                }
                .thenBy { it.sortOrder },
        )
        .map { label ->
            val group = label.groupId?.let { liveGroups[it] }
            LabelChipData(name = label.name, color = group?.let { Color(it.color) } ?: otherColor)
        }
}
