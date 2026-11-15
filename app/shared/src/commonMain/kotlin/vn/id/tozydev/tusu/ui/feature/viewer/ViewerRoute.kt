package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.uuid.Uuid

/** Full-screen photos of [entryId], opened at [startIndex]; [onClose] leaves the viewer. */
@Composable
fun ViewerRoute(
    entryId: Uuid,
    startIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = metroViewModel<ViewerViewModel>()
    LaunchedEffect(entryId) { viewModel.bind(entryId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ViewerScreen(state = state, startIndex = startIndex, onClose = onClose, modifier = modifier)
}
