package vn.id.tozydev.tusu.data.db.entry

import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.TransactionScope
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.text.foldDiacritics

/**
 * Rewrites the search row of one entry: its folded search text, live label names and folded place
 * name. The row is standalone, so it is rebuilt rather than updated, and it must be written in the
 * same transaction as the entry it describes.
 */
context(tx: TransactionScope)
suspend fun TusuDatabase.reindexEntry(entryId: Uuid) {
    val dao = entryDao()
    dao.deleteFtsByEntryId(entryId)
    val entry = dao.getById(entryId) ?: return
    dao.insertFts(
        EntryFtsEntity(
            rowId = null,
            entryId = entryId,
            textSearch = entry.textSearch,
            labelNames =
                dao.liveLabelNames(entryId).joinToString(separator = " ") { it.foldDiacritics() },
            placeName = entry.placeName?.foldDiacritics().orEmpty(),
        ),
    )
}

/** Rebuilds the search row of every entry that links to [labelId], after a rename or delete. */
context(tx: TransactionScope)
suspend fun TusuDatabase.reindexEntriesWithLabel(labelId: Uuid) {
    for (entryId in entryDao().entryIdsWithLabel(labelId)) {
        reindexEntry(entryId)
    }
}
