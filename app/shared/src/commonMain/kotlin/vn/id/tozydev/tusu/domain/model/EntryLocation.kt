package vn.id.tozydev.tusu.domain.model

/** Where an entry happened, stored on the entry itself so it reads offline. */
data class EntryLocation(
    val name: String,
    val address: String? = null,
    /** Null for a free-typed name such as "Nhà ngoại". */
    val coordinates: GeoPoint? = null,
    /** Small map image saved for offline viewing; null until it has been downloaded. */
    val mapSnapshotPath: String? = null,
)
