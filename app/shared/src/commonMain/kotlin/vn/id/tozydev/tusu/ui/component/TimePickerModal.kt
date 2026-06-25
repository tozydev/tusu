package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.cancel_btn
import vn.id.tozydev.tusu.generated.resources.ic_keyboard_24px
import vn.id.tozydev.tusu.generated.resources.ic_schedule_24px
import vn.id.tozydev.tusu.generated.resources.now_btn
import vn.id.tozydev.tusu.generated.resources.ok_btn
import vn.id.tozydev.tusu.generated.resources.time_picker_title
import vn.id.tozydev.tusu.generated.resources.time_picker_type_toggle_desc

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimePickerModal(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    initialHour: Int = 0,
    initialMinute: Int = 0,
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
        )

    var showDial by remember { mutableStateOf(true) }

    val toggleIcon =
        if (showDial) {
            Res.drawable.ic_keyboard_24px
        } else {
            Res.drawable.ic_schedule_24px
        }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.time_picker_title),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FilledTonalButton(
                        onClick = {
                            // fixme time picker dialog state not update
                            val localTime = clock.now().toLocalDateTime(timeZone)
                            timePickerState.hour = localTime.hour
                            timePickerState.minute = localTime.minute
                        },
                        contentPadding = ButtonDefaults.ExtraSmallContentPadding,
                        modifier = Modifier.height(ButtonDefaults.ExtraSmallContainerHeight),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_schedule_24px),
                            contentDescription = null,
                            modifier =
                                Modifier.size(ButtonDefaults.ExtraSmallIconSize)
                                    .padding(end = ButtonDefaults.ExtraSmallIconSpacing),
                        )
                        Text(stringResource(Res.string.now_btn))
                    }
                }

                if (showDial) {
                    TimePicker(state = timePickerState)
                } else {
                    TimeInput(state = timePickerState)
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { showDial = !showDial }) {
                        Icon(
                            painter = painterResource(toggleIcon),
                            contentDescription =
                                stringResource(Res.string.time_picker_type_toggle_desc),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel_btn))
                    }
                    TextButton(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text(stringResource(Res.string.ok_btn))
                    }
                }
            }
        }
    }
}
