package vn.io.tozyworks.tusu.domain.repository

import kotlin.uuid.Uuid

interface MediaRepository {
    suspend fun deleteMedia(mediaId: Uuid)
}
