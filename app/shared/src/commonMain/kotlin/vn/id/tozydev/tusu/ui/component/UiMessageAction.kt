package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Immutable
import vn.id.tozydev.tusu.ui.format.UiText

@Immutable data class UiMessageAction(val label: UiText, val onAction: () -> Unit)
