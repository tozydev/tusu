package vn.id.tozydev.tusu.data.db.label

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "labels",
    foreignKeys =
        [
            ForeignKey(
                entity = LabelGroupEntity::class,
                parentColumns = ["id"],
                childColumns = ["group_id"],
                onDelete = ForeignKey.SET_NULL,
            ),
        ],
    indices = [Index(value = ["group_id"])],
)
data class LabelEntity(
    @PrimaryKey val id: Uuid,
    /** Null places the label in the built-in "Khác" group. */
    @ColumnInfo(name = "group_id") val groupId: Uuid?,
    val name: String,
    @ColumnInfo(name = "is_hidden") val isHidden: Boolean,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
