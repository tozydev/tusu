package vn.id.tozydev.tusu.ui.feature.composer

import androidx.paging.PagingData
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaRepository

/** Media kept in memory for the composer tests; observers see every change at once. */
class FakeMediaRepository : MediaRepository {
    private val items = MutableStateFlow<List<Media>>(emptyList())

    /** All stored media, including soft-deleted items; a test sets it to arrange the data. */
    var media: List<Media>
        get() = items.value
        set(value) {
            items.value = value
        }

    override fun observeEntryMedia(entryId: Uuid): Flow<List<Media>> = items.map { list ->
        list.filter { it.entryId == entryId }.sortedBy { it.position }
    }

    override fun observeLibrary(type: MediaType?): Flow<PagingData<Media>> = error("not used")

    override suspend fun getByStatus(status: MediaStatus): List<Media> = error("not used")

    override suspend fun add(media: Media): Outcome<Media, MediaError> {
        items.update { it + media }
        return Outcome.Success(media)
    }

    override suspend fun setStatus(id: Uuid, status: MediaStatus): Outcome<Unit, MediaError> =
        updateMedia(id) { it.copy(status = status) }

    override suspend fun reorder(entryId: Uuid, orderedIds: List<Uuid>): Outcome<Unit, MediaError> =
        error("not used")

    override suspend fun remove(id: Uuid): Outcome<Unit, MediaError> =
        updateMedia(id) { it.copy(deletedAt = Clock.System.now()) }

    override suspend fun restore(id: Uuid): Outcome<Unit, MediaError> =
        updateMedia(id) { it.copy(deletedAt = null) }

    override suspend fun deletePermanently(id: Uuid): Outcome<Unit, MediaError> = error("not used")

    private fun updateMedia(id: Uuid, transform: (Media) -> Media): Outcome<Unit, MediaError> {
        if (items.value.none { it.id == id }) return Outcome.Failure(MediaError.NotFound)
        items.update { list -> list.map { if (it.id == id) transform(it) else it } }
        return Outcome.Success(Unit)
    }
}
