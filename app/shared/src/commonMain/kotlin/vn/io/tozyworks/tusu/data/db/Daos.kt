package vn.io.tozyworks.tusu.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entry: EntryEntity)

    @Delete suspend fun delete(entry: EntryEntity)

    @Query("SELECT count(*) FROM entries") suspend fun count(): Int

    @Query("SELECT * FROM entries ORDER BY recorded_at DESC")
    fun getAllAsFlow(): Flow<List<EntryEntity>>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryWithRelations(id: Uuid): EntryWithRelations?

    @Transaction
    @Query("SELECT * FROM entries ORDER BY recorded_at DESC")
    fun getAllEntriesWithRelationsFlow(): Flow<List<EntryWithRelations>>

    @Query("SELECT id FROM entries LIMIT 1") suspend fun getFirstEntryId(): Uuid?

    @Insert suspend fun insertAll(entries: List<EntryEntity>)
}

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(tag: TagEntity)

    @Delete suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags") fun getAllAsFlow(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name") suspend fun getByName(name: String): TagEntity?

    @Insert suspend fun insertAll(tagSeeds: List<TagEntity>)
}

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(media: MediaEntity)

    @Delete suspend fun delete(media: MediaEntity)

    @Query("SELECT * FROM media WHERE entry_id = :entryId")
    suspend fun getByEntryId(entryId: Uuid): List<MediaEntity>
}

@Dao
interface EntryTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(crossRef: EntryTagCrossRef)

    @Delete suspend fun delete(crossRef: EntryTagCrossRef)

    @Insert suspend fun insertAll(entryTagSeeds: List<EntryTagCrossRef>)
}
