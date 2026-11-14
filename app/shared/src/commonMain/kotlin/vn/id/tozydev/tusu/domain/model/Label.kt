package vn.id.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Label(
    val id: Uuid,
    /**
     * Null puts the label in the built-in "Khác" (Other) group; its colour comes from the group.
     */
    val groupId: Uuid? = null,
    val name: String,
    val isHidden: Boolean = false,
    /** Seeded on first run rather than created by the user. */
    val isDefault: Boolean = false,
    val sortOrder: Int,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
