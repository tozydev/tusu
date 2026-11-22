package vn.id.tozydev.tusu.ui.feature.calendar.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.calendar_next_month
import vn.id.tozydev.tusu.generated.resources.calendar_previous_month
import vn.id.tozydev.tusu.generated.resources.ic_chevron_left_24px
import vn.id.tozydev.tusu.generated.resources.ic_chevron_right_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The month title between the previous and next month buttons. */
@Composable
fun CalendarMonthHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
        IconButton(onClick = onPrevious) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_left_24px),
                contentDescription = stringResource(Res.string.calendar_previous_month),
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right_24px),
                contentDescription = stringResource(Res.string.calendar_next_month),
            )
        }
    }
}

@Preview
@Composable
private fun CalendarMonthHeaderPreview() {
    AppTheme { CalendarMonthHeader(title = "Tháng 11, 2026", onPrevious = {}, onNext = {}) }
}
