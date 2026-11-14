package vn.id.tozydev.tusu.domain.repository

import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.PlaceError
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.domain.model.Place

interface PlaceRepository {
    /** Saved places in display order. */
    fun observePlaces(): Flow<List<Place>>

    suspend fun create(
        name: String,
        address: String?,
        coordinates: GeoPoint?,
    ): Outcome<Place, PlaceError>

    suspend fun update(place: Place): Outcome<Unit, PlaceError>

    suspend fun delete(id: Uuid): Outcome<Unit, PlaceError>

    suspend fun reorder(orderedIds: List<Uuid>): Outcome<Unit, PlaceError>
}
