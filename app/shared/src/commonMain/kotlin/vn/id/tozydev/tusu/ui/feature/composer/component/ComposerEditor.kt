package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlin.random.Random
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_placeholder_feeling
import vn.id.tozydev.tusu.generated.resources.composer_placeholder_learned
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The rich text editor; it takes focus when shown so the keyboard opens at once. */
@OptIn(ExperimentalRichTextApi::class)
@Composable
fun ComposerEditor(
    richTextState: RichTextState,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    // Picked once per opening, so the prompt does not change while the user is writing.
    val placeholderIndex = rememberSaveable { Random.nextInt(2) }
    val placeholder =
        stringResource(
            if (placeholderIndex == 0) Res.string.composer_placeholder_feeling
            else Res.string.composer_placeholder_learned,
        )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BoxWithConstraints(modifier = modifier) {
        // Read here: the nested ColumnScope hides this scope's maxHeight.
        val viewportHeight = maxHeight
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
        ) {
            // At least a viewport tall, so taps on the empty space below the text reach the editor.
            BasicRichTextEditor(
                state = richTextState,
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = viewportHeight)
                        .focusRequester(focusRequester),
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (richTextState.annotatedString.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun ComposerEditorPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerEditor(
            richTextState = richTextState,
            focusRequester = remember { FocusRequester() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
