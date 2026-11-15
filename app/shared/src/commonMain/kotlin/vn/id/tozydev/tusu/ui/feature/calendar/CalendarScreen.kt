package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.placeholder_calendar_description
import vn.id.tozydev.tusu.generated.resources.placeholder_calendar_title
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun CalendarScreen(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = stringResource(Res.string.placeholder_calendar_title),
            description = stringResource(Res.string.placeholder_calendar_description),
            illustration = { EmojiBadge(emoji = "🗓️", size = 72.dp) },
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(contentPadding),
        )
    }
}

@Preview
@Composable
private fun CalendarScreenPreview() {
    AppTheme { CalendarScreen(contentPadding = PaddingValues(bottom = 104.dp)) }
}
