package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/** Scale and pan of one photo; [offset] is the translation in pixels from the centred position. */
@Immutable
data class ZoomState(val scale: Float = MinScale, val offset: Offset = Offset.Zero) {
    val isZoomed: Boolean
        get() = scale > MinScale

    /** Applies a pinch/pan gesture step and keeps the photo inside [container]. */
    fun transformBy(zoom: Float, pan: Offset, centroid: Offset, container: Size): ZoomState =
        zoomAround(scale * zoom, centroid, container, pan)

    /** Double tap: zooms to [DoubleTapScale] around [tap], or back to 1x when already zoomed. */
    fun toggle(tap: Offset, container: Size): ZoomState =
        if (isZoomed) ZoomState() else zoomAround(DoubleTapScale, tap, container, Offset.Zero)

    private fun zoomAround(
        newScale: Float,
        centroid: Offset,
        container: Size,
        pan: Offset,
    ): ZoomState {
        val clampedScale = newScale.coerceIn(MinScale, MaxScale)
        if (clampedScale == MinScale) return ZoomState()
        // The content under [centroid] must stay under it: with r = newScale / scale and
        // d = centroid - centre, the new offset is r * offset + (1 - r) * d.
        val ratio = clampedScale / scale
        val fromCentre = centroid - Offset(container.width / 2f, container.height / 2f)
        val zoomed = offset * ratio + fromCentre * (1f - ratio) + pan
        return ZoomState(clampedScale, zoomed.clampTo(container, clampedScale))
    }

    companion object {
        const val MinScale = 1f
        const val MaxScale = 5f
        const val DoubleTapScale = 2.5f
    }
}

/** Keeps the scaled photo covering the container: each axis may move by half the overhang. */
private fun Offset.clampTo(container: Size, scale: Float): Offset {
    val maxX = container.width * (scale - 1f) / 2f
    val maxY = container.height * (scale - 1f) / 2f
    return Offset(x.coerceIn(-maxX, maxX), y.coerceIn(-maxY, maxY))
}
