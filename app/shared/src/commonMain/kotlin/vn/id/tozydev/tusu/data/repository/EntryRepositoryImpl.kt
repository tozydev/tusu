package vn.id.tozydev.tusu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.id.tozydev.tusu.data.db.EntryDao
import vn.id.tozydev.tusu.data.db.EntryEntity
import vn.id.tozydev.tusu.data.db.toModel
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.repository.EntryRepository

@Inject
@ContributesBinding(AppScope::class, binding<EntryRepository>())
class EntryRepositoryImpl(private val entryDao: EntryDao, private val clock: Clock) :
    EntryRepository {
    override fun getEntriesPagingFlow(pageSize: Int, tagIdFilter: Uuid?): Flow<PagingData<Entry>> {
        return Pager(
                config =
                    PagingConfig(
                        pageSize = pageSize,
                        enablePlaceholders = false,
                        initialLoadSize = pageSize,
                    ),
                pagingSourceFactory = {
                    if (tagIdFilter == null) {
                        entryDao.pagingSource()
                    } else {
                        entryDao.pagingSourceWithTagFilter(tagIdFilter)
                    }
                },
            )
            .flow
            .map { data -> data.map { it.toModel() } }
    }

    override suspend fun getEntry(id: Uuid): Entry? = entryDao.getEntry(id)?.toModel()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createEmptyEntry(): Entry {
        val entryId = Uuid.generateV7()
        val entryEntity =
            EntryEntity(
                id = entryId,
                recordedAt = clock.now(),
                content = "",
                emoji = null,
            )
        entryDao.insert(entryEntity)
        return entryEntity.toModel()
    }

    override suspend fun updateContent(entryId: Uuid, newContent: String) {
        entryDao.updateContent(entryId, newContent)
    }

    override suspend fun updateRecordedAt(
        entryId: Uuid,
        newRecordedAt: Instant,
    ) {
        entryDao.updateRecordedAt(entryId, newRecordedAt)
    }

    override suspend fun updateEmoji(entryId: Uuid, newEmoji: String?) {
        entryDao.updateEmoji(entryId, newEmoji)
    }

    override suspend fun assignTag(entryId: Uuid, tagId: Uuid) {
        entryDao.assignTag(entryId, tagId)
    }

    override suspend fun unassignTag(entryId: Uuid, tagId: Uuid) {
        entryDao.unassignTag(entryId, tagId)
    }

    override suspend fun deleteEntry(entryId: Uuid) {
        entryDao.delete(entryId)
    }
}
