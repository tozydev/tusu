package vn.io.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Entry(
    val id: Uuid,
    val recordedAt: Instant,
    val content: String,
    val emoji: String?,
    val tags: List<Tag> = emptyList(),
    val media: List<Media> = emptyList(),
)
