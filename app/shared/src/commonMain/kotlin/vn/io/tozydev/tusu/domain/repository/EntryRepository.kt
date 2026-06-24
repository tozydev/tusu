package vn.io.tozydev.tusu.domain.repository

import androidx.paging.PagingData
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import vn.io.tozydev.tusu.domain.model.Entry

interface EntryRepository {
    fun getEntriesPagingFlow(pageSize: Int = 20, tagIdFilter: Uuid? = null): Flow<PagingData<Entry>>

    suspend fun getEntry(id: Uuid): Entry?

    suspend fun createEmptyEntry(): Entry

    suspend fun updateContent(entryId: Uuid, newContent: String)

    suspend fun updateRecordedAt(entryId: Uuid, newRecordedAt: Instant)

    suspend fun updateEmoji(entryId: Uuid, newEmoji: String?)

    suspend fun deleteEntry(entryId: Uuid)
}
