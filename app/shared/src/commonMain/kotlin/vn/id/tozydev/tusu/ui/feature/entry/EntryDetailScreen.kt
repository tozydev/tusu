package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.entry_more_actions
import vn.id.tozydev.tusu.generated.resources.ic_more_vert_24px
import vn.id.tozydev.tusu.ui.component.DateTimePickerDialog
import vn.id.tozydev.tusu.ui.component.LabelChip
import vn.id.tozydev.tusu.ui.component.LocationCard
import vn.id.tozydev.tusu.ui.component.RichTextContent
import vn.id.tozydev.tusu.ui.component.labelChipsOf
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryDetailHeader
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryDetailTopBar
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryMoreSheet
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryPhotoCarousel
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryPhotoStrip
import vn.id.tozydev.tusu.ui.feature.entry.component.EntryVoiceGroup
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
    labelGroups: List<LabelGroup> = emptyList(),
) {
    var moreOpen by rememberSaveable { mutableStateOf(false) }
    var changingTime by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val collapseAt = with(LocalDensity.current) { CollapseOffset.roundToPx() }
    // Past the carousel the top bar takes over the emoji and date, and the photos shrink to tiles.
    val collapsed by remember { derivedStateOf { scrollState.value > collapseAt } }
    val dateTime = entryDateTime(state)
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            EntryDetailTopBar(
                onBack = onBack,
                title =
                    if (collapsed && dateTime != null) {
                        { CollapsedTitle(emoji = state.emoji, dateTime = dateTime) }
                    } else null,
            ) {
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
                else -> {
                    AnimatedVisibility(visible = collapsed && state.photos.isNotEmpty()) {
                        EntryPhotoStrip(photos = state.photos, onPhotoClick = onPhotoClick)
                    }
                    EntryDetailContent(
                        state = state,
                        dateTime = dateTime,
                        scrollState = scrollState,
                        labelGroups = labelGroups,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
    if (moreOpen) {
        val formatter = LocalAppDateTimeFormatter.current
        val zone = state.currentTimeZone
        val createdAt = state.createdAt
        val updatedAt = state.updatedAt
        // Each row closes the sheet first, so the next step is not shown over it.
        EntryMoreSheet(
            createdAt =
                if (zone != null && createdAt != null) {
                    context(zone) { formatter.fullDateTime(createdAt) }
                } else null,
            // An entry that was never edited only shows when it was created.
            updatedAt =
                if (zone != null && updatedAt != null && updatedAt != createdAt) {
                    context(zone) { formatter.fullDateTime(updatedAt) }
                } else null,
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
    dateTime: String?,
    scrollState: ScrollState,
    labelGroups: List<LabelGroup>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.photos.isNotEmpty()) {
            EntryPhotoCarousel(photos = state.photos, onPhotoClick = onPhotoClick)
        }
        if (dateTime != null) {
            EntryDetailHeader(dateTime = dateTime, emoji = state.emoji)
        }
        if (state.labels.isNotEmpty()) {
            val otherColor = MaterialTheme.colorScheme.outline
            val chips =
                remember(state.labels, labelGroups, otherColor) {
                    labelChipsOf(state.labels, labelGroups, otherColor)
                }
            FlowRow(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                chips.forEach { LabelChip(it) }
            }
        }
        if (state.voices.isNotEmpty()) {
            EntryVoiceGroup(
                voices = state.voices,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        // Empty text is hidden completely; textPlain is derived from the body on save.
        if (state.textPlain.isNotBlank()) {
            RichTextContent(
                body = state.body,
                fallbackText = state.textPlain,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        state.location?.let { location ->
            LocationCard(
                name = location.name,
                address = location.address,
                mapSnapshotPath = location.mapSnapshotPath,
                hasCoordinates = location.coordinates != null,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
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
                        voiceCount = 2,
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

private val CollapseOffset = 240.dp

/** The entry's date and time in the zone it was written in, so its day does not shift. */
@Composable
private fun entryDateTime(state: EntryDetailUiState): String? {
    val formatter = LocalAppDateTimeFormatter.current
    val occurredAt = state.occurredAt ?: return null
    val timeZone = state.timeZone ?: return null
    return context(timeZone) { formatter.fullDateTime(occurredAt) }
}

@Composable
private fun CollapsedTitle(emoji: String?, dateTime: String, modifier: Modifier = Modifier) {
    Text(
        text = if (emoji != null) "$emoji  $dateTime" else dateTime,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
