package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "entries",
    indices =
        [
            Index(
                value = ["deleted_at", "occurred_at"],
                orders = [Index.Order.ASC, Index.Order.DESC],
            ),
            Index(value = ["local_date"]),
        ],
)
data class EntryEntity(
    @PrimaryKey val id: Uuid,
    val body: String,
    @ColumnInfo(name = "text_plain") val textPlain: String,
    @ColumnInfo(name = "text_search") val textSearch: String,
    val emoji: String?,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
    /**
     * Zone id such as `Asia/Ho_Chi_Minh`; kept as text because kotlinx-datetime has no column type.
     */
    val timezone: String,
    @ColumnInfo(name = "local_date") val localDate: LocalDate,
    @ColumnInfo(name = "place_name") val placeName: String?,
    @ColumnInfo(name = "place_address") val placeAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "map_snapshot_path") val mapSnapshotPath: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
