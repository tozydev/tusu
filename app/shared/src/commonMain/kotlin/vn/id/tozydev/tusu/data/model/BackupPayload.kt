package vn.id.tozydev.tusu.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val entries: List<Entry>,
    val tags: List<Tag>,
    val entryTags: List<EntryTag>,
    val media: List<Media>,
) {
    @Serializable
    data class Entry(
        val id: String,
        val recordedAt: Long,
        val content: String,
        val emoji: String?,
    )

    @Serializable
    data class Tag(
        val id: String,
        val name: String,
    )

    @Serializable
    data class EntryTag(
        val entryId: String,
        val tagId: String,
    )

    @Serializable
    data class Media(
        val id: String,
        val entryId: String,
        val mimeType: String,
        val filename: String,
        val path: String,
        val order: Float,
        val height: Int?,
        val width: Int?,
    )
}
