package vn.id.tozydev.tusu.domain.usecase

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.data.repository.FakeMediaFileStore
import vn.id.tozydev.tusu.data.repository.MediaRepositoryImpl
import vn.id.tozydev.tusu.data.repository.TestClock
import vn.id.tozydev.tusu.data.repository.entryRow
import vn.id.tozydev.tusu.data.repository.instantAt
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private fun bytes(text: String): RawSource = Buffer().also { it.write(text.encodeToByteArray()) }

class AttachMediaUseCaseTest :
    FunSpec(
        {
            test("first photo is stored and added as PROCESSING at position 0") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    db.entryDao().upsert(entryRow(entryId))
                    val files = FakeMediaFileStore()
                    val clock = TestClock(instantAt(8))
                    val repo = MediaRepositoryImpl(db, files, clock)
                    val useCase = AttachMediaUseCase(files, repo, clock)

                    val result =
                        useCase(entryId, bytes("jpeg"), MediaType.PHOTO, "JPG", instantAt(2))

                    val media = result.shouldBeInstanceOf<Outcome.Success<Media>>().value
                    media.entryId shouldBe entryId
                    media.type shouldBe MediaType.PHOTO
                    media.status shouldBe MediaStatus.PROCESSING
                    media.position shouldBe 0
                    media.takenAt shouldBe instantAt(2)
                    files.files[media.localPath]?.decodeToString() shouldBe "jpeg"
                    repo.observeEntryMedia(entryId).first() shouldBe listOf(media)
                }
            }

            test("audio is READY and goes after the existing media") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    db.entryDao().upsert(entryRow(entryId))
                    val files = FakeMediaFileStore()
                    val clock = TestClock(instantAt(8))
                    val repo = MediaRepositoryImpl(db, files, clock)
                    val useCase = AttachMediaUseCase(files, repo, clock)
                    useCase(entryId, bytes("jpeg"), MediaType.PHOTO, "jpg")

                    val result = useCase(entryId, bytes("voice"), MediaType.AUDIO, "m4a")

                    val media = result.shouldBeInstanceOf<Outcome.Success<Media>>().value
                    media.status shouldBe MediaStatus.READY
                    media.position shouldBe 1
                    media.localPath.endsWith(".m4a") shouldBe true
                    files.files.size shouldBe 2
                    repo.observeEntryMedia(entryId).first().map { it.position } shouldContainExactly
                        listOf(0, 1)
                }
            }

            test("attaching to a missing entry fails with NotFound and leaves no file behind") {
                withTestDatabase { db ->
                    val files = FakeMediaFileStore()
                    val clock = TestClock(instantAt(8))
                    val repo = MediaRepositoryImpl(db, files, clock)
                    val useCase = AttachMediaUseCase(files, repo, clock)

                    useCase(Uuid.random(), bytes("jpeg"), MediaType.PHOTO, "jpg") shouldBe
                        Outcome.Failure(MediaError.NotFound)

                    files.files.keys.shouldBeEmpty()
                    files.deleted.size shouldBe 1
                }
            }
        },
    )
