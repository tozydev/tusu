package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid

/** Shows the entry [entryId]; [onEdit] opens the composer and [onOpenViewer] the photo viewer. */
@Composable
fun EntryDetailRoute(
    entryId: Uuid,
    onBack: () -> Unit,
    onEdit: (Uuid) -> Unit,
    onOpenViewer: (startIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(TOZY-67): load the entry and show its content.
    EntryDetailScreen(onBack = onBack, modifier = modifier)
}
