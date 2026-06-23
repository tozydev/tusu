package vn.io.tozydev.tusu.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.resolve
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import vn.io.tozydev.tusu.data.db.MediaDao
import vn.io.tozydev.tusu.data.db.MediaEntity
import vn.io.tozydev.tusu.data.db.toModel
import vn.io.tozydev.tusu.data.util.getImageDimension
import vn.io.tozydev.tusu.di.AppDir
import vn.io.tozydev.tusu.domain.model.Media
import vn.io.tozydev.tusu.domain.repository.MediaRepository

@Inject
@ContributesBinding(AppScope::class, binding<MediaRepository>())
class MediaRepositoryImpl(
    private val mediaDao: MediaDao,
    @AppDir private val appDir: PlatformFile,
) : MediaRepository {

    // todo proper image validation
    override suspend fun addMedia(
        entryId: Uuid,
        files: List<PlatformFile>,
    ): List<Media> {
        return files
            .map { file ->
                runCatching {
                    createEntity(entryId, file).also {
                        file.copyToAppDir(it.path)
                    }
                }
            }
            .mapNotNull { result ->
                // todo proper error handling
                result
                    .onFailure {
                        logger.error(it) { "Error when adding media" }
                    }
                    .getOrNull()
            }
            .also {
                mediaDao.insertAll(it)
            }
            .map { it.toModel() }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createEntity(entryId: Uuid, file: PlatformFile): MediaEntity {
        val id = Uuid.generateV7()
        val mimeType =
            requireNotNull(file.mimeType()) {
                "Cannot determine mime type for file: $file"
            }
        val extension = file.extension
        val dimension = getImageDimension(file)

        return MediaEntity(
            id = id,
            entryId = entryId,
            mimeType = mimeType.toString(),
            filename = file.name,
            path = resolveMediaRelativePath(id, extension),
            order = 0f,
            width = dimension.width,
            height = dimension.height,
        )
    }

    private suspend fun PlatformFile.copyToAppDir(destPath: String) {
        val dest = appDir / destPath
        dest.parent()?.createDirectories()
        copyTo(dest)
    }

    private fun resolveMediaRelativePath(id: Uuid, extension: String): String {
        val idStr = id.toString()
        val seg1 = idStr.substring(0, 2)
        val seg2 = idStr.substring(2, 4)
        return "./$MEDIA_DIR/$seg1/$seg2/$idStr.$extension"
    }

    override suspend fun deleteMedia(mediaId: Uuid) {
        val entity = mediaDao.deleteAndReturning(mediaId)
        if (entity != null) {
            appDir.resolve(entity.path).delete()
        }
    }

    private companion object {
        const val MEDIA_DIR = "media"
        val logger = KotlinLogging.logger {}
    }
}
