package vn.id.tozydev.tusu.ui.feature.composer

import androidx.paging.PagingData
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaRepository

/** Media of one entry, set directly by the test. */
class FakeMediaRepository : MediaRepository {
    var media: List<Media> = emptyList()

    override fun observeEntryMedia(entryId: Uuid): Flow<List<Media>> = flow { emit(media) }

    override fun observeLibrary(type: MediaType?): Flow<PagingData<Media>> = error("not used")

    override suspend fun getByStatus(status: MediaStatus): List<Media> = error("not used")

    override suspend fun add(media: Media): Outcome<Media, MediaError> = error("not used")

    override suspend fun setStatus(id: Uuid, status: MediaStatus): Outcome<Unit, MediaError> =
        error("not used")

    override suspend fun reorder(entryId: Uuid, orderedIds: List<Uuid>): Outcome<Unit, MediaError> =
        error("not used")

    override suspend fun remove(id: Uuid): Outcome<Unit, MediaError> = error("not used")

    override suspend fun restore(id: Uuid): Outcome<Unit, MediaError> = error("not used")

    override suspend fun deletePermanently(id: Uuid): Outcome<Unit, MediaError> = error("not used")
}
