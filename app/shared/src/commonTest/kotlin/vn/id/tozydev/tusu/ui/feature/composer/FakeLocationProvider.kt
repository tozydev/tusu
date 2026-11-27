package vn.id.tozydev.tusu.ui.feature.composer

import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.platform.LocationProvider

/** Always at [point] unless a test sets a failure; geocodes to [place]. */
class FakeLocationProvider : LocationProvider {
    val point = GeoPoint(10.78756, 106.67891)
    var place = EntryLocation(name = "Cà phê Lê Văn Sỹ", address = "Quận 3", coordinates = point)

    var fixResult: Outcome<GeoPoint, LocationError> = Outcome.Success(point)
    var geocodeResult: Outcome<EntryLocation, LocationError>? = null
    var fixes = 0
        private set

    override suspend fun currentLocation(): Outcome<GeoPoint, LocationError> {
        fixes++
        return fixResult
    }

    override suspend fun reverseGeocode(point: GeoPoint): Outcome<EntryLocation, LocationError> =
        geocodeResult ?: Outcome.Success(place)
}
