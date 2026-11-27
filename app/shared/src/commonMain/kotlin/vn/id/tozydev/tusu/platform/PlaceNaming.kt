package vn.id.tozydev.tusu.platform

import dev.jordond.compass.Place
import kotlin.math.abs
import kotlin.math.roundToLong
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

private const val COORDINATE_DECIMALS = 5

/**
 * The short name and the address line of a reverse-geocoded [Place]. Geocoders often report only a
 * house number as the name, so a name made of digits gives way to the street.
 */
fun Place.toEntryLocation(point: GeoPoint): EntryLocation {
    val ownName = name?.trim()?.takeIf { it.isNotEmpty() && it.any(Char::isLetter) }
    val streetName =
        thoroughfare
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { road ->
                listOfNotNull(subThoroughfare?.trim()?.takeIf { it.isNotEmpty() }, road)
                    .joinToString(" ")
            }
    val areas =
        listOf(subLocality, locality, subAdministrativeArea, administrativeArea, country)
            .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            .distinct()
    val placeName = ownName ?: streetName ?: areas.firstOrNull() ?: point.label()
    val addressLine =
        street?.trim()?.takeIf { it.isNotEmpty() }
            ?: areas.filter { it != placeName }.joinToString(", ").takeIf { it.isNotEmpty() }
    return EntryLocation(
        name = placeName,
        address = addressLine?.takeIf { it != placeName },
        coordinates = point,
    )
}

/** Coordinates as `lat, lon` with five decimals (about one metre). */
fun GeoPoint.label(): String = "${latitude.decimals()}, ${longitude.decimals()}"

private fun Double.decimals(): String {
    var scale = 1L
    repeat(COORDINATE_DECIMALS) { scale *= 10 }
    val scaled = (abs(this) * scale).roundToLong()
    val whole = scaled / scale
    val fraction = (scaled % scale).toString().padStart(COORDINATE_DECIMALS, '0')
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    return "$sign$whole.$fraction"
}
