package vn.id.tozydev.tusu.data.db.entry

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity

/**
 * Queries observe every table they read, so a write to any of them re-emits. Paging queries return
 * a `PagingSource` that Room recreates when the data changes.
 */
@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
abstract class EntryDao {
    @Upsert abstract suspend fun upsert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract suspend fun getById(id: Uuid): EntryEntity?

    @Query("DELETE FROM entries WHERE id = :id") abstract suspend fun deleteById(id: Uuid)

    /** Counts every row, trashed ones included. */
    @Query("SELECT COUNT(*) FROM entries") abstract suspend fun countAll(): Int

    @Insert abstract suspend fun insertFts(row: EntryFtsEntity)

    @Query("DELETE FROM entries_fts WHERE entry_id = :entryId")
    abstract suspend fun deleteFtsByEntryId(entryId: Uuid)

    /** [query] is an FTS5 MATCH expression; the repository folds and escapes it first. */
    @Query("SELECT entry_id FROM entries_fts WHERE entries_fts MATCH :query")
    abstract fun searchEntryIds(query: String): Flow<List<Uuid>>

    @Query("SELECT * FROM entry_labels WHERE entry_id = :entryId")
    abstract suspend fun getEntryLabelRows(entryId: Uuid): List<EntryLabelEntity>

    /** Names of the live labels of an entry in display order; the FTS row stores them folded. */
    @Query(
        """
        SELECT l.name FROM entry_labels el
        JOIN labels l ON l.id = el.label_id
        WHERE el.entry_id = :entryId AND el.deleted_at IS NULL AND l.deleted_at IS NULL
        ORDER BY l.sort_order
        """,
    )
    abstract suspend fun liveLabelNames(entryId: Uuid): List<String>

    /** Every entry that has a link to the label, including soft-deleted links. */
    @Query("SELECT DISTINCT entry_id FROM entry_labels WHERE label_id = :labelId")
    abstract suspend fun entryIdsWithLabel(labelId: Uuid): List<Uuid>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    abstract fun observeWithRelations(id: Uuid): Flow<EntryWithRelations?>

    @Transaction
    @Query("SELECT * FROM entries WHERE deleted_at IS NULL ORDER BY occurred_at DESC, id DESC")
    abstract fun timelinePagingSource(): PagingSource<Int, EntryWithRelations>

    @Transaction
    @Query(
        """
        SELECT * FROM entries
        WHERE local_date = :date AND deleted_at IS NULL
        ORDER BY occurred_at ASC, id ASC
        """,
    )
    abstract fun observeDay(date: LocalDate): Flow<List<EntryWithRelations>>

    @Transaction
    @Query("SELECT * FROM entries WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC, id DESC")
    abstract fun observeTrash(): Flow<List<EntryWithRelations>>

    /**
     * The emoji is the one of the latest live entry of the day that has an emoji; the correlated
     * subquery keeps that choice in SQL.
     */
    @Query(
        """
        SELECT e.local_date AS date, COUNT(*) AS entryCount,
            (SELECT e2.emoji FROM entries e2
             WHERE e2.local_date = e.local_date AND e2.deleted_at IS NULL AND e2.emoji IS NOT NULL
             ORDER BY e2.occurred_at DESC, e2.id DESC LIMIT 1) AS emoji
        FROM entries e
        WHERE e.deleted_at IS NULL AND e.local_date BETWEEN :from AND :to
        GROUP BY e.local_date
        ORDER BY e.local_date
        """,
    )
    abstract fun observeCalendarRows(from: LocalDate, to: LocalDate): Flow<List<CalendarRow>>

    @Query(
        """
        SELECT place_name, place_address, latitude, longitude,
            MAX(map_snapshot_path) AS map_snapshot_path
        FROM entries
        WHERE deleted_at IS NULL AND place_name IS NOT NULL
        GROUP BY place_name, place_address, latitude, longitude
        ORDER BY MAX(occurred_at) DESC
        LIMIT :limit
        """,
    )
    abstract fun observeRecentLocationRows(limit: Int): Flow<List<LocationRow>>

    /**
     * The SQL comes from [toRoomRawQuery]; the observed tables are every table the filter reads.
     */
    @Transaction
    @RawQuery(
        observedEntities =
            [
                EntryEntity::class,
                EntryFtsEntity::class,
                vn.id.tozydev.tusu.data.db.media.MediaEntity::class,
                EntryLabelEntity::class,
                vn.id.tozydev.tusu.data.db.label.LabelEntity::class,
            ],
    )
    abstract fun searchPagingSource(query: RoomRawQuery): PagingSource<Int, EntryWithRelations>
}
