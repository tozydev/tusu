package vn.id.tozydev.tusu.data.repository

import androidx.paging.testing.asSnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.data.db.media.toDomain
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore

/** In-memory file store; the real folder is covered by MediaStoreTest. */
internal class FakeMediaFileStore : MediaFileStore {
    val files = mutableMapOf<String, ByteArray>()
    val deleted = mutableListOf<String>()

    /** When set, [delete] fails with it and keeps the file. */
    var deleteError: MediaError? = null

    override fun newRelativePath(id: Uuid, extension: String): String = "$id.$extension"

    override fun resolve(relativePath: String): Path = Path("fake-media", relativePath)

    override suspend fun write(
        relativePath: String,
        source: RawSource,
    ): Outcome<Unit, MediaError> {
        files[relativePath] = source.use { it.buffered().readByteArray() }
        return Outcome.Success(Unit)
    }

    override suspend fun delete(relativePath: String): Outcome<Unit, MediaError> {
        deleteError?.let {
            return Outcome.Failure(it)
        }
        files.remove(relativePath)
        deleted += relativePath
        return Outcome.Success(Unit)
    }

    override suspend fun exists(relativePath: String): Boolean = relativePath in files
}

internal val t0: Instant = Instant.parse("2026-11-01T00:00:00Z")

internal fun instantAt(day: Int): Instant = Instant.parse("2026-11-%02dT00:00:00Z".format(day))

internal fun entryRow(id: Uuid, deletedAt: Instant? = null) =
    EntryEntity(
        id = id,
        body = """{"blocks":[]}""",
        textPlain = "",
        textSearch = "",
        emoji = null,
        occurredAt = t0,
        timezone = "UTC",
        localDate = LocalDate(2026, 11, 1),
        placeName = null,
        placeAddress = null,
        latitude = null,
        longitude = null,
        mapSnapshotPath = null,
        createdAt = t0,
        updatedAt = t0,
        deletedAt = deletedAt,
    )

