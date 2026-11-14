package vn.id.tozydev.tusu.domain.model

import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A photo, video or voice recording attached to an entry. */
data class Media(
    val id: Uuid,
    val entryId: Uuid,
    val type: MediaType,
    /** Path relative to the app's media folder. */
    val localPath: String,
    /** Key of the uploaded copy; null until sync (v1.1) has uploaded it. */
    val remoteKey: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Duration? = null,
    /** Order inside the entry's album. */
    val position: Int,
    val takenAt: Instant? = null,
    val status: MediaStatus,
    /** Downsampled amplitudes of a voice recording, each in 0..1; empty for other types. */
    val waveform: List<Float> = emptyList(),
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)
