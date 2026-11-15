package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_retry
import vn.id.tozydev.tusu.generated.resources.timeline_append_error
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** End of the list: a spinner while the next page loads, or an inline message with a retry. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineAppendFooter(state: LoadState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        is LoadState.Loading ->
            Box(
                modifier = modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
        is LoadState.Error ->
            Row(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.timeline_append_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
            }
        is LoadState.NotLoading -> Unit
    }
}

@Preview
@Composable
private fun TimelineAppendFooterErrorPreview() {
    AppTheme {
        Surface {
            TimelineAppendFooter(
                state = LoadState.Error(IllegalStateException("preview")),
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun TimelineAppendFooterLoadingPreview() {
    AppTheme { Surface { TimelineAppendFooter(state = LoadState.Loading, onRetry = {}) } }
}
