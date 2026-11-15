package vn.id.tozydev.tusu.ui.feature.viewer.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.media_photo
import vn.id.tozydev.tusu.ui.feature.viewer.ZoomState
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val DismissDistance = 120.dp

/**
 * One photo that zooms and pans, and follows a vertical drag at 1x until it is released.
 * [onDismissProgress] reports the drag from 0 (at rest) to 1 (at [DismissDistance]), so the viewer
 * can fade its background; [onDismiss] runs when the drag is released beyond that distance.
 */
@Composable
fun ZoomablePhoto(
    path: String,
    onZoomChange: (Boolean) -> Unit,
    onDismissProgress: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoom by remember { mutableStateOf(ZoomState()) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val dismissDistance = with(LocalDensity.current) { DismissDistance.toPx() }
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val scope = rememberCoroutineScope()
    val currentOnDismissProgress by rememberUpdatedState(onDismissProgress)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    fun progressOf(dy: Float): Float = (abs(dy) / dismissDistance).coerceIn(0f, 1f)

    fun endDismissDrag() {
        if (abs(dragY) > dismissDistance) {
            currentOnDismiss()
        } else {
            scope.launch {
                animate(dragY, 0f) { value, _ ->
                    dragY = value
                    currentOnDismissProgress(progressOf(value))
                }
            }
        }
    }

    LaunchedEffect(zoom.isZoomed) { onZoomChange(zoom.isZoomed) }

    AsyncImage(
        model = path,
        contentDescription = stringResource(Res.string.media_photo),
        contentScale = ContentScale.Fit,
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .graphicsLayer {
                    scaleX = zoom.scale
                    scaleY = zoom.scale
                    translationX = zoom.offset.x
                    translationY = zoom.offset.y + dragY
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tap -> zoom = zoom.toggle(tap, containerSize) },
                    )
                }
                .pointerInput(touchSlop) {
                    detectPhotoGestures(
                        touchSlop = touchSlop,
                        isZoomed = { zoom.isZoomed },
                        onTransform = { factor, pan, centroid ->
                            zoom = zoom.transformBy(factor, pan, centroid, containerSize)
                        },
                        onDismissDrag = { dy ->
                            dragY += dy
                            currentOnDismissProgress(progressOf(dragY))
                        },
                        onDismissDragEnd = { endDismissDrag() },
                    )
                },
    )
}

@Preview
@Composable
private fun ZoomablePhotoPreview() {
    AppTheme {
        ZoomablePhoto(
            path = "",
            onZoomChange = {},
            onDismissProgress = {},
            onDismiss = {},
            modifier = Modifier.background(Color.Black),
        )
    }
}
