package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_ok
import vn.id.tozydev.tusu.generated.resources.date_time_picker_time_title
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Picks a date, then a time. [onConfirm] gets the chosen local date-time; the caller decides the
 * time zone. [onDismiss] is called when either step is cancelled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initial: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stored as UTC millis, the form the Material date picker uses, so it survives rotation.
    var pickedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val pickedDate = pickedDateMillis?.toDatePickerLocalDate()
    if (pickedDate == null) {
        DateStep(
            initialDate = initial.date,
            onNext = { pickedDateMillis = it.toDatePickerMillis() },
            onDismiss = onDismiss,
            modifier = modifier,
        )
    } else {
        TimeStep(
            initialTime = initial.time,
            onConfirm = { time -> onConfirm(LocalDateTime(pickedDate, time)) },
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateStep(
    initialDate: LocalDate,
    onNext: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state =
        rememberDatePickerState(initialSelectedDateMillis = initialDate.toDatePickerMillis())
    val selectedMillis = state.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        confirmButton = {
            TextButton(
                enabled = selectedMillis != null,
                onClick = { selectedMillis?.let { onNext(it.toDatePickerLocalDate()) } },
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeStep(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state =
        rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true,
        )
    TimePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(Res.string.date_time_picker_time_title),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(state.hour, state.minute)) }) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    ) {
        TimePicker(state = state)
    }
}

@Preview
@Composable
private fun DateTimePickerDialogPreview() {
    AppTheme {
        Surface {
            DateTimePickerDialog(
                initial = LocalDateTime(2026, 11, 15, 9, 30),
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
