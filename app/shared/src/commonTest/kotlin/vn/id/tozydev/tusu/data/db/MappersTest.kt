package vn.id.tozydev.tusu.data.db

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.Tag

class MappersTest : ShouldSpec() {
    init {
        should("map TagEntity to Tag") {
            val id = Uuid.random()
            val entity = TagEntity(id = id, name = "Kotlin")
            val model = entity.toModel()
            model.id shouldBe id
            model.name shouldBe "Kotlin"
        }

        should("map Tag to TagEntity") {
            val id = Uuid.random()
            val model = Tag(id = id, name = "Compose")
            val entity = model.toEntity()
            entity.id shouldBe id
            entity.name shouldBe "Compose"
        }

        should("map MediaEntity to Media") {
            val id = Uuid.random()
            val entryId = Uuid.random()
            val entity =
                MediaEntity(
                    id = id,
                    entryId = entryId,
                    mimeType = "image/png",
                    filename = "photo.png",
                    path = "/path/to/photo.png",
                    order = 0.001f,
                    height = 1080,
                    width = 1920,
                )
            val model = entity.toModel()
            model.id shouldBe id
            model.mimeType shouldBe "image/png"
            model.filename shouldBe "photo.png"
            model.path shouldBe "/path/to/photo.png"
            model.order shouldBe 0.001f
            model.height shouldBe 1080
            model.width shouldBe 1920
        }

        should("map Media to MediaEntity") {
            val id = Uuid.random()
            val entryId = Uuid.random()
            val model =
                Media(
                    id = id,
                    mimeType = "video/mp4",
                    filename = "video.mp4",
                    path = "/path/to/video.mp4",
                    order = 0.001f,
                    height = 720,
                    width = 1280,
                )
            val entity = model.toEntity(entryId = entryId)
            entity.id shouldBe id
            entity.entryId shouldBe entryId
            entity.mimeType shouldBe "video/mp4"
            entity.filename shouldBe "video.mp4"
            entity.path shouldBe "/path/to/video.mp4"
            entity.order shouldBe 0.001f
            entity.height shouldBe 720
            entity.width shouldBe 1280
        }

        should("map EntryEntity to Entry") {
            val id = Uuid.random()
            val recordedAt = Instant.fromEpochMilliseconds(1718440000000L)
            val entity =
                EntryEntity(
                    id = id,
                    recordedAt = recordedAt,
                    content = "Hello world",
                    emoji = "👋",
                )
            val entryWithRelations =
                EntryWithRelations(
                    entry = entity,
                    tags = emptyList(),
                    media = emptyList(),
                )
            val model = entryWithRelations.toModel()
            model.id shouldBe id
            model.recordedAt shouldBe recordedAt
            model.content shouldBe "Hello world"
            model.emoji shouldBe "👋"
            model.tags shouldBe emptyList()
            model.media shouldBe emptyList()
        }

        should("map EntryWithRelations to Entry with tags and media") {
            val entryId = Uuid.random()
            val recordedAt = Instant.fromEpochMilliseconds(1718440000000L)
            val entryEntity =
                EntryEntity(
                    id = entryId,
                    recordedAt = recordedAt,
                    content = "Great day!",
                    emoji = "☀️",
                )
            val tagEntity = TagEntity(id = Uuid.random(), name = "Life")
            val mediaEntity =
                MediaEntity(
                    id = Uuid.random(),
                    entryId = entryId,
                    mimeType = "image/jpeg",
                    filename = "sunset.jpg",
                    path = "/path/to/sunset.jpg",
                    order = 1.2f,
                    height = 600,
                    width = 800,
                )

            val entryWithRelations =
                EntryWithRelations(
                    entry = entryEntity,
                    tags = listOf(tagEntity),
                    media = listOf(mediaEntity),
                )

            val model = entryWithRelations.toModel()
            model.id shouldBe entryId
            model.recordedAt shouldBe recordedAt
            model.content shouldBe "Great day!"
            model.emoji shouldBe "☀️"
            model.tags.size shouldBe 1
            model.tags[0].id shouldBe tagEntity.id
            model.tags[0].name shouldBe "Life"
            model.media.size shouldBe 1
            model.media[0].id shouldBe mediaEntity.id
            model.media[0].mimeType shouldBe "image/jpeg"
        }

        should("map Entry to EntryEntity") {
            val id = Uuid.random()
            val recordedAt = Instant.fromEpochMilliseconds(1718440000000L)
            val model =
                Entry(
                    id = id,
                    recordedAt = recordedAt,
                    content = "Draft",
                    emoji = null,
                    tags = listOf(Tag(id = Uuid.random(), name = "Draft")),
                    media =
                        listOf(
                            Media(
                                id = Uuid.random(),
                                mimeType = "image/png",
                                filename = "draft.png",
                                path = "/draft.png",
                                order = 1f,
                                height = null,
                                width = null,
                            )
                        ),
                )
            val entity = model.toEntity()
            entity.id shouldBe id
            entity.recordedAt shouldBe recordedAt
            entity.content shouldBe "Draft"
            entity.emoji shouldBe null
        }
    }
}
