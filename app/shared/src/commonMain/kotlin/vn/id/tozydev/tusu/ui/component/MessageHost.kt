package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Renders the messages of [state]; place it where snackbars should appear. */
@Composable
fun MessageHost(state: MessageHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = state.snackbarHostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.large,
            actionColor = MaterialTheme.colorScheme.inversePrimary,
        )
    }
}

/** Shows every message of [messages] on [state] for as long as this is in composition. */
@Composable
fun MessageEffect(
    messages: Flow<UiMessage>,
    state: MessageHostState = LocalMessageHostState.current,
) {
    LaunchedEffect(messages, state) { messages.collect { state.show(it) } }
}

@Preview
@Composable
private fun MessageHostPreview() {
    AppTheme {
        val state = remember { MessageHostState() }
        LaunchedEffect(state) {
            state.show(UiMessage.retryable(UiText.of("Could not save the photo.")) {})
        }
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                MessageHost(state)
            }
        }
    }
}
