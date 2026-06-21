package vn.io.tozyworks.tusu.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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
    fun pagingSourceWithTagFilter(tagId:Uuid): PagingSource<Int, EntryWithRelations>

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
    @Insert suspend fun insertAll(media: List<MediaEntity>)
}

@Dao
interface EntryTagDao {
    @Insert suspend fun insertAll(entryTags: List<EntryTagCrossRef>)
}
