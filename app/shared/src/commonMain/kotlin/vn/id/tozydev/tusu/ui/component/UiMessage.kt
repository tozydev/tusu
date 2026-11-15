package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_retry
import vn.id.tozydev.tusu.ui.format.UiText

/**
 * A one-off message for the user. A message with an [action] stays until the user acts on it or
 * dismisses it, so errors are never missed.
 */
@Immutable
data class UiMessage(
    val text: UiText,
    val action: UiMessageAction? = null,
    val persistent: Boolean = action != null,
    /** The message disappears by itself after this time, even when it has an action. */
    val timeout: Duration? = null,
) {
    companion object {
        /** An error message with a "Retry" button. */
        fun retryable(text: UiText, onRetry: () -> Unit): UiMessage =
            UiMessage(text, UiMessageAction(UiText.of(Res.string.action_retry), onRetry))
    }
}
