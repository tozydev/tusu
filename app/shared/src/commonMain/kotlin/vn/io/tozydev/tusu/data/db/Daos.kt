package vn.io.tozydev.tusu.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Transaction
    @Query(
        """
        select distinct e.* from entries e
        left join media m on e.id = m.entry_id
        order by e.recorded_at desc, m.`order` asc
        """
    )
    fun pagingSource(): PagingSource<Int, EntryWithRelations>

    @Transaction
    @Query(
        """
        select distinct e.* from entries e
        left join media m on e.id = m.entry_id
        left join entry_tags et on e.id = et.entry_id
        where et.tag_id = :tagId
        order by e.recorded_at desc, m.`order` asc
        """
    )
    fun pagingSourceWithTagFilter(tagId: Uuid): PagingSource<Int, EntryWithRelations>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    fun getAsFlow(id: Uuid): Flow<EntryWithRelations?>

    @Transaction
    @Query(
        """
        select distinct e.* from entries e
        left join media m on e.id = m.entry_id
        where e.id = :id
        order by m.`order` asc
        """
    )
    suspend fun getEntry(id: Uuid): EntryWithRelations?

    @Insert suspend fun insert(entity: EntryEntity)

    @Query("UPDATE entries SET content = :newContent WHERE id = :entryId")
    suspend fun updateContent(entryId: Uuid, newContent: String)

    @Query("UPDATE entries SET recorded_at = :newRecordedAt WHERE id = :entryId")
    suspend fun updateRecordedAt(entryId: Uuid, newRecordedAt: Instant)

    @Query("UPDATE entries SET emoji = :newEmoji WHERE id = :entryId")
    suspend fun updateEmoji(entryId: Uuid, newEmoji: String?)

    @Query("DELETE FROM entries WHERE id = :entryId") suspend fun delete(entryId: Uuid)

    @Query("SELECT id FROM entries LIMIT 1") suspend fun getFirstEntryId(): Uuid?

    @Insert suspend fun insertAll(entries: List<EntryEntity>)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags") fun getAllAsFlow(): Flow<List<TagEntity>>

    @Insert suspend fun insertAll(tags: List<TagEntity>)
}

@Dao
interface MediaDao {

    @Query("SELECT * FROM media WHERE id = :mediaId ")
    suspend fun getMedia(mediaId: Uuid): MediaEntity?

    @Query("DELETE FROM media WHERE id = :mediaId") suspend fun deleteById(mediaId: Uuid)

    @Transaction
    suspend fun deleteAndReturning(mediaId: Uuid): MediaEntity? {
        val entity = getMedia(mediaId)
        deleteById(mediaId)
        return entity
    }

    @Insert suspend fun insertAll(media: List<MediaEntity>)
}

@Dao
interface EntryTagDao {
    @Insert suspend fun insertAll(entryTags: List<EntryTagCrossRef>)
}
