package vn.id.tozydev.tusu.data.db.label

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LabelDao {
    @Upsert abstract suspend fun upsertGroup(group: LabelGroupEntity)

    /** Live entries holding two or more live labels of [groupId]. */
    @Query(
        "SELECT COUNT(*) FROM (SELECT el.entry_id FROM entry_labels el " +
            "JOIN labels l ON l.id = el.label_id JOIN entries e ON e.id = el.entry_id " +
            "WHERE l.group_id = :groupId AND l.deleted_at IS NULL AND el.deleted_at IS NULL " +
            "AND e.deleted_at IS NULL GROUP BY el.entry_id HAVING COUNT(*) > 1)",
    )
    abstract suspend fun countEntriesWithSeveralLabels(groupId: Uuid): Int

    @Query("SELECT * FROM label_groups WHERE id = :id")
    abstract suspend fun getGroupById(id: Uuid): LabelGroupEntity?

    /** Live groups in display order. */
    @Query("SELECT * FROM label_groups WHERE deleted_at IS NULL ORDER BY sort_order, name")
    abstract fun observeGroups(): Flow<List<LabelGroupEntity>>

    @Query("SELECT * FROM label_groups WHERE deleted_at IS NULL")
    abstract suspend fun listLiveGroups(): List<LabelGroupEntity>

    @Query("SELECT MAX(sort_order) FROM label_groups WHERE deleted_at IS NULL")
    abstract suspend fun maxGroupSortOrder(): Int?

    @Upsert abstract suspend fun upsertLabel(label: LabelEntity)

    @Query("SELECT * FROM labels WHERE id = :id")
    abstract suspend fun getLabelById(id: Uuid): LabelEntity?

    /** Live labels of every group in display order. */
    @Query("SELECT * FROM labels WHERE deleted_at IS NULL ORDER BY sort_order, name")
    abstract fun observeLabels(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM labels WHERE deleted_at IS NULL")
    abstract suspend fun listLiveLabels(): List<LabelEntity>

    /** Live labels of one group, in display order. */
    @Query(
        "SELECT * FROM labels WHERE deleted_at IS NULL AND group_id = :groupId " +
            "ORDER BY sort_order, name",
    )
    abstract suspend fun listLiveLabelsInGroup(groupId: Uuid): List<LabelEntity>

    /** Live labels of the built-in "Khác" group (no group), in display order. */
    @Query(
        "SELECT * FROM labels WHERE deleted_at IS NULL AND group_id IS NULL " +
            "ORDER BY sort_order, name",
    )
    abstract suspend fun listLiveLabelsInOther(): List<LabelEntity>

    @Query(
        "SELECT MAX(sort_order) FROM labels WHERE deleted_at IS NULL AND group_id = :groupId",
    )
    abstract suspend fun maxLabelSortOrderInGroup(groupId: Uuid): Int?

    @Query("SELECT MAX(sort_order) FROM labels WHERE deleted_at IS NULL AND group_id IS NULL")
    abstract suspend fun maxLabelSortOrderInOther(): Int?

    @Upsert abstract suspend fun upsertEntryLabel(entryLabel: EntryLabelEntity)

    @Query("DELETE FROM entry_labels WHERE entry_id = :entryId AND label_id = :labelId")
    abstract suspend fun deleteEntryLabel(entryId: Uuid, labelId: Uuid)

    @Query("SELECT * FROM entry_labels WHERE entry_id = :entryId")
    abstract fun observeEntryLabels(entryId: Uuid): Flow<List<EntryLabelEntity>>

    /** Live entry-label rows of one label, across every entry. */
    @Query("SELECT * FROM entry_labels WHERE label_id = :labelId AND deleted_at IS NULL")
    abstract suspend fun listLiveEntryLabelsOfLabel(labelId: Uuid): List<EntryLabelEntity>
}