internal fun mediaRow(
    id: Uuid,
    entryId: Uuid,
    position: Int = 0,
    type: MediaType = MediaType.PHOTO,
    status: MediaStatus = MediaStatus.READY,
    takenAt: Instant? = null,
    updatedAt: Instant = t0,
    deletedAt: Instant? = null,
    localPath: String = "$id.jpg",
) =
    MediaEntity(
        id = id,
        entryId = entryId,
        type = type,
        localPath = localPath,
        remoteKey = null,
        width = null,
        height = null,
        durationMs = null,
        position = position,
        takenAt = takenAt,
        status = status,
        waveform = emptyList(),
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

private val entryA = Uuid.parse("00000000-0000-0000-0000-00000000000a")
private val entryB = Uuid.parse("00000000-0000-0000-0000-00000000000b")
private val idA = Uuid.parse("00000000-0000-0000-0000-000000000001")
private val idB = Uuid.parse("00000000-0000-0000-0000-000000000002")
private val idC = Uuid.parse("00000000-0000-0000-0000-000000000003")
private val idD = Uuid.parse("00000000-0000-0000-0000-000000000004")

class MediaRepositoryImplTest :
    FunSpec(
        {
            test(
                "observeEntryMedia lists live items of the entry by position, whatever their status",
            ) {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.entryDao().upsert(entryRow(entryB))
                    db.mediaDao()
                        .upsert(
                            mediaRow(idA, entryA, position = 1, status = MediaStatus.PROCESSING),
                        )
                    db.mediaDao()
                        .upsert(mediaRow(idB, entryA, position = 0, status = MediaStatus.FAILED))
                    db.mediaDao().upsert(mediaRow(idC, entryA, position = 2, deletedAt = t0))
                    db.mediaDao().upsert(mediaRow(idD, entryB, position = 0))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idB, idA)
                }
            }

            test("observeLibrary shows only ready media of live entries, filtered by type") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.entryDao().upsert(entryRow(entryB, deletedAt = t0))
                    db.mediaDao().upsert(mediaRow(idA, entryA, takenAt = instantAt(3)))
                    db.mediaDao()
                        .upsert(
                            mediaRow(idB, entryA, type = MediaType.VIDEO, updatedAt = instantAt(2)),
                        )
                    db.mediaDao().upsert(mediaRow(idC, entryA, status = MediaStatus.PROCESSING))
                    db.mediaDao().upsert(mediaRow(idD, entryA, deletedAt = t0))
                    val other = Uuid.parse("00000000-0000-0000-0000-0000000000ff")
                    db.mediaDao().upsert(mediaRow(other, entryB))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.observeLibrary(null).asSnapshot().map { it.id } shouldContainExactly
                        listOf(idA, idB)
                    repo.observeLibrary(MediaType.PHOTO).asSnapshot().map {
                        it.id
                    } shouldContainExactly listOf(idA)
                }
            }

            test(
                "observeLibrary orders by taken_at, then updated_at, newest first, id breaking ties",
            ) {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    // Taken on day 3, so idA, idC and idD tie on taken_at; idB has no taken_at and
                    // falls back to updated_at on day 5, which is newer than all three.
                    db.mediaDao().upsert(mediaRow(idA, entryA, takenAt = instantAt(3)))
                    db.mediaDao().upsert(mediaRow(idC, entryA, takenAt = instantAt(3)))
                    db.mediaDao().upsert(mediaRow(idD, entryA, takenAt = instantAt(3)))
                    db.mediaDao().upsert(mediaRow(idB, entryA, updatedAt = instantAt(5)))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.observeLibrary(null).asSnapshot().map { it.id } shouldContainExactly
                        listOf(idB, idD, idC, idA)
                }
            }

            test("getByStatus returns live media with that status only") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, status = MediaStatus.PROCESSING))
                    db.mediaDao().upsert(mediaRow(idB, entryA, status = MediaStatus.READY))
                    db.mediaDao()
                        .upsert(
                            mediaRow(idC, entryA, status = MediaStatus.PROCESSING, deletedAt = t0),
                        )
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.getByStatus(MediaStatus.PROCESSING).map { it.id } shouldContainExactly
                        listOf(idA)
                }
            }

            test("add inserts the row with updatedAt set to now") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    val clock = TestClock(instantAt(9))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), clock)
                    val media = mediaRow(idA, entryA).toDomain().copy(updatedAt = t0)

                    repo.add(media) shouldBe Outcome.Success(media.copy(updatedAt = instantAt(9)))

                    db.mediaDao().getById(idA)?.updatedAt shouldBe instantAt(9)
                }
            }

            test("add moves to the end of the album when the position is already taken") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, position = 0))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))
                    val idNew = Uuid.random()

                    val stored = repo.add(mediaRow(idNew, entryA, position = 0).toDomain())

                    (stored as? Outcome.Success)?.value?.position shouldBe 1
                    db.mediaDao().getById(idNew)?.position shouldBe 1
                }
            }

            test("add to an entry that does not exist is NotFound and writes nothing") {
                withTestDatabase { db ->
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.add(mediaRow(idA, entryA).toDomain()) shouldBe
                        Outcome.Failure(MediaError.NotFound)

                    db.mediaDao().getById(idA) shouldBe null
                }
            }

            test("add with an id that is already stored is StorageFailed") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.add(mediaRow(idA, entryA).toDomain()) shouldBe
                        Outcome.Failure(MediaError.StorageFailed)
                }
            }

            test("setStatus changes the status and bumps updated_at") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, status = MediaStatus.PROCESSING))
                    val repo =
                        MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(instantAt(4)))

                    repo.setStatus(idA, MediaStatus.READY) shouldBe Outcome.Success(Unit)

                    val row = db.mediaDao().getById(idA)
                    row?.status shouldBe MediaStatus.READY
                    row?.updatedAt shouldBe instantAt(4)
                }
            }

            test("setStatus on a missing id is NotFound") {
                withTestDatabase { db ->
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.setStatus(idA, MediaStatus.READY) shouldBe
                        Outcome.Failure(MediaError.NotFound)
                }
            }

            test("reorder sets positions 0..n-1 in the given order") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, position = 0))
                    db.mediaDao().upsert(mediaRow(idB, entryA, position = 1))
                    db.mediaDao().upsert(mediaRow(idC, entryA, position = 2))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.reorder(entryA, listOf(idC, idA, idB)) shouldBe Outcome.Success(Unit)

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idC, idA, idB)
                    db.mediaDao().getById(idC)?.position shouldBe 0
                }
            }

            test("reorder with a missing, extra or repeated id is NotFound and changes nothing") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, position = 0))
                    db.mediaDao().upsert(mediaRow(idB, entryA, position = 1))
                    db.mediaDao().upsert(mediaRow(idC, entryA, position = 2))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.reorder(entryA, listOf(idC, idA)) shouldBe
                        Outcome.Failure(MediaError.NotFound)
                    repo.reorder(entryA, listOf(idC, idA, idB, idD)) shouldBe
                        Outcome.Failure(MediaError.NotFound)
                    repo.reorder(entryA, listOf(idC, idA, idA)) shouldBe
                        Outcome.Failure(MediaError.NotFound)

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idA, idB, idC)
                }
            }

            test("remove hides the item and keeps its file; restore brings it back in place") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, position = 0))
                    db.mediaDao().upsert(mediaRow(idB, entryA, position = 1))
                    val files = FakeMediaFileStore()
                    val clock = TestClock(instantAt(6))
                    val repo = MediaRepositoryImpl(db, files, clock)

                    repo.remove(idA) shouldBe Outcome.Success(Unit)

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idB)
                    db.mediaDao().getById(idA)?.deletedAt shouldBe instantAt(6)
                    files.deleted.shouldBeEmpty()

                    clock.instant = instantAt(7)
                    repo.restore(idA) shouldBe Outcome.Success(Unit)

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idA, idB)
                    db.mediaDao().getById(idA)?.deletedAt shouldBe null
                    db.mediaDao().getById(idA)?.updatedAt shouldBe instantAt(7)
                }
            }

            test("restore appends the item at the end when its old position is taken") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, position = 0))
                    db.mediaDao().upsert(mediaRow(idB, entryA, position = 1))
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))
                    repo.remove(idA)
                    // Another item takes the freed position 0 while idA is in the trash.
                    db.mediaDao().upsert(mediaRow(idC, entryA, position = 0))

                    repo.restore(idA) shouldBe Outcome.Success(Unit)

                    repo.observeEntryMedia(entryA).first().map { it.id } shouldContainExactly
                        listOf(idC, idB, idA)
                }
            }

            test("remove and restore of a missing id are NotFound") {
                withTestDatabase { db ->
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.remove(idA) shouldBe Outcome.Failure(MediaError.NotFound)
                    repo.restore(idA) shouldBe Outcome.Failure(MediaError.NotFound)
                }
            }

            test("deletePermanently removes the row and its file") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA, localPath = "ab/cd/a.jpg"))
                    val files = FakeMediaFileStore()
                    files.files["ab/cd/a.jpg"] = byteArrayOf(1)
                    val repo = MediaRepositoryImpl(db, files, TestClock(t0))

                    repo.deletePermanently(idA) shouldBe Outcome.Success(Unit)

                    db.mediaDao().getById(idA) shouldBe null
                    files.files.containsKey("ab/cd/a.jpg") shouldBe false
                }
            }

            test("deletePermanently of a missing id is NotFound") {
                withTestDatabase { db ->
                    val repo = MediaRepositoryImpl(db, FakeMediaFileStore(), TestClock(t0))

                    repo.deletePermanently(idA) shouldBe Outcome.Failure(MediaError.NotFound)
                }
            }

            test("a file that cannot be deleted is reported, and the row is already gone") {
                withTestDatabase { db ->
                    db.entryDao().upsert(entryRow(entryA))
                    db.mediaDao().upsert(mediaRow(idA, entryA))
                    val files =
                        FakeMediaFileStore().apply { deleteError = MediaError.StorageFailed }
                    val repo = MediaRepositoryImpl(db, files, TestClock(t0))

                    repo.deletePermanently(idA) shouldBe Outcome.Failure(MediaError.StorageFailed)

                    db.mediaDao().getById(idA) shouldBe null
                }
            }
        },
    )
