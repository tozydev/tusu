package vn.id.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class LabelGroup(
    val id: Uuid,
    val name: String,
    /** ARGB colour shared by every label in the group. */
    val color: Int,
    val selection: LabelSelection = LabelSelection.MULTI,
    val showInComposer: Boolean = true,
    /** Seeded on first run rather than created by the user. */
    val isDefault: Boolean = false,
    val sortOrder: Int,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
