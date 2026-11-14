package vn.id.tozydev.tusu.domain.repository

import androidx.paging.PagingData
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.domain.model.EntryLocation

interface EntryRepository {
    /** Emits null when the entry does not exist; an entry in the trash is still emitted. */
    fun observeEntry(id: Uuid): Flow<Entry?>

    /** Entries outside the trash, newest first. */
    fun observeTimeline(): Flow<PagingData<Entry>>

    fun observeCalendar(month: YearMonth): Flow<List<CalendarDay>>

    /** Entries of one day outside the trash, in chronological order. */
    fun observeDay(date: LocalDate): Flow<List<Entry>>

    fun search(filter: EntryFilter): Flow<PagingData<Entry>>

    /** Entries in the trash, most recently deleted first. */
    fun observeTrash(): Flow<List<Entry>>

    /** Locations of the most recent entries, without duplicates. */
    fun observeRecentLocations(limit: Int): Flow<List<EntryLocation>>

    /** Creates the entry or updates the one with the same id. */
    suspend fun save(draft: EntryDraft): Outcome<Unit, EntryError>

    /** Replaces the labels of the entry with exactly [labelIds]. */
    suspend fun setLabels(entryId: Uuid, labelIds: Set<Uuid>): Outcome<Unit, EntryError>

    suspend fun moveToTrash(id: Uuid): Outcome<Unit, EntryError>

    suspend fun restoreFromTrash(id: Uuid): Outcome<Unit, EntryError>

    /** Removes the entry's rows; its media files are removed through [MediaRepository]. */
    suspend fun deletePermanently(id: Uuid): Outcome<Unit, EntryError>
}
