package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore

/** A media item ready to render: [path] is absolute, so Coil can load it directly. */
@Immutable
data class MediaUi(
    val id: Uuid,
    val path: String,
    val type: MediaType,
    val status: MediaStatus,
    val width: Int?,
    val height: Int?,
    /** Length of a voice recording or video; null for photos. */
    val duration: Duration? = null,
    /** Downsampled levels of a voice recording, each in 0..1; empty for other types. */
    val waveform: List<Float> = emptyList(),
)

fun Media.toUi(files: MediaFileStore): MediaUi =
    MediaUi(
        id = id,
        path = files.resolve(localPath).toString(),
        type = type,
        status = status,
        width = width,
        height = height,
        duration = duration,
        waveform = waveform,
    )
