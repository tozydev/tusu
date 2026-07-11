package vn.id.tozydev.tusu.data.db

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

    @Query("update entries set content = :newContent where id = :entryId")
    suspend fun updateContent(entryId: Uuid, newContent: String)

    @Query("update entries set recorded_at = :newRecordedAt where id = :entryId")
    suspend fun updateRecordedAt(entryId: Uuid, newRecordedAt: Instant)

    @Query("update entries set emoji = :newEmoji where id = :entryId")
    suspend fun updateEmoji(entryId: Uuid, newEmoji: String?)

    @Query("insert into entry_tags (entry_id, tag_id) values(:entryId, :tagId)")
    suspend fun assignTag(entryId: Uuid, tagId: Uuid)

    @Query("delete from entry_tags where entry_id = :entryId and tag_id = :tagId")
    suspend fun unassignTag(entryId: Uuid, tagId: Uuid)

    @Query("delete from entries where id = :entryId") suspend fun delete(entryId: Uuid)

    @Query("select * from entries") suspend fun getAll(): List<EntryEntity>

    @Query("delete from entries") suspend fun deleteAll()

    @Query("select id from entries LIMIT 1") suspend fun getFirstEntryId(): Uuid?

    @Insert suspend fun insertAll(entries: List<EntryEntity>)

    @Transaction
    suspend fun restoreData(
        entries: List<EntryEntity>,
        tags: List<TagEntity>,
        entryTags: List<EntryTagCrossRef>,
        media: List<MediaEntity>,
        tagDao: TagDao,
        entryTagDao: EntryTagDao,
        mediaDao: MediaDao,
    ) {
        deleteAll()
        tagDao.deleteAll()
        entryTagDao.deleteAll()
        mediaDao.deleteAll()

        insertAll(entries)
        tagDao.insertAll(tags)
        entryTagDao.insertAll(entryTags)
        mediaDao.insertAll(media)
    }
}

@Dao
interface TagDao {
    @Query("select * from tags") fun getAllAsFlow(): Flow<List<TagEntity>>

    @Query("select * from tags") suspend fun getAll(): List<TagEntity>

    @Query("delete from tags") suspend fun deleteAll()

    @Insert suspend fun insert(tag: TagEntity)

    @Insert suspend fun insertAll(tags: List<TagEntity>)
}

@Dao
interface MediaDao {

    @Query("select * from media where id = :mediaId ")
    suspend fun getMedia(mediaId: Uuid): MediaEntity?

    @Query("delete from media where id = :mediaId") suspend fun deleteById(mediaId: Uuid)

    @Transaction
    suspend fun deleteAndReturning(mediaId: Uuid): MediaEntity? {
        val entity = getMedia(mediaId)
        deleteById(mediaId)
        return entity
    }

    @Query("select * from media") suspend fun getAll(): List<MediaEntity>

    @Query("delete from media") suspend fun deleteAll()

    @Insert suspend fun insertAll(media: List<MediaEntity>)
}

@Dao
interface EntryTagDao {
    @Query("select * from entry_tags") suspend fun getAll(): List<EntryTagCrossRef>

    @Query("delete from entry_tags") suspend fun deleteAll()

    @Insert suspend fun insertAll(entryTags: List<EntryTagCrossRef>)
}
