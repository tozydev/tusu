package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.materialkolor.hct.Hct
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val LightContainerTone = 90.0
private const val LightContentTone = 30.0
private const val DarkContainerTone = 30.0
private const val DarkContentTone = 90.0

/** A pill tinted from a label group's colour; clickable only when [onClick] is set. */
@Composable
fun LabelChip(
    label: LabelChipData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val (container, content) = remember(label.color, isDark) { chipColors(label.color, isDark) }
    ChipSurface(
        container = container,
        content = content,
        modifier = modifier,
        onClick = onClick,
        text = label.name,
    )
}

@Composable
internal fun ChipSurface(
    container: Color,
    content: Color,
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val body: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    val sized = modifier.widthIn(max = 160.dp)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = sized.minimumInteractiveComponentSize(),
            shape = CircleShape,
            color = container,
            contentColor = content,
            content = body,
        )
    } else {
        Surface(
            modifier = sized,
            shape = CircleShape,
            color = container,
            contentColor = content,
            content = body,
        )
    }
}

/** Keeps the group's hue and chroma but fixes the tone, so text stays legible on both themes. */
private fun chipColors(group: Color, isDark: Boolean): Pair<Color, Color> {
    val hct = Hct.fromInt(group.toArgb())
    val containerTone = if (isDark) DarkContainerTone else LightContainerTone
    val contentTone = if (isDark) DarkContentTone else LightContentTone
    return Color(Hct.from(hct.hue, hct.chroma, containerTone).toInt()) to
        Color(Hct.from(hct.hue, hct.chroma, contentTone).toInt())
}

private val PreviewLabels =
    listOf(
        LabelChipData("Gratitude", Color(0xFF68A500)),
        LabelChipData("Trip", Color(0xFF1E88E5)),
        LabelChipData("Family", Color(0xFFE53935)),
    )

@Preview
@Composable
private fun LabelChipPreview() {
    AppTheme(isDarkTheme = false) {
        Surface {
            Row(Modifier.padding(16.dp)) {
                PreviewLabels.forEach { LabelChip(it, Modifier.padding(end = 8.dp), onClick = {}) }
            }
        }
    }
}

@Preview
@Composable
private fun LabelChipDarkPreview() {
    AppTheme(isDarkTheme = true) {
        Surface {
            Row(Modifier.padding(16.dp)) {
                PreviewLabels.forEach { LabelChip(it, Modifier.padding(end = 8.dp)) }
            }
        }
    }
}
