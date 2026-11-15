package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
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
    // TODO(TOZY-68): the "more" sheet calls onEdit.
    val viewModel = metroViewModel<EntryDetailViewModel>()
    LaunchedEffect(entryId) { viewModel.bind(entryId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isGone) { if (state.isGone) onBack() }
    EntryDetailScreen(
        state = state,
        onBack = onBack,
        onPhotoClick = onOpenViewer,
        modifier = modifier,
    )
}
