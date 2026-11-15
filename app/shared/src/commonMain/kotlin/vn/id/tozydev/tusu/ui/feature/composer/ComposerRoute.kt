package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.ui.component.OnBackgroundEffect

/** Writes a new entry, or edits [entryId]; [onClose] leaves the composer. */
@Composable
fun ComposerRoute(entryId: Uuid?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = metroViewModel<ComposerViewModel>()
    LaunchedEffect(entryId) { viewModel.bind(entryId) }
    LaunchedEffect(viewModel) {
        snapshotFlow { viewModel.richTextState.annotatedString }
            .collect { viewModel.onBodyChanged() }
    }
    OnBackgroundEffect(viewModel::onBackground)
    // Leaving the route clears the ViewModel, which finishes the session.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ComposerScreen(
        state = state,
        richTextState = viewModel.richTextState,
        onClose = onClose,
        onRetrySave = viewModel::retrySave,
        modifier = modifier,
    )
}
