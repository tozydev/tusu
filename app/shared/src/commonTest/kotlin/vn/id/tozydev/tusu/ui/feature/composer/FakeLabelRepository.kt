package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.repository.LabelRepository

private val fakeLabelTime = Instant.parse("2026-11-11T03:00:00Z")

/** Groups and labels in memory; only what the composer uses is implemented. */
class FakeLabelRepository : LabelRepository {
    val groups = MutableStateFlow<List<LabelGroup>>(emptyList())
    val labels = MutableStateFlow<List<Label>>(emptyList())

    fun addGroup(selection: LabelSelection = LabelSelection.MULTI): LabelGroup =
        LabelGroup(
                id = Uuid.random(),
                name = "Group ${groups.value.size}",
                color = 0xFF68A500.toInt(),
                selection = selection,
                sortOrder = groups.value.size,
                updatedAt = fakeLabelTime,
            )
            .also { groups.value += it }

    fun addLabel(group: LabelGroup?, name: String = "Label ${labels.value.size}"): Label =
        Label(
                id = Uuid.random(),
                groupId = group?.id,
                name = name,
                sortOrder = labels.value.size,
                updatedAt = fakeLabelTime,
            )
            .also { labels.value += it }

    override fun observeGroups(): Flow<List<LabelGroup>> = groups

    override fun observeLabels(): Flow<List<Label>> = labels

    override suspend fun createLabel(name: String, groupId: Uuid?): Outcome<Label, LabelError> {
        val group = groupId?.let { id -> groups.value.first { it.id == id } }
        return Outcome.Success(addLabel(group, name))
    }

    override suspend fun createGroup(
        name: String,
        color: Int,
        selection: LabelSelection,
    ): Outcome<LabelGroup, LabelError> = error("not used")

    override suspend fun countEntriesWithSeveralLabels(groupId: Uuid): Outcome<Int, LabelError> =
        error("not used")

    override suspend fun updateGroup(group: LabelGroup): Outcome<Unit, LabelError> =
        error("not used")

    override suspend fun deleteGroup(id: Uuid): Outcome<Unit, LabelError> = error("not used")

    override suspend fun reorderGroups(orderedIds: List<Uuid>): Outcome<Unit, LabelError> =
        error("not used")

    override suspend fun updateLabel(label: Label): Outcome<Unit, LabelError> = error("not used")

    override suspend fun deleteLabel(id: Uuid): Outcome<Unit, LabelError> = error("not used")

    override suspend fun reorderLabels(
        groupId: Uuid?,
        orderedIds: List<Uuid>,
    ): Outcome<Unit, LabelError> = error("not used")
}
