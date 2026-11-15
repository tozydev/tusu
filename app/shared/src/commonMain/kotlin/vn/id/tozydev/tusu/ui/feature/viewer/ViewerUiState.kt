package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.runtime.Immutable
import vn.id.tozydev.tusu.ui.component.MediaUi

/** Photos of one entry in album order; [isLoading] stays true until the first load arrives. */
@Immutable
data class ViewerUiState(val isLoading: Boolean = true, val photos: List<MediaUi> = emptyList())
