package vn.id.tozydev.tusu.platform

import dev.jordond.compass.Coordinates
import dev.jordond.compass.Place
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import vn.id.tozydev.tusu.domain.model.GeoPoint

private val point = GeoPoint(10.78756, 106.67891)

private fun place(
    name: String? = null,
    street: String? = null,
    thoroughfare: String? = null,
    subThoroughfare: String? = null,
    subLocality: String? = null,
    locality: String? = null,
    subAdministrativeArea: String? = null,
    administrativeArea: String? = null,
    country: String? = null,
) =
    Place(
        coordinates = Coordinates(point.latitude, point.longitude),
        name = name,
        street = street,
        isoCountryCode = null,
        country = country,
        postalCode = null,
        administrativeArea = administrativeArea,
        subAdministrativeArea = subAdministrativeArea,
        locality = locality,
        subLocality = subLocality,
        thoroughfare = thoroughfare,
        subThoroughfare = subThoroughfare,
    )

class PlaceNamingTest :
    FunSpec(
        {
            test("a named place keeps its name and uses the address line as the address") {
                val location =
                    place(
                            name = "Cà phê Lê Văn Sỹ",
                            street = "123 Lê Văn Sỹ, Phường 13, Quận 3, Hồ Chí Minh",
                            thoroughfare = "Lê Văn Sỹ",
                            subThoroughfare = "123",
                        )
                        .toEntryLocation(point)

                location.name shouldBe "Cà phê Lê Văn Sỹ"
                location.address shouldBe "123 Lê Văn Sỹ, Phường 13, Quận 3, Hồ Chí Minh"
                location.coordinates shouldBe point
            }

            test("a house number is not a name, so the street is used") {
                val location =
                    place(
                            name = "123",
                            street = "123 Lê Văn Sỹ, Phường 13, Quận 3",
                            thoroughfare = "Lê Văn Sỹ",
                            subThoroughfare = "123",
                        )
                        .toEntryLocation(point)

                location.name shouldBe "123 Lê Văn Sỹ"
            }

            test(
                "without a street the smallest area names the place, and the larger ones the address",
            ) {
                val location =
                    place(
                            subLocality = "Phường 13",
                            locality = "Quận 3",
                            administrativeArea = "Hồ Chí Minh",
                            country = "Việt Nam",
                        )
                        .toEntryLocation(point)

                location.name shouldBe "Phường 13"
                location.address shouldBe "Quận 3, Hồ Chí Minh, Việt Nam"
            }

            test("an address equal to the name is dropped") {
                place(name = "Hồ Gươm", street = "Hồ Gươm")
                    .toEntryLocation(point)
                    .address
                    .shouldBeNull()
            }

            test("an empty place is named by its coordinates") {
                val location = place().toEntryLocation(point)

                location.name shouldBe "10.78756, 106.67891"
                location.address.shouldBeNull()
            }
        },
    )
