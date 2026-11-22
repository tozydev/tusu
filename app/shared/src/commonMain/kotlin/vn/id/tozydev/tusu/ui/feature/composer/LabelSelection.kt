package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection

/**
 * [selected] after tapping [label]: a chosen label is removed; otherwise it is added, and in a
 * group of kind [LabelSelection.SINGLE] it replaces the group's other labels.
 */
internal fun toggledLabels(
    selected: Set<Uuid>,
    label: Label,
    groups: List<LabelGroup>,
    labels: List<Label>,
): Set<Uuid> {
    if (label.id in selected) return selected - label.id
    val group = label.groupId?.let { id -> groups.firstOrNull { it.id == id } }
    if (group?.selection != LabelSelection.SINGLE) return selected + label.id
    val sameGroup = labels.filter { it.groupId == group.id }.map { it.id }.toSet()
    return selected - sameGroup + label.id
}
