package vn.id.tozydev.tusu.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.resolve
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import vn.id.tozydev.tusu.data.db.EntryDao
import vn.id.tozydev.tusu.data.db.EntryEntity
import vn.id.tozydev.tusu.data.db.EntryTagCrossRef
import vn.id.tozydev.tusu.data.db.EntryTagDao
import vn.id.tozydev.tusu.data.db.MediaDao
import vn.id.tozydev.tusu.data.db.MediaEntity
import vn.id.tozydev.tusu.data.db.TagDao
import vn.id.tozydev.tusu.data.db.TagEntity
import vn.id.tozydev.tusu.data.model.BackupPayload
import vn.id.tozydev.tusu.data.util.readBackupZip
import vn.id.tozydev.tusu.data.util.writeBackupZip
import vn.id.tozydev.tusu.di.AppDir
import vn.id.tozydev.tusu.domain.repository.BackupRepository

@Inject
@ContributesBinding(AppScope::class, binding = binding<BackupRepository>())
class BackupRepositoryImpl(
    private val entryDao: EntryDao,
    private val tagDao: TagDao,
    private val mediaDao: MediaDao,
    private val entryTagDao: EntryTagDao,
    @AppDir private val appDir: PlatformFile,
) : BackupRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun exportBackup(destFile: PlatformFile) =
        withContext(Dispatchers.IO) {
            val entries =
                entryDao.getAll().map {
                    BackupPayload.Entry(
                        id = it.id.toString(),
                        recordedAt = it.recordedAt.toEpochMilliseconds(),
                        content = it.content,
                        emoji = it.emoji,
                    )
                }
            val tags =
                tagDao.getAll().map {
                    BackupPayload.Tag(
                        id = it.id.toString(),
                        name = it.name,
                    )
                }
            val entryTags =
                entryTagDao.getAll().map {
                    BackupPayload.EntryTag(
                        entryId = it.entryId.toString(),
                        tagId = it.tagId.toString(),
                    )
                }
            val media =
                mediaDao.getAll().map {
                    BackupPayload.Media(
                        id = it.id.toString(),
                        entryId = it.entryId.toString(),
                        mimeType = it.mimeType,
                        filename = it.filename,
                        path = it.path,
                        order = it.order,
                        height = it.height,
                        width = it.width,
                    )
                }

            val payload =
                BackupPayload(
                    entries = entries,
                    tags = tags,
                    entryTags = entryTags,
                    media = media,
                )

            val jsonContent = Json.encodeToString(BackupPayload.serializer(), payload)

            val mediaFiles =
                media
                    .map { m ->
                        m.path to appDir.resolve(m.path)
                    }
                    .filter { it.second.exists() }

            writeBackupZip(destFile, jsonContent, mediaFiles)
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun importBackup(srcFile: PlatformFile) =
        withContext(Dispatchers.IO) {
            val oldMedia = mediaDao.getAll()

            val dataJson = readBackupZip(srcFile, appDir)
            val payload = Json.decodeFromString(BackupPayload.serializer(), dataJson)

            val entryEntities =
                payload.entries.map {
                    EntryEntity(
                        id = Uuid.parse(it.id),
                        recordedAt = Instant.fromEpochMilliseconds(it.recordedAt),
                        content = it.content,
                        emoji = it.emoji,
                    )
                }
            val tagEntities =
                payload.tags.map {
                    TagEntity(
                        id = Uuid.parse(it.id),
                        name = it.name,
                    )
                }
            val entryTagCrossRefs =
                payload.entryTags.map {
                    EntryTagCrossRef(
                        entryId = Uuid.parse(it.entryId),
                        tagId = Uuid.parse(it.tagId),
                    )
                }
            val mediaEntities =
                payload.media.map {
                    MediaEntity(
                        id = Uuid.parse(it.id),
                        entryId = Uuid.parse(it.entryId),
                        mimeType = it.mimeType,
                        filename = it.filename,
                        path = it.path,
                        order = it.order,
                        height = it.height,
                        width = it.width,
                    )
                }

            entryDao.restoreData(
                entries = entryEntities,
                tags = tagEntities,
                entryTags = entryTagCrossRefs,
                media = mediaEntities,
                tagDao = tagDao,
                entryTagDao = entryTagDao,
                mediaDao = mediaDao,
            )

            val newPaths = mediaEntities.map { it.path }.toSet()
            for (m in oldMedia) {
                if (m.path !in newPaths) {
                    runCatching { appDir.resolve(m.path).delete() }
                }
            }
        }
}
