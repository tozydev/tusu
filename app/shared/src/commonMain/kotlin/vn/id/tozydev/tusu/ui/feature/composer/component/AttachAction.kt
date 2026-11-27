package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_add_location
import vn.id.tozydev.tusu.generated.resources.composer_pick_photos
import vn.id.tozydev.tusu.generated.resources.composer_record_voice
import vn.id.tozydev.tusu.generated.resources.composer_take_photo
import vn.id.tozydev.tusu.generated.resources.ic_add_a_photo_24px
import vn.id.tozydev.tusu.generated.resources.ic_add_photo_alternate_24px
import vn.id.tozydev.tusu.generated.resources.ic_location_on_24px
import vn.id.tozydev.tusu.generated.resources.ic_mic_24px

/** One thing that can be added to an entry, offered by the toolbar and by the FAB menu. */
@Immutable
data class AttachAction(
    val icon: DrawableResource,
    val label: StringResource,
    val onClick: () -> Unit,
)

/**
 * The attach actions in their fixed order. The camera is left out on platforms without one, and the
 * location once the entry has a place.
 */
fun attachActions(
    onTakePhoto: (() -> Unit)?,
    onPickPhotos: () -> Unit,
    onRecord: () -> Unit,
    onAddLocation: (() -> Unit)?,
): List<AttachAction> = buildList {
    onTakePhoto?.let {
        add(AttachAction(Res.drawable.ic_add_a_photo_24px, Res.string.composer_take_photo, it))
    }
    add(
        AttachAction(
            Res.drawable.ic_add_photo_alternate_24px,
            Res.string.composer_pick_photos,
            onPickPhotos,
        ),
    )
    add(AttachAction(Res.drawable.ic_mic_24px, Res.string.composer_record_voice, onRecord))
    onAddLocation?.let {
        add(AttachAction(Res.drawable.ic_location_on_24px, Res.string.composer_add_location, it))
    }
}
