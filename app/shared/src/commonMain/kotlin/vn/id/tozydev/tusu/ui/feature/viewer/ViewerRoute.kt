package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid

/** Full-screen photos of [entryId], opened at [startIndex]; [onClose] leaves the viewer. */
@Composable
fun ViewerRoute(
    entryId: Uuid,
    startIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(TOZY-14): show the entry's photos.
    ViewerScreen(onClose = onClose, modifier = modifier)
}
