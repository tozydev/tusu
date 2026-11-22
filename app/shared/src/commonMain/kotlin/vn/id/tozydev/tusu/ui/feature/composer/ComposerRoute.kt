package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.ui.component.OnBackgroundEffect
import vn.id.tozydev.tusu.ui.platform.rememberCameraLauncher
import vn.id.tozydev.tusu.ui.platform.rememberPhotoPickerLauncher

/** Writes a new entry (dated [date] when given), or edits [entryId]; [onClose] leaves it. */
@Composable
fun ComposerRoute(
    entryId: Uuid?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    date: LocalDate? = null,
) {
    val viewModel = metroViewModel<ComposerViewModel>()
    LaunchedEffect(entryId) { viewModel.bind(entryId, date) }
    LaunchedEffect(viewModel) {
        snapshotFlow { viewModel.richTextState.annotatedString }
            .collect { viewModel.onBodyChanged() }
    }
    OnBackgroundEffect(viewModel::onBackground)
    // Leaving the route clears the ViewModel, which finishes the session.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val photoPicker =
        rememberPhotoPickerLauncher(
            onResult = viewModel::onPhotosPicked,
            onError = viewModel::onPickerFailed,
        )
    // Null on platforms without a camera; the screen then hides the camera button.
    val camera =
        rememberCameraLauncher(
            onResult = { photo -> photo?.let { viewModel.onPhotosPicked(listOf(it)) } },
            onError = viewModel::onPickerFailed,
        )
    ComposerScreen(
        state = state,
        richTextState = viewModel.richTextState,
        actions =
            ComposerActions(
                onClose = onClose,
                onRetrySave = viewModel::retrySave,
                onEmojiChange = viewModel::onEmojiChange,
                onOccurredAtChange = viewModel::onOccurredAtChange,
                onPickPhotos = photoPicker::launch,
                onTakePhoto = camera?.let { launcher -> launcher::launch },
                onRemovePhoto = viewModel::onRemovePhoto,
                onRetryPhoto = viewModel::onRetryPhoto,
                onLabelToggle = viewModel::onLabelToggle,
                onCreateLabel = viewModel::onCreateLabel,
            ),
        modifier = modifier,
    )
}
