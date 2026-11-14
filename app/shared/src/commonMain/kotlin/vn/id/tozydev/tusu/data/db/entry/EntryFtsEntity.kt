package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Fts5
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

/**
 * Standalone FTS5 index rather than an external-content table: `label_names` is not a column of
 * `entries`. The repository rewrites the row in the same transaction as the entry.
 */
@Entity(tableName = "entries_fts")
@Fts5(notIndexed = ["entry_id"])
data class EntryFtsEntity(
    /** Null lets FTS5 assign the rowid; FTS5 requires the primary key to be the rowid. */
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long?,
    @ColumnInfo(name = "entry_id") val entryId: Uuid,
    @ColumnInfo(name = "text_search") val textSearch: String,
    @ColumnInfo(name = "label_names") val labelNames: String,
    @ColumnInfo(name = "place_name") val placeName: String,
)
