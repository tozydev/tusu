package vn.id.tozydev.tusu.ui.feature.viewer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.component.MediaUi

/**
 * Swipeable photos starting at [startIndex]. Swiping is off while the current photo is zoomed, so a
 * pan never turns into a page change. [chromeAlpha] fades the page indicator during a dismiss.
 */
@Composable
fun PhotoPages(
    photos: List<MediaUi>,
    startIndex: Int,
    chromeAlpha: Float,
    onDismiss: () -> Unit,
    onDismissProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            initialPage = startIndex.coerceIn(photos.indices),
            pageCount = { photos.size },
        )
    var zoomed by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !zoomed,
            beyondViewportPageCount = 1,
            key = { photos[it].id.toString() },
        ) { page ->
            ZoomablePhoto(
                path = photos[page].path,
                onZoomChange = { zoomed = it },
                onDismissProgress = onDismissProgress,
                onDismiss = onDismiss,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (photos.size > 1) {
            PageIndicator(
                current = pagerState.currentPage + 1,
                total = photos.size,
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .safeDrawingPadding()
                        .padding(8.dp)
                        .graphicsLayer { alpha = chromeAlpha },
            )
        }
    }
}
