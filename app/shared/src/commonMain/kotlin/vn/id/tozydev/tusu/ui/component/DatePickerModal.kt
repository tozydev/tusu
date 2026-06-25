package vn.id.tozydev.tusu.ui.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.cancel_btn
import vn.id.tozydev.tusu.generated.resources.ok_btn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onConfirm: (Long?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedDateMillis: Long? = null,
) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
                Text(stringResource(Res.string.ok_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel_btn))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier,
    ) {
        DatePicker(state = datePickerState)
    }
}
