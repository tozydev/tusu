package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.viewer_empty
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.feature.viewer.component.PhotoPages
import vn.id.tozydev.tusu.ui.feature.viewer.component.ViewerCloseButton
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Chrome opacity at the end of a dismiss drag; it fades from 1 to this value. */
private const val MinChromeAlpha = 0.3f

/**
 * Full-screen photos. The background and the controls fade while a photo is dragged down to dismiss
 * it; [onClose] runs for the close button and for a completed dismiss drag.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ViewerScreen(
    state: ViewerUiState,
    startIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dismissProgress by remember { mutableFloatStateOf(0f) }
    val chromeAlpha = 1f - (1f - MinChromeAlpha) * dismissProgress
    Box(modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = chromeAlpha))) {
        when {
            state.isLoading ->
                LoadingIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            state.photos.isEmpty() -> ViewerEmpty(Modifier.align(Alignment.Center))
            else ->
                PhotoPages(
                    photos = state.photos,
                    startIndex = startIndex,
                    chromeAlpha = chromeAlpha,
                    onDismiss = onClose,
                    onDismissProgress = { dismissProgress = it },
                )
        }
        ViewerCloseButton(
            onClick = onClose,
            modifier =
                Modifier.align(Alignment.TopStart)
                    .safeDrawingPadding()
                    .padding(8.dp)
                    .graphicsLayer { alpha = chromeAlpha },
        )
    }
}

@Composable
private fun ViewerEmpty(modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        EmptyState(title = stringResource(Res.string.viewer_empty), modifier = modifier)
    }
}

@Preview
@Composable
private fun ViewerScreenLoadingPreview() {
    AppTheme { ViewerScreen(state = ViewerUiState(), startIndex = 0, onClose = {}) }
}

@Preview
@Composable
private fun ViewerScreenEmptyPreview() {
    AppTheme {
        ViewerScreen(state = ViewerUiState(isLoading = false), startIndex = 0, onClose = {})
    }
}

@Preview
@Composable
private fun ViewerScreenPhotosPreview() {
    AppTheme {
        ViewerScreen(
            state =
                ViewerUiState(
                    isLoading = false,
                    photos =
                        listOf(
                            MediaUi(
                                Uuid.random(),
                                "",
                                MediaType.PHOTO,
                                MediaStatus.READY,
                                null,
                                null,
                            ),
                            MediaUi(
                                Uuid.random(),
                                "",
                                MediaType.PHOTO,
                                MediaStatus.READY,
                                null,
                                null,
                            ),
                        ),
                ),
            startIndex = 1,
            onClose = {},
        )
    }
}
