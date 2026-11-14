package vn.id.tozydev.tusu.data.db.label

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LabelDao {
    @Upsert abstract suspend fun upsertGroup(group: LabelGroupEntity)

    @Query("SELECT * FROM label_groups WHERE id = :id")
    abstract suspend fun getGroupById(id: Uuid): LabelGroupEntity?

    /** Live groups in display order. */
    @Query("SELECT * FROM label_groups WHERE deleted_at IS NULL ORDER BY sort_order")
    abstract fun observeGroups(): Flow<List<LabelGroupEntity>>

    @Upsert abstract suspend fun upsertLabel(label: LabelEntity)

    @Query("SELECT * FROM labels WHERE id = :id")
    abstract suspend fun getLabelById(id: Uuid): LabelEntity?

    /** Live labels of every group in display order. */
    @Query("SELECT * FROM labels WHERE deleted_at IS NULL ORDER BY sort_order")
    abstract fun observeLabels(): Flow<List<LabelEntity>>

    @Upsert abstract suspend fun upsertEntryLabel(entryLabel: EntryLabelEntity)

    @Query("DELETE FROM entry_labels WHERE entry_id = :entryId AND label_id = :labelId")
    abstract suspend fun deleteEntryLabel(entryId: Uuid, labelId: Uuid)

    @Query("SELECT * FROM entry_labels WHERE entry_id = :entryId")
    abstract fun observeEntryLabels(entryId: Uuid): Flow<List<EntryLabelEntity>>
}
