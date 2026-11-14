package vn.id.tozydev.tusu.domain.repository

import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection

interface LabelRepository {
    /** Groups in display order. */
    fun observeGroups(): Flow<List<LabelGroup>>

    /** Labels of every group, hidden ones included, in display order. */
    fun observeLabels(): Flow<List<Label>>

    suspend fun createGroup(
        name: String,
        color: Int,
        selection: LabelSelection,
    ): Outcome<LabelGroup, LabelError>

    /** Updates the editable fields; `sortOrder` is ignored, see [reorderGroups]. */
    suspend fun updateGroup(group: LabelGroup): Outcome<Unit, LabelError>

    /** Deletes the group and moves its labels to "Khác"; no entry loses a label. */
    suspend fun deleteGroup(id: Uuid): Outcome<Unit, LabelError>

    suspend fun reorderGroups(orderedIds: List<Uuid>): Outcome<Unit, LabelError>

    /** Null [groupId] creates the label in "Khác". */
    suspend fun createLabel(name: String, groupId: Uuid?): Outcome<Label, LabelError>

    /**
     * Also moves the label to the end of the new group when its `groupId` changed. `sortOrder` is
     * ignored, see [reorderLabels].
     */
    suspend fun updateLabel(label: Label): Outcome<Unit, LabelError>

    /** Deletes the label and removes it from every entry. */
    suspend fun deleteLabel(id: Uuid): Outcome<Unit, LabelError>

    suspend fun reorderLabels(groupId: Uuid?, orderedIds: List<Uuid>): Outcome<Unit, LabelError>
}
