package vn.io.tozyworks.tusu.data.db

import kotlin.uuid.Uuid
import vn.io.tozyworks.tusu.model.Entry
import vn.io.tozyworks.tusu.model.Media
import vn.io.tozyworks.tusu.model.Tag

fun TagEntity.toModel() =
    Tag(
        id = id,
        name = name,
    )

fun Tag.toEntity() =
    TagEntity(
        id = id,
        name = name,
    )

fun MediaEntity.toModel() =
    Media(
        id = id,
        mimeType = mimeType,
        filename = filename,
        path = path,
        height = height,
        width = width,
    )

fun Media.toEntity(entryId: Uuid) =
    MediaEntity(
        id = id,
        entryId = entryId,
        mimeType = mimeType,
        filename = filename,
        path = path,
        height = height,
        width = width,
    )

fun EntryWithRelations.toModel() =
    Entry(
        id = entry.id,
        recordedAt = entry.recordedAt,
        content = entry.content,
        emoji = entry.emoji,
        tags = tags.map { it.toModel() },
        media = media.map { it.toModel() },
    )

fun Entry.toEntity() =
    EntryEntity(
        id = id,
        recordedAt = recordedAt,
        content = content,
        emoji = emoji,
    )
