package vn.id.tozydev.tusu.domain.repository

import androidx.paging.PagingData
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

interface MediaRepository {
    /** Media of one entry in album order, whatever their status. */
    fun observeEntryMedia(entryId: Uuid): Flow<List<Media>>

    /** Ready media of entries outside the trash, newest first; null [type] means every type. */
    fun observeLibrary(type: MediaType?): Flow<PagingData<Media>>

    /** Media whose background job has to be queued again, e.g. after the app was killed. */
    suspend fun getByStatus(status: MediaStatus): List<Media>

    /**
     * Returns the stored item. Its position moves to the end of the album when another live item
     * already holds the requested one.
     */
    suspend fun add(media: Media): Outcome<Media, MediaError>

    suspend fun setStatus(id: Uuid, status: MediaStatus): Outcome<Unit, MediaError>

    /** Sets album positions of the entry's media to the order of [orderedIds]. */
    suspend fun reorder(entryId: Uuid, orderedIds: List<Uuid>): Outcome<Unit, MediaError>

    /** Soft-deletes the media item; its file is kept until the trash is emptied. */
    suspend fun remove(id: Uuid): Outcome<Unit, MediaError>

    suspend fun restore(id: Uuid): Outcome<Unit, MediaError>

    /** Removes the row and the file. */
    suspend fun deletePermanently(id: Uuid): Outcome<Unit, MediaError>
}
