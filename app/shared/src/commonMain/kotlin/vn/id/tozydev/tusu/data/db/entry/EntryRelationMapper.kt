package vn.id.tozydev.tusu.data.db.entry

import vn.id.tozydev.tusu.data.db.label.toDomain
import vn.id.tozydev.tusu.data.db.media.toDomain
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

/**
 * Drops soft-deleted media, label links and labels, and puts media in album order and labels in
 * display order.
 */
fun EntryWithRelations.toDomain(): Entry =
    entry.toDomain(
        media =
            media.filter { it.deletedAt == null }.sortedBy { it.position }.map { it.toDomain() },
        labels =
            labelLinks
                .filter { it.link.deletedAt == null }
                .mapNotNull { it.label }
                .filter { it.deletedAt == null }
                .sortedBy { it.sortOrder }
                .map { it.toDomain() },
    )

fun CalendarRow.toCalendarDay(): CalendarDay =
    CalendarDay(date = date, emoji = emoji, entryCount = entryCount)

fun LocationRow.toEntryLocation(): EntryLocation =
    EntryLocation(
        name = placeName,
        address = placeAddress,
        coordinates =
            if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
        mapSnapshotPath = mapSnapshotPath,
    )
