package vn.id.tozydev.tusu.ui.feature.labels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

/** Manages label groups and labels; [onBack] leaves the screen. */
@Composable
fun LabelsRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = metroViewModel<LabelsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LabelsScreen(
        state = state,
        actions =
            LabelsActions(
                onBack = onBack,
                onCreateGroup = viewModel::onCreateGroup,
                onUpdateGroup = viewModel::onUpdateGroup,
                onDeleteGroup = viewModel::onDeleteGroup,
                onCreateLabel = viewModel::onCreateLabel,
                onUpdateLabel = viewModel::onUpdateLabel,
                onDeleteLabel = viewModel::onDeleteLabel,
            ),
        modifier = modifier,
    )
}
