package vn.id.tozydev.tusu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.SQLiteException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.media.MediaDao
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.data.db.media.toDomain
import vn.id.tozydev.tusu.data.db.media.toEntity
import vn.id.tozydev.tusu.data.db.transaction
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.Raise
import vn.id.tozydev.tusu.domain.error.bind
import vn.id.tozydev.tusu.domain.error.ensure
import vn.id.tozydev.tusu.domain.error.ensureNotNull
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository

private val logger = KotlinLogging.logger {}

private const val LIBRARY_PAGE_SIZE = 60

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class MediaRepositoryImpl(
    private val database: TusuDatabase,
    private val files: MediaFileStore,
    private val clock: Clock,
) : MediaRepository {
    private val dao: MediaDao
        get() = database.mediaDao()

    override fun observeEntryMedia(entryId: Uuid): Flow<List<Media>> =
        dao.observeByEntry(entryId).map { rows -> rows.map(MediaEntity::toDomain) }

    override fun observeLibrary(type: MediaType?): Flow<PagingData<Media>> =
        Pager(PagingConfig(pageSize = LIBRARY_PAGE_SIZE)) { dao.pageLibrary(type) }
            .flow
            .map { page -> page.map(MediaEntity::toDomain) }

    override suspend fun getByStatus(status: MediaStatus): List<Media> =
        dao.getByStatus(status).map(MediaEntity::toDomain)

    override suspend fun add(media: Media): Outcome<Media, MediaError> = outcome {
        writeStorage {
            database.transaction {
                ensure(dao.entryExists(media.entryId)) { MediaError.NotFound }
                // Callers pick the position from a snapshot, so two concurrent adds can ask for
                // the same slot. The check runs inside the write transaction, where it is exact.
                val position =
                    if (dao.isPositionTaken(media.entryId, media.position, media.id)) {
                        dao.maxLivePosition(media.entryId) + 1
                    } else {
                        media.position
                    }
                val stored = media.copy(position = position, updatedAt = clock.now())
                dao.insert(stored.toEntity())
                stored
            }
        }
    }

    override suspend fun setStatus(id: Uuid, status: MediaStatus): Outcome<Unit, MediaError> =
        outcome {
            writeStorage {
                database.transaction {
                    ensureNotNull(dao.getById(id)) { MediaError.NotFound }
                    dao.updateStatus(id, status, clock.now())
                }
            }
        }

    override suspend fun reorder(entryId: Uuid, orderedIds: List<Uuid>): Outcome<Unit, MediaError> =
        outcome {
            writeStorage {
                database.transaction {
                    val liveIds = dao.liveIdsOf(entryId)
                    // Same ids and same count: a trashed or foreign id, or a repeated one, would
                    // leave a gap or a clash in the positions.
                    ensure(
                        orderedIds.size == liveIds.size && orderedIds.toSet() == liveIds.toSet(),
                    ) {
                        MediaError.NotFound
                    }
                    val now = clock.now()
                    orderedIds.forEachIndexed { index, id -> dao.updatePosition(id, index, now) }
                }
            }
        }

    override suspend fun remove(id: Uuid): Outcome<Unit, MediaError> = outcome {
        writeStorage {
            database.transaction {
                val row = ensureNotNull(dao.getById(id)) { MediaError.NotFound }
                // Removing twice keeps the first deletion time, so the trash timer is not reset.
                if (row.deletedAt == null) {
                    val now = clock.now()
                    dao.updateDeletedAt(id, now, now)
                }
            }
        }
    }

    override suspend fun restore(id: Uuid): Outcome<Unit, MediaError> = outcome {
        writeStorage {
            database.transaction {
                val row = ensureNotNull(dao.getById(id)) { MediaError.NotFound }
                if (row.deletedAt != null) {
                    val now = clock.now()
                    // The old slot may have been taken while the item was in the trash; then it
                    // goes to the end of the album instead of sharing a position.
                    if (dao.isPositionTaken(row.entryId, row.position, id)) {
                        dao.updatePosition(id, dao.maxLivePosition(row.entryId) + 1, now)
                    }
                    dao.updateDeletedAt(id, null, now)
                }
            }
        }
    }

    override suspend fun deletePermanently(id: Uuid): Outcome<Unit, MediaError> = outcome {
        val row: MediaEntity = writeStorage {
            database.transaction {
                val row = ensureNotNull(dao.getById(id)) { MediaError.NotFound }
                dao.deleteById(id)
                row
            }
        }
        // The row goes first on purpose: an orphan file is harmless and a later sweep removes it,
        // while a row pointing at a missing file breaks the entry's album.
        files.delete(row.localPath).bind()
    }
}

/**
 * Maps SQLite driver failures to [MediaError.StorageFailed]; every other exception passes through.
 */
context(raise: Raise<MediaError>)
private suspend fun <T> writeStorage(block: suspend () -> T): T =
    try {
        block()
    } catch (e: SQLiteException) {
        logger.warn(e) { "Could not write media rows" }
        raise.raise(MediaError.StorageFailed)
    }
