package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun CalendarRoute(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    // TODO(TOZY-19): replace the placeholder with the calendar.
    CalendarScreen(contentPadding = contentPadding, modifier = modifier)
}
