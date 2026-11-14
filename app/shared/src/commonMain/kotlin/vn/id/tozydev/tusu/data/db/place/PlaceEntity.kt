package vn.id.tozydev.tusu.data.db.place

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: Uuid,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "deleted_at") val deletedAt: Instant?,
)
