package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun CalendarRoute(
    contentPadding: PaddingValues,
    onNewEntryForDay: (LocalDate) -> Unit,
    onEntryClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = metroViewModel<CalendarViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(
        state = state,
        contentPadding = contentPadding,
        onMonthChange = viewModel::onMonthChange,
        onDaySelect = viewModel::onDaySelect,
        onEntryClick = onEntryClick,
        onNewEntryForDay = onNewEntryForDay,
        modifier = modifier,
    )
}
