package vn.id.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A location the user saved for quick picking, such as Home or Work. */
data class Place(
    val id: Uuid,
    val name: String,
    val address: String? = null,
    val coordinates: GeoPoint? = null,
    val sortOrder: Int,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
