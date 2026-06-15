package vn.io.tozyworks.tusu.model

import kotlin.uuid.Uuid

data class Media(
    val id: Uuid,
    val mimeType: String,
    val filename: String,
    val path: String,
    val height: Int?,
    val width: Int?,
)
