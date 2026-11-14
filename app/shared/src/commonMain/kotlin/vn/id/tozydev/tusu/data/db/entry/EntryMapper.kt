package vn.id.tozydev.tusu.data.db.entry

import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.Media

/** Media and labels are loaded by their own DAOs, so the caller passes them in. */
fun EntryEntity.toDomain(media: List<Media>, labels: List<Label>): Entry =
    Entry(
        id = id,
        body = body,
        textPlain = textPlain,
        emoji = emoji,
        occurredAt = occurredAt,
        timeZone = TimeZone.of(timezone),
        location = toLocation(),
        media = media,
        labels = labels,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

/**
 * [textSearch] is derived from the body by the repository, which is why the domain has no field.
 */
fun Entry.toEntity(textSearch: String): EntryEntity =
    EntryEntity(
        id = id,
        body = body,
        textPlain = textPlain,
        textSearch = textSearch,
        emoji = emoji,
        occurredAt = occurredAt,
        timezone = timeZone.id,
        localDate = localDate,
        placeName = location?.name,
        placeAddress = location?.address,
        latitude = location?.coordinates?.latitude,
        longitude = location?.coordinates?.longitude,
        mapSnapshotPath = location?.mapSnapshotPath,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

/** A location exists only when it has a name; coordinates are optional for free-typed places. */
private fun EntryEntity.toLocation(): EntryLocation? {
    val name = placeName ?: return null
    val lat = latitude
    val lon = longitude
    return EntryLocation(
        name = name,
        address = placeAddress,
        coordinates = if (lat != null && lon != null) GeoPoint(lat, lon) else null,
        mapSnapshotPath = mapSnapshotPath,
    )
}
