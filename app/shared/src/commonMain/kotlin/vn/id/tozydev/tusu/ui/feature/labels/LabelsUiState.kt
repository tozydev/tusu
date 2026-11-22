package vn.id.tozydev.tusu.ui.feature.labels

import androidx.compose.runtime.Immutable
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup

@Immutable
data class LabelsUiState(
    val isLoading: Boolean = true,
    /** Live groups in display order. */
    val groups: List<LabelGroup> = emptyList(),
    /** Live labels, hidden ones included, in display order. */
    val labels: List<Label> = emptyList(),
)
