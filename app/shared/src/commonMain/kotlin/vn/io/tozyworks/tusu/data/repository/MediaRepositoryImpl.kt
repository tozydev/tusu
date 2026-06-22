package vn.io.tozyworks.tusu.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.resolve
import kotlin.uuid.Uuid
import vn.io.tozyworks.tusu.data.db.MediaDao
import vn.io.tozyworks.tusu.di.AppDir
import vn.io.tozyworks.tusu.domain.repository.MediaRepository

@Inject
@ContributesBinding(AppScope::class, binding<MediaRepository>())
class MediaRepositoryImpl(
    private val mediaDao: MediaDao,
    @AppDir private val appDir: PlatformFile,
) : MediaRepository {
    override suspend fun deleteMedia(mediaId: Uuid) {
        val entity = mediaDao.deleteAndReturning(mediaId)
        if (entity != null) {
            appDir.resolve(entity.path).delete()
        }
    }

    private companion object {
        const val MEDIA_DIR = "media"
    }
}
