package vn.id.tozydev.tusu.data.db.entry

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EntryDao {
    @Upsert abstract suspend fun upsert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract suspend fun getById(id: Uuid): EntryEntity?

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract fun observeById(id: Uuid): Flow<EntryEntity?>

    @Query("DELETE FROM entries WHERE id = :id") abstract suspend fun deleteById(id: Uuid)

    @Insert abstract suspend fun insertFts(row: EntryFtsEntity)

    @Query("DELETE FROM entries_fts WHERE entry_id = :entryId")
    abstract suspend fun deleteFtsByEntryId(entryId: Uuid)

    /** [query] is an FTS5 MATCH expression; the repository folds and escapes it first. */
    @Query("SELECT entry_id FROM entries_fts WHERE entries_fts MATCH :query")
    abstract fun searchEntryIds(query: String): Flow<List<Uuid>>
}
