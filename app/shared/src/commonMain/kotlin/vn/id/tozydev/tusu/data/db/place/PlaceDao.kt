package vn.id.tozydev.tusu.data.db.place

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaceDao {
    @Upsert abstract suspend fun upsert(place: PlaceEntity)

    @Query("SELECT * FROM places WHERE id = :id")
    abstract suspend fun getById(id: Uuid): PlaceEntity?

    @Query("DELETE FROM places WHERE id = :id") abstract suspend fun deleteById(id: Uuid)

    /** Live places in display order. */
    @Query("SELECT * FROM places WHERE deleted_at IS NULL ORDER BY sort_order")
    abstract fun observeAll(): Flow<List<PlaceEntity>>
}
