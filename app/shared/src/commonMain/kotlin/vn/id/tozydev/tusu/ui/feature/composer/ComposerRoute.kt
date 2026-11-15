package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid

/** Writes a new entry, or edits [entryId]; [onClose] leaves the composer. */
@Composable
fun ComposerRoute(entryId: Uuid?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    // TODO(TOZY-58): replace the placeholder with the editor and its ViewModel.
    ComposerScreen(onClose = onClose, modifier = modifier)
}
