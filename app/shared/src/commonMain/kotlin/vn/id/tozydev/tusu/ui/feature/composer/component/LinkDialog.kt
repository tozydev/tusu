package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_done
import vn.id.tozydev.tusu.generated.resources.link_dialog_title
import vn.id.tozydev.tusu.generated.resources.link_remove
import vn.id.tozydev.tusu.generated.resources.link_url_label
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Asks for the URL of a link; [isEditing] adds a button that removes the existing link. */
@Composable
fun LinkDialog(
    initialUrl: String,
    isEditing: Boolean,
    onConfirm: (String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    val trimmed = url.trim()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(Res.string.link_dialog_title)) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(Res.string.link_url_label)) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(onDone = { if (trimmed.isNotEmpty()) onConfirm(trimmed) }),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trimmed) }, enabled = trimmed.isNotEmpty()) {
                Text(stringResource(Res.string.action_done))
            }
        },
        dismissButton = {
            Row {
                if (isEditing) {
                    TextButton(onClick = onRemove) { Text(stringResource(Res.string.link_remove)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
            }
        },
    )
}

@Preview
@Composable
private fun LinkDialogPreview() {
    AppTheme {
        LinkDialog(
            initialUrl = "https://tozydev.id.vn",
            isEditing = true,
            onConfirm = {},
            onRemove = {},
            onDismiss = {},
        )
    }
}
