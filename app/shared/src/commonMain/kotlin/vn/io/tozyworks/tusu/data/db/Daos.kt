package vn.io.tozyworks.tusu.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlin.uuid.Uuid

@Dao
interface EntryDao {
    @Transaction
    @Query("select * from entries order by recorded_at desc")
    fun pagingSource(): PagingSource<Int, EntryWithRelations>

    @Query("SELECT id FROM entries LIMIT 1") suspend fun getFirstEntryId(): Uuid?

    @Insert suspend fun insertAll(entries: List<EntryEntity>)
}

@Dao
interface TagDao {
    @Insert suspend fun insertAll(tagSeeds: List<TagEntity>)
}

@Dao interface MediaDao {}

@Dao
interface EntryTagDao {
    @Insert suspend fun insertAll(entryTagSeeds: List<EntryTagCrossRef>)
}
