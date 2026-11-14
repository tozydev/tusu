package vn.id.tozydev.tusu.domain.model

/** What the composer does with the current location when it opens. */
enum class LocationMode {
    /** Attach it to the new entry. */
    ALWAYS,
    /** Offer it without attaching. */
    SUGGEST,
    OFF,
}
