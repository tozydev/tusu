package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.entry_more_actions
import vn.id.tozydev.tusu.generated.resources.ic_more_vert_24px
import vn.id.tozydev.tusu.ui.component.DateTimePickerDialog
import vn.id.tozydev.tusu.ui.component.RichTextContent
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryDetailHeader
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryDetailTopBar
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryMoreSheet
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryPhotoCarousel
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.format.ProvideAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The entry in full: top bar, photo carousel, date and formatted text. [onPhotoClick] gets the
 * index of the tapped photo in the carousel order. The "more" sheet and the date-time dialog are
 * kept open here; [onOccurredAtChange] gets the new local date-time in the current zone.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EntryDetailScreen(
    state: EntryDetailUiState,
    onBack: () -> Unit,
    onPhotoClick: (index: Int) -> Unit,
    onEditClick: () -> Unit,
    onOccurredAtChange: (LocalDateTime) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreOpen by rememberSaveable { mutableStateOf(false) }
    var changingTime by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            EntryDetailTopBar(onBack = onBack) {
                if (!state.isLoading && !state.isGone) {
                    IconButton(onClick = { moreOpen = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert_24px),
                            contentDescription = stringResource(Res.string.entry_more_actions),
                        )
                    }
                }
            }
            when {
                state.isLoading -> EntryDetailLoading(Modifier.weight(1f))
                // The route closes the screen once the entry is gone.
                state.isGone -> Unit
                else ->
                    EntryDetailContent(
                        state = state,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f),
                    )
            }
        }
    }
    if (moreOpen) {
        // Each row closes the sheet first, so the next step is not shown over it.
        EntryMoreSheet(
            onEdit = {
                moreOpen = false
                onEditClick()
            },
            onChangeTime = {
                moreOpen = false
                changingTime = true
            },
            onDelete = {
                moreOpen = false
                onDeleteClick()
            },
            onDismiss = { moreOpen = false },
        )
    }
    val currentTimeZone = state.currentTimeZone
    val occurredAt = state.occurredAt
    if (changingTime && currentTimeZone != null && occurredAt != null) {
        DateTimePickerDialog(
            initial = occurredAt.toLocalDateTime(currentTimeZone),
            onConfirm = {
                changingTime = false
                onOccurredAtChange(it)
            },
            onDismiss = { changingTime = false },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EntryDetailLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LoadingIndicator()
    }
}

@Composable
private fun EntryDetailContent(
    state: EntryDetailUiState,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalAppDateTimeFormatter.current
    val occurredAt = state.occurredAt
    val timeZone = state.timeZone
    // The entry keeps the zone it was written in, so its day does not shift while travelling.
    val dateTime =
        if (occurredAt != null && timeZone != null) {
            context(timeZone) { formatter.fullDateTime(occurredAt) }
        } else {
            null
        }
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.photos.isNotEmpty()) {
            EntryPhotoCarousel(photos = state.photos, onPhotoClick = onPhotoClick)
        }
        if (dateTime != null) {
            EntryDetailHeader(dateTime = dateTime, emoji = state.emoji)
        }
        // Empty text is hidden completely; textPlain is derived from the body on save.
        if (state.textPlain.isNotBlank()) {
            RichTextContent(
                body = state.body,
                fallbackText = state.textPlain,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun EntryDetailScreenTextPreview() {
    AppTheme {
        ProvideAppDateTimeFormatter {
            EntryDetailScreen(
                state = sampleEntryState(text = "Hôm nay trời đẹp, đi bộ quanh hồ."),
                onBack = {},
                onPhotoClick = {},
                onEditClick = {},
                onOccurredAtChange = {},
                onDeleteClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun EntryDetailScreenFullPreview() {
    AppTheme {
        ProvideAppDateTimeFormatter {
            EntryDetailScreen(
                state =
                    sampleEntryState(
                        text = "Buổi sáng ở Đà Lạt, sương còn chưa tan hết trên đồi thông.",
                        emoji = "🌱",
                        photoCount = 3,
                    ),
                onBack = {},
                onPhotoClick = {},
                onEditClick = {},
                onOccurredAtChange = {},
                onDeleteClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun EntryDetailScreenOnePhotoPreview() {
    AppTheme {
        ProvideAppDateTimeFormatter {
            EntryDetailScreen(
                state = sampleEntryState(photoCount = 1),
                onBack = {},
                onPhotoClick = {},
                onEditClick = {},
                onOccurredAtChange = {},
                onDeleteClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun EntryDetailScreenLoadingPreview() {
    AppTheme {
        EntryDetailScreen(
            state = EntryDetailUiState(),
            onBack = {},
            onPhotoClick = {},
            onEditClick = {},
            onOccurredAtChange = {},
            onDeleteClick = {},
        )
    }
}
