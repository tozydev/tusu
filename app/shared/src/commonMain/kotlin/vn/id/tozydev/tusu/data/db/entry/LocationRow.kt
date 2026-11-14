package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.ColumnInfo

/** A distinct place of the live entries; [mapSnapshotPath] is one of the group's snapshots. */
data class LocationRow(
    @ColumnInfo(name = "place_name") val placeName: String,
    @ColumnInfo(name = "place_address") val placeAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "map_snapshot_path") val mapSnapshotPath: String?,
)
