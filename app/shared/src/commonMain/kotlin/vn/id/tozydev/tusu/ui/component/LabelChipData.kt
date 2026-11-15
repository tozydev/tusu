package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** What a label chip renders: the label name and the colour of its group. */
@Immutable data class LabelChipData(val name: String, val color: Color)
