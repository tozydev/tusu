package vn.id.tozydev.tusu.domain.model

import kotlin.uuid.Uuid

data class Media(
    val id: Uuid,
    val mimeType: String,
    val filename: String,
    val path: String,
    val order: Float,
    val height: Int?,
    val width: Int?,
)
