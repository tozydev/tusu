package vn.id.tozydev.tusu.data.repository

import androidx.sqlite.SQLiteException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.id.tozydev.tusu.data.db.TransactionScope
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.entry.reindexEntriesWithLabel
import vn.id.tozydev.tusu.data.db.label.LabelDao
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelGroupEntity
import vn.id.tozydev.tusu.data.db.label.toDomain
import vn.id.tozydev.tusu.data.db.transaction
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.Raise
import vn.id.tozydev.tusu.domain.error.ensure
import vn.id.tozydev.tusu.domain.error.ensureNotNull
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.text.foldDiacritics

private val logger = KotlinLogging.logger {}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class LabelRepositoryImpl(private val database: TusuDatabase, private val clock: Clock) :
    LabelRepository {
    private val dao: LabelDao = database.labelDao()

    override fun observeGroups(): Flow<List<LabelGroup>> =
        dao.observeGroups().map { groups -> groups.map(LabelGroupEntity::toDomain) }

    override fun observeLabels(): Flow<List<Label>> =
        dao.observeLabels().map { labels -> labels.map(LabelEntity::toDomain) }

    override suspend fun createGroup(
        name: String,
        color: Int,
        selection: LabelSelection,
    ): Outcome<LabelGroup, LabelError> = write {
        val cleanName = cleanName(name)
        checkGroupNameFree(cleanName, exceptId = null)
        val entity =
            LabelGroupEntity(
                id = Uuid.random(),
                name = cleanName,
                color = color,
                selection = selection,
                showInComposer = true,
                isDefault = false,
                sortOrder = (dao.maxGroupSortOrder() ?: -1) + 1,
                updatedAt = clock.now(),
                deletedAt = null,
            )
        dao.upsertGroup(entity)
        entity.toDomain()
    }

    override suspend fun countEntriesWithSeveralLabels(groupId: Uuid): Outcome<Int, LabelError> =
        write {
            dao.countEntriesWithSeveralLabels(groupId)
        }

    override suspend fun updateGroup(group: LabelGroup): Outcome<Unit, LabelError> = write {
        val stored = liveGroup(group.id)
        val cleanName = cleanName(group.name)
        checkGroupNameFree(cleanName, exceptId = group.id)
        // isDefault and deletedAt are not editable, and the order changes only through
        // reorderGroups, so an edit made from an older copy cannot undo a reorder.
        dao.upsertGroup(
            LabelGroupEntity(
                id = stored.id,
                name = cleanName,
                color = group.color,
                selection = group.selection,
                showInComposer = group.showInComposer,
                isDefault = stored.isDefault,
                sortOrder = stored.sortOrder,
                updatedAt = clock.now(),
                deletedAt = stored.deletedAt,
            ),
        )
    }

    override suspend fun deleteGroup(id: Uuid): Outcome<Unit, LabelError> = write {
        val group = liveGroup(id)
        val now = clock.now()
        // Labels are appended to "Khác" in their current order; no entry_labels row is touched.
        var nextSortOrder = (dao.maxLabelSortOrderInOther() ?: -1) + 1
        for (label in dao.listLiveLabelsInGroup(id)) {
            dao.upsertLabel(
                label.copy(groupId = null, sortOrder = nextSortOrder++, updatedAt = now),
            )
        }
        dao.upsertGroup(group.copy(updatedAt = now, deletedAt = now))
    }

    override suspend fun reorderGroups(orderedIds: List<Uuid>): Outcome<Unit, LabelError> = write {
        val live = dao.listLiveGroups()
        ensureSameIds(orderedIds, live.map(LabelGroupEntity::id), LabelError.GroupNotFound)
        val byId = live.associateBy(LabelGroupEntity::id)
        val now = clock.now()
        orderedIds.forEachIndexed { index, id ->
            dao.upsertGroup(byId.getValue(id).copy(sortOrder = index, updatedAt = now))
        }
    }

    override suspend fun createLabel(name: String, groupId: Uuid?): Outcome<Label, LabelError> =
        write {
            val cleanName = cleanName(name)
            checkLabelNameFree(cleanName, exceptId = null)
            if (groupId != null) liveGroup(groupId)
            val entity =
                LabelEntity(
                    id = Uuid.random(),
                    groupId = groupId,
                    name = cleanName,
                    isHidden = false,
                    isDefault = false,
                    sortOrder = nextLabelSortOrder(groupId),
                    updatedAt = clock.now(),
                    deletedAt = null,
                )
            dao.upsertLabel(entity)
            entity.toDomain()
        }

    override suspend fun updateLabel(label: Label): Outcome<Unit, LabelError> = write {
        val stored = liveLabel(label.id)
        val cleanName = cleanName(label.name)
        checkLabelNameFree(cleanName, exceptId = label.id)
        val targetGroupId = label.groupId
        val moved = targetGroupId != stored.groupId
        if (moved && targetGroupId != null) liveGroup(targetGroupId)
        // A label that changes group is appended to the end of its new group. Otherwise the
        // order stays as stored: it changes only through reorderLabels.
        val sortOrder = if (moved) nextLabelSortOrder(targetGroupId) else stored.sortOrder
        dao.upsertLabel(
            LabelEntity(
                id = stored.id,
                groupId = targetGroupId,
                name = cleanName,
                isHidden = label.isHidden,
                isDefault = stored.isDefault,
                sortOrder = sortOrder,
                updatedAt = clock.now(),
                deletedAt = stored.deletedAt,
            ),
        )
        // The search index stores label names per entry, so a rename has to rewrite those rows.
        if (cleanName != stored.name) database.reindexEntriesWithLabel(stored.id)
    }

    override suspend fun deleteLabel(id: Uuid): Outcome<Unit, LabelError> = write {
        val label = liveLabel(id)
        val now = clock.now()
        dao.upsertLabel(label.copy(updatedAt = now, deletedAt = now))
        for (entryLabel in dao.listLiveEntryLabelsOfLabel(id)) {
            dao.upsertEntryLabel(entryLabel.copy(updatedAt = now, deletedAt = now))
        }
        database.reindexEntriesWithLabel(id)
    }

    override suspend fun reorderLabels(
        groupId: Uuid?,
        orderedIds: List<Uuid>,
    ): Outcome<Unit, LabelError> = write {
        if (groupId != null) liveGroup(groupId)
        val live = labelsOf(groupId)
        ensureSameIds(orderedIds, live.map(LabelEntity::id), LabelError.NotFound)
        val byId = live.associateBy(LabelEntity::id)
        val now = clock.now()
        orderedIds.forEachIndexed { index, id ->
            dao.upsertLabel(byId.getValue(id).copy(sortOrder = index, updatedAt = now))
        }
    }

    /**
     * Runs [block] in one write transaction. A raised [LabelError] rolls the transaction back. A
     * SQLite failure is logged and returned as [LabelError.StorageFailed]; other exceptions are
     * bugs and propagate.
     */
    private suspend fun <T> write(
        block: suspend context(TransactionScope, Raise<LabelError>) () -> T,
    ): Outcome<T, LabelError> =
        try {
            outcome<T, LabelError> { database.transaction { block() } }
        } catch (e: SQLiteException) {
            logger.error(e) { "Label write failed" }
            Outcome.Failure(LabelError.StorageFailed)
        }

    context(raise: Raise<LabelError>)
    private fun cleanName(name: String): String {
        val trimmed = name.trim()
        ensure(trimmed.isNotEmpty()) { LabelError.BlankName }
        return trimmed
    }

    context(raise: Raise<LabelError>)
    private suspend fun checkGroupNameFree(name: String, exceptId: Uuid?) {
        val key = name.foldDiacritics()
        val taken =
            dao.listLiveGroups().any { it.id != exceptId && it.name.foldDiacritics() == key }
        ensure(!taken) { LabelError.DuplicateName }
    }

    /** Label names are unique across every group, because hashtags look labels up by name. */
    context(raise: Raise<LabelError>)
    private suspend fun checkLabelNameFree(name: String, exceptId: Uuid?) {
        val key = name.foldDiacritics()
        val taken =
            dao.listLiveLabels().any { it.id != exceptId && it.name.foldDiacritics() == key }
        ensure(!taken) { LabelError.DuplicateName }
    }

    context(raise: Raise<LabelError>)
    private suspend fun liveGroup(id: Uuid): LabelGroupEntity =
        ensureNotNull(dao.getGroupById(id)?.takeIf { it.deletedAt == null }) {
            LabelError.GroupNotFound
        }

    context(raise: Raise<LabelError>)
    private suspend fun liveLabel(id: Uuid): LabelEntity =
        ensureNotNull(dao.getLabelById(id)?.takeIf { it.deletedAt == null }) { LabelError.NotFound }

    /** Live labels of [groupId], or of "Khác" when it is null. */
    private suspend fun labelsOf(groupId: Uuid?): List<LabelEntity> =
        if (groupId == null) dao.listLiveLabelsInOther() else dao.listLiveLabelsInGroup(groupId)

    private suspend fun nextLabelSortOrder(groupId: Uuid?): Int {
        val max =
            if (groupId == null) dao.maxLabelSortOrderInOther()
            else dao.maxLabelSortOrderInGroup(groupId)
        return (max ?: -1) + 1
    }

    /** [orderedIds] must be exactly the live set, each id once; otherwise nothing is written. */
    context(raise: Raise<LabelError>)
    private fun ensureSameIds(orderedIds: List<Uuid>, liveIds: List<Uuid>, error: LabelError) {
        ensure(orderedIds.size == liveIds.size && orderedIds.toSet() == liveIds.toSet()) { error }
    }
}
