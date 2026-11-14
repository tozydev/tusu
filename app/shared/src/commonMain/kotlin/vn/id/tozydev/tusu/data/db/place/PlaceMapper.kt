package vn.id.tozydev.tusu.data.db.place

import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.domain.model.Place

fun PlaceEntity.toDomain(): Place =
    Place(
        id = id,
        name = name,
        address = address,
        coordinates =
            if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun Place.toEntity(): PlaceEntity =
    PlaceEntity(
        id = id,
        name = name,
        address = address,
        latitude = coordinates?.latitude,
        longitude = coordinates?.longitude,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
