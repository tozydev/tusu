package vn.id.tozydev.tusu.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.withTimeoutOrNull
import vn.id.tozydev.tusu.ui.format.resolve

/** Queues [UiMessage]s and feeds them to a [MessageHost], one at a time in call order. */
@Stable
class MessageHostState {
    internal val snackbarHostState = SnackbarHostState()

    /**
     * Shows [message] once earlier ones are gone and suspends until it disappears. Returns true
     * when the user pressed its action, after the action's callback has run. If the calling
     * coroutine is cancelled the message is removed, so call this from a scope that outlives the
     * screen.
     */
    suspend fun show(message: UiMessage): Boolean {
        val action = message.action
        val timeout = message.timeout
        val result =
            if (timeout == null) {
                snackbarHostState.showSnackbar(
                    message = message.text.resolve(),
                    actionLabel = action?.label?.resolve(),
                    withDismissAction = message.persistent,
                    duration =
                        if (message.persistent) SnackbarDuration.Indefinite
                        else SnackbarDuration.Short,
                )
            } else {
                // Cancelling showSnackbar on timeout removes the snackbar from the host.
                withTimeoutOrNull(timeout) {
                    snackbarHostState.showSnackbar(
                        message = message.text.resolve(),
                        actionLabel = action?.label?.resolve(),
                        withDismissAction = false,
                        duration = SnackbarDuration.Indefinite,
                    )
                } ?: return false
            }
        if (result != SnackbarResult.ActionPerformed) return false
        action?.onAction?.invoke()
        return true
    }
}

/** The app-level host; reading it without a provider is a bug, so it fails instead of dropping. */
val LocalMessageHostState =
    staticCompositionLocalOf<MessageHostState> {
        error("No MessageHostState provided; wrap the content in CompositionLocalProvider.")
    }
