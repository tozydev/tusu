package vn.id.tozydev.tusu.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.GeocoderResult
import dev.jordond.compass.geocoder.mobile
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

private val logger = KotlinLogging.logger {}

/**
 * Fused location and the Android geocoder through Compass. The permission is checked here first, so
 * Compass never shows its own prompt; the app asks through its own requester.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class CompassLocationProvider(private val context: Context) : LocationProvider {
    private val geolocator by lazy { Geolocator.mobile() }
    private val geocoder by lazy { Geocoder.mobile() }

    override suspend fun currentLocation(): Outcome<GeoPoint, LocationError> {
        if (!hasPermission()) {
            return Outcome.Failure(
                LocationError.PermissionRequired(PermissionError.Denied(AppPermission.LOCATION))
            )
        }
        if (!geolocator.isAvailable()) return Outcome.Failure(LocationError.ServiceDisabled)
        // A place name needs a precise fix when the user allowed one. Asking Compass for more than
        // was granted would make it prompt on its own, so approximate access stays Balanced.
        val priority = if (isGranted(FINE)) Priority.HighAccuracy else Priority.Balanced
        return when (val result = geolocator.current(priority)) {
            is GeolocatorResult.Success ->
                Outcome.Success(
                    GeoPoint(result.data.coordinates.latitude, result.data.coordinates.longitude)
                )
            is GeolocatorResult.PermissionDenied ->
                Outcome.Failure(
                    LocationError.PermissionRequired(
                        if (result.forever) PermissionError.DeniedPermanently(AppPermission.LOCATION)
                        else PermissionError.Denied(AppPermission.LOCATION)
                    )
                )
            is GeolocatorResult.Error -> {
                logger.warn { "No location fix: ${result.message}" }
                Outcome.Failure(LocationError.Unavailable)
            }
        }
    }

    override suspend fun reverseGeocode(point: GeoPoint): Outcome<EntryLocation, LocationError> =
        when (val result = geocoder.reverse(point.latitude, point.longitude)) {
            is GeocoderResult.Success -> {
                val place = result.data.firstOrNull { !it.isEmpty }
                if (place == null) Outcome.Failure(LocationError.Unavailable)
                else Outcome.Success(place.toEntryLocation(point))
            }
            is GeocoderResult.Error -> {
                logger.warn { "Reverse geocoding failed: $result" }
                Outcome.Failure(LocationError.Unavailable)
            }
        }

    private fun hasPermission(): Boolean = isGranted(FINE) || isGranted(COARSE)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}
