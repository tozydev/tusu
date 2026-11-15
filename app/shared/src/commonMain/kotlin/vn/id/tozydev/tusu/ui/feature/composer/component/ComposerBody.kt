package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.asString
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Shows what the composer body holds: a spinner, an error, a read-only entry or the editor. */
@Composable
fun ComposerBody(
    isLoading: Boolean,
    loadError: UiText?,
    isReadOnly: Boolean,
    readOnlyText: String,
    richTextState: RichTextState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            loadError != null ->
                EmptyState(title = loadError.asString(), modifier = Modifier.fillMaxSize())
            isLoading -> ComposerLoading(modifier = Modifier.fillMaxSize())
            isReadOnly -> ReadOnlyEntry(text = readOnlyText, modifier = Modifier.fillMaxSize())
            else -> ComposerEditor(richTextState = richTextState, modifier = Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ComposerLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LoadingIndicator(Modifier.size(48.dp))
    }
}

@Preview
@Composable
private fun ComposerBodyLoadingPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerBody(
            isLoading = true,
            loadError = null,
            isReadOnly = false,
            readOnlyText = "",
            richTextState = richTextState,
        )
    }
}
