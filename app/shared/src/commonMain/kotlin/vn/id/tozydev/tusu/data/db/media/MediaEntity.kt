package vn.id.tozydev.tusu.data.db.media

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

@Entity(
    tableName = "media",
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["entry_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices =
        [
            Index(value = ["entry_id", "position"]),
            Index(value = ["type", "taken_at"], orders = [Index.Order.ASC, Index.Order.DESC]),
        ],
)
data class MediaEntity(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "entry_id") val entryId: Uuid,
    val type: MediaType,
    @ColumnInfo(name = "local_path") val localPath: String,
    @ColumnInfo(name = "remote_key") val remoteKey: String?,
    val width: Int?,
    val height: Int?,
    @ColumnInfo(name = "duration_ms") val durationMs: Long?,
    val position: Int,
    @ColumnInfo(name = "taken_at") val takenAt: Instant?,
    val status: MediaStatus,
    val waveform: List<Float>,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
