package vn.io.tozyworks.tusu.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import vn.io.tozyworks.tusu.domain.model.Entry

interface EntryRepository {
    fun getEntriesPagingFlow(pageSize: Int = 20): Flow<PagingData<Entry>>
}
