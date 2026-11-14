package vn.id.tozydev.tusu.data.db.media

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
abstract class MediaDao {
    @Upsert abstract suspend fun upsert(media: MediaEntity)

    /** Plain insert, so an id that is already stored fails instead of replacing another row. */
    @Insert abstract suspend fun insert(media: MediaEntity)

    @Query("SELECT * FROM media WHERE id = :id")
    abstract suspend fun getById(id: Uuid): MediaEntity?

    @Query("DELETE FROM media WHERE id = :id") abstract suspend fun deleteById(id: Uuid)

    /** Live media of one entry in album order; trashed items are left out. */
    @Query("SELECT * FROM media WHERE entry_id = :entryId AND deleted_at IS NULL ORDER BY position")
    abstract fun observeByEntry(entryId: Uuid): Flow<List<MediaEntity>>

    @Query(
        "SELECT * FROM media WHERE status = :status AND deleted_at IS NULL ORDER BY updated_at, id",
    )
    abstract suspend fun getByStatus(status: MediaStatus): List<MediaEntity>

    /**
     * Ready, live media of live entries, newest first. A null [type] means every type. `taken_at`
     * falls back to `updated_at` so items without a capture time still sort by when they arrived.
     */
    @Query(
        """
        SELECT media.* FROM media
        INNER JOIN entries ON entries.id = media.entry_id
        WHERE media.deleted_at IS NULL
          AND entries.deleted_at IS NULL
          AND media.status = 'READY'
          AND (:type IS NULL OR media.type = :type)
        ORDER BY COALESCE(media.taken_at, media.updated_at) DESC, media.id DESC
        """,
    )
    abstract fun pageLibrary(type: MediaType?): PagingSource<Int, MediaEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM entries WHERE id = :entryId)")
    abstract suspend fun entryExists(entryId: Uuid): Boolean

    /** Ids of the entry's live media in album order. */
    @Query(
        "SELECT id FROM media WHERE entry_id = :entryId AND deleted_at IS NULL ORDER BY position",
    )
    abstract suspend fun liveIdsOf(entryId: Uuid): List<Uuid>

    /** -1 when the entry has no live media, so the next position is 0. */
    @Query(
        "SELECT COALESCE(MAX(position), -1) FROM media WHERE entry_id = :entryId AND deleted_at IS NULL",
    )
    abstract suspend fun maxLivePosition(entryId: Uuid): Int

    /**
     * Whether another live item of the entry already sits at [position]; [id] itself is ignored.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM media
            WHERE entry_id = :entryId AND position = :position AND deleted_at IS NULL AND id != :id
        )
        """,
    )
    abstract suspend fun isPositionTaken(entryId: Uuid, position: Int, id: Uuid): Boolean

    @Query("UPDATE media SET status = :status, updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun updateStatus(id: Uuid, status: MediaStatus, updatedAt: Instant)

    @Query("UPDATE media SET position = :position, updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun updatePosition(id: Uuid, position: Int, updatedAt: Instant)

    @Query("UPDATE media SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    abstract suspend fun updateDeletedAt(id: Uuid, deletedAt: Instant?, updatedAt: Instant)
}
