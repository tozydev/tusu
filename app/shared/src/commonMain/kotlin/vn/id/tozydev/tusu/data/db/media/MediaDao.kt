package vn.id.tozydev.tusu.data.db.media

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MediaDao {
    @Upsert abstract suspend fun upsert(media: MediaEntity)

    @Query("SELECT * FROM media WHERE id = :id")
    abstract suspend fun getById(id: Uuid): MediaEntity?

    @Query("DELETE FROM media WHERE id = :id") abstract suspend fun deleteById(id: Uuid)

    /** Live media of one entry in album order; trashed items are left out. */
    @Query("SELECT * FROM media WHERE entry_id = :entryId AND deleted_at IS NULL ORDER BY position")
    abstract fun observeByEntry(entryId: Uuid): Flow<List<MediaEntity>>
}
