package vn.io.tozyworks.tusu.domain.repository

import io.github.vinceglb.filekit.PlatformFile
import kotlin.uuid.Uuid
import vn.io.tozyworks.tusu.domain.model.Media

interface MediaRepository {
    suspend fun addMedia(entryId: Uuid, files: List<PlatformFile>): List<Media>

    suspend fun deleteMedia(mediaId: Uuid)
}
