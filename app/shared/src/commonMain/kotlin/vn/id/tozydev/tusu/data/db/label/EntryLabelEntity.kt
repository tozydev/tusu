package vn.id.tozydev.tusu.data.db.label

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.entry.EntryEntity

@Entity(
    tableName = "entry_labels",
    primaryKeys = ["entry_id", "label_id"],
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["entry_id"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = LabelEntity::class,
                parentColumns = ["id"],
                childColumns = ["label_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index(value = ["label_id"])],
)
data class EntryLabelEntity(
    @ColumnInfo(name = "entry_id") val entryId: Uuid,
    @ColumnInfo(name = "label_id") val labelId: Uuid,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
