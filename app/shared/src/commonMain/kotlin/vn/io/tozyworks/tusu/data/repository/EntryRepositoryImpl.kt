package vn.io.tozyworks.tusu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.tozyworks.tusu.data.db.EntryDao
import vn.io.tozyworks.tusu.data.db.toModel
import vn.io.tozyworks.tusu.domain.model.Entry
import vn.io.tozyworks.tusu.domain.repository.EntryRepository

@Inject
@ContributesBinding(AppScope::class, binding<EntryRepository>())
class EntryRepositoryImpl(private val entryDao: EntryDao) : EntryRepository {
    override fun getEntriesPagingFlow(pageSize: Int): Flow<PagingData<Entry>> {
        return Pager(
                config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
                pagingSourceFactory = { entryDao.pagingSource() },
            )
            .flow
            .map { data -> data.map { it.toModel() } }
    }
}
