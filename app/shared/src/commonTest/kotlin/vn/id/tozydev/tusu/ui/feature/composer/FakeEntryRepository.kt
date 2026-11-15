package vn.id.tozydev.tusu.ui.feature.composer

import androidx.paging.PagingData
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.repository.EntryRepository

/** In-memory entries that record every write for the composer tests. */
class FakeEntryRepository : EntryRepository {
    /** Entries that exist before the test writes anything. */
    val entries = mutableMapOf<Uuid, Entry>()

    val saves = mutableListOf<EntryDraft>()
    val trashed = mutableListOf<Uuid>()
    val restored = mutableListOf<Uuid>()
    val deleted = mutableListOf<Uuid>()

    /** The next [save] records the attempt and fails with [EntryError.StorageFailed]. */
    var failNextSave = false

    override fun observeEntry(id: Uuid): Flow<Entry?> = flow { emit(entries[id]) }

    override suspend fun save(draft: EntryDraft): Outcome<Unit, EntryError> {
        saves += draft
        if (failNextSave) {
            failNextSave = false
            return Outcome.Failure(EntryError.StorageFailed)
        }
        return Outcome.Success(Unit)
    }

    override suspend fun moveToTrash(id: Uuid): Outcome<Unit, EntryError> {
        trashed += id
        return Outcome.Success(Unit)
    }

    override suspend fun restoreFromTrash(id: Uuid): Outcome<Unit, EntryError> {
        restored += id
        return Outcome.Success(Unit)
    }

    override suspend fun deletePermanently(id: Uuid): Outcome<Unit, EntryError> {
        deleted += id
        return Outcome.Success(Unit)
    }

    override fun observeTimeline(): Flow<PagingData<Entry>> = error("not used")

    override fun observeCalendar(month: YearMonth): Flow<List<CalendarDay>> = error("not used")

    override fun observeDay(date: LocalDate): Flow<List<Entry>> = error("not used")

    override fun search(filter: EntryFilter): Flow<PagingData<Entry>> = error("not used")

    override fun observeTrash(): Flow<List<Entry>> = error("not used")

    override fun observeRecentLocations(limit: Int): Flow<List<EntryLocation>> = error("not used")

    override suspend fun setLabels(entryId: Uuid, labelIds: Set<Uuid>): Outcome<Unit, EntryError> =
        error("not used")
}
