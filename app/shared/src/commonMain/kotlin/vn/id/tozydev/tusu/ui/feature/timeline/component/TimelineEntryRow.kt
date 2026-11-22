package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.timeline_change_emoji
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.component.RichTextContent
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val MaxTextLines = 6

/**
 * One entry of the feed, laid out like a diary page: the time on a rail, then the emoji, the text
 * and the photos. The whole row opens the entry; a long press calls [onLongClick], which the feed
 * uses to change the emoji.
 */
@Composable
fun TimelineEntryRow(
    entry: Entry,
    photos: List<MediaUi>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val formatter = LocalAppDateTimeFormatter.current
    val haptics = LocalHapticFeedback.current
    val longClickLabel = stringResource(Res.string.timeline_change_emoji)
    // Each entry keeps the zone it was written in, so travelling does not shift its time.
    val time =
        remember(entry.occurredAt, entry.timeZone) {
            context(entry.timeZone) { formatter.time(entry.occurredAt) }
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = onClick,
                    onLongClickLabel = onLongClick?.let { longClickLabel },
                    onLongClick =
                        onLongClick?.let {
                            {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                it()
                            }
                        },
                )
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TimelineRail(time = time)
        Column(
            modifier = Modifier.weight(1f).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entry.emoji != null) {
                EmojiBadge(emoji = entry.emoji, size = 32.dp)
            }
            // The stored body of an empty entry is not blank, so only the plain text decides.
            if (entry.textPlain.isNotBlank()) {
                RichTextContent(
                    body = entry.body,
                    fallbackText = entry.textPlain,
                    maxLines = MaxTextLines,
                )
            }
            if (photos.isNotEmpty()) {
                TimelinePhotoGrid(photos = photos)
            }
        }
    }
}

@Preview
@Composable
private fun TimelineEntryRowTextPreview() {
    AppTheme {
        Surface {
            Column {
                sampleTimelineEntries().forEach {
                    TimelineEntryRow(entry = it, photos = emptyList(), onClick = {})
                }
            }
        }
    }
}

@Preview
@Composable
private fun TimelineEntryRowLongTextPreview() {
    AppTheme {
        Surface {
            TimelineEntryRow(entry = sampleLongTimelineEntry(), photos = emptyList(), onClick = {})
        }
    }
}

@Preview
@Composable
private fun TimelineEntryRowPhotosPreview() {
    AppTheme {
        Surface {
            Column {
                listOf(1, 3, 4, 7).forEach { count ->
                    TimelineEntryRow(
                        entry = sampleTimelineEntries().first(),
                        photos = samplePhotos(count),
                        onClick = {},
                    )
                }
            }
        }
    }
}
