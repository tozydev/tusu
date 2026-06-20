package vn.io.tozyworks.tusu.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "entries",
    indices = [Index("recorded_at")],
)
data class EntryEntity(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "recorded_at") val recordedAt: Instant,
    val content: String,
    val emoji: String?,
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)],
)
data class TagEntity(
    @PrimaryKey val id: Uuid,
    val name: String,
)

@Entity(
    tableName = "entry_tags",
    primaryKeys = ["entry_id", "tag_id"],
    indices = [Index("tag_id")],
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["entry_id"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = TagEntity::class,
                parentColumns = ["id"],
                childColumns = ["tag_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class EntryTagCrossRef(
    @ColumnInfo(name = "entry_id") val entryId: Uuid,
    @ColumnInfo(name = "tag_id") val tagId: Uuid,
)

@Entity(
    tableName = "media",
    indices = [Index("entry_id")],
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["entry_id"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class MediaEntity(
    @PrimaryKey val id: Uuid,
    @ColumnInfo(name = "entry_id") val entryId: Uuid,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    val filename: String,
    val path: String,
    val order: Float,
    val height: Int?,
    val width: Int?,
)
