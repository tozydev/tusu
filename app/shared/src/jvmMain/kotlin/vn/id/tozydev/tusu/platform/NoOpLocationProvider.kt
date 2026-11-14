package vn.id.tozydev.tusu.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

/** JVM is a build and test target only, so no location is ever available. */
@ContributesBinding(AppScope::class)
@Inject
class NoOpLocationProvider : LocationProvider {
    override suspend fun currentLocation(): Outcome<GeoPoint, LocationError> =
        Outcome.Failure(LocationError.Unavailable)

    override suspend fun reverseGeocode(point: GeoPoint): Outcome<EntryLocation, LocationError> =
        Outcome.Failure(LocationError.Unavailable)
}
