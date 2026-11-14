package vn.id.tozydev.tusu.data.db.label

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.LabelSelection

@Entity(tableName = "label_groups")
data class LabelGroupEntity(
    @PrimaryKey val id: Uuid,
    val name: String,
    /** ARGB, as in the domain model. */
    val color: Int,
    val selection: LabelSelection,
    @ColumnInfo(name = "show_in_composer") val showInComposer: Boolean,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
