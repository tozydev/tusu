package vn.id.tozydev.tusu.data.db.media

import kotlin.time.Duration.Companion.milliseconds
import vn.id.tozydev.tusu.domain.model.Media

fun MediaEntity.toDomain(): Media =
    Media(
        id = id,
        entryId = entryId,
        type = type,
        localPath = localPath,
        remoteKey = remoteKey,
        width = width,
        height = height,
        duration = durationMs?.milliseconds,
        position = position,
        takenAt = takenAt,
        status = status,
        waveform = waveform,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun Media.toEntity(): MediaEntity =
    MediaEntity(
        id = id,
        entryId = entryId,
        type = type,
        localPath = localPath,
        remoteKey = remoteKey,
        width = width,
        height = height,
        durationMs = duration?.inWholeMilliseconds,
        position = position,
        takenAt = takenAt,
        status = status,
        waveform = waveform,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
