package vn.id.tozydev.tusu.platform

import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

interface LocationProvider {
    suspend fun currentLocation(): Outcome<GeoPoint, LocationError>

    suspend fun reverseGeocode(point: GeoPoint): Outcome<EntryLocation, LocationError>
}
