package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.emoji_badge_add
import vn.id.tozydev.tusu.generated.resources.ic_add_reaction_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val CornerFraction = 0.32f
private const val EmojiScale = 0.52f

/**
 * One emoji in a rounded container. Without an [emoji] it shows a dashed "add" placeholder, which
 * only makes sense together with [onClick].
 */
@Composable
fun EmojiBadge(
    emoji: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val shape = RoundedCornerShape(size * CornerFraction)
    val description =
        contentDescription
            ?: if (emoji == null) stringResource(Res.string.emoji_badge_add) else null
    val clickableModifier =
        if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier
    val semanticsModifier =
        if (description != null) Modifier.semantics { this.contentDescription = description }
        else Modifier
    val content: @Composable () -> Unit = { EmojiBadgeContent(emoji, size) }
    val containerModifier = modifier.then(clickableModifier).then(semanticsModifier).size(size)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = containerModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = content,
        )
    } else {
        Surface(
            modifier = containerModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = content,
        )
    }
}

@Composable
private fun dashedOutline(size: Dp): Modifier {
    val color = MaterialTheme.colorScheme.outline
    val density = LocalDensity.current
    return Modifier.drawBehind {
        val stroke = with(density) { 1.5.dp.toPx() }
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(this.size.width - stroke, this.size.height - stroke),
            cornerRadius = CornerRadius(with(density) { (size * CornerFraction).toPx() }),
            style =
                Stroke(
                    width = stroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 4, stroke * 3)),
                ),
        )
    }
}

@Composable
private fun EmojiBadgeContent(emoji: String?, size: Dp) {
    val outline = if (emoji == null) dashedOutline(size) else Modifier
    Box(Modifier.fillMaxSize().then(outline), contentAlignment = Alignment.Center) {
        if (emoji != null) {
            val fontSize = with(LocalDensity.current) { (size * EmojiScale).toSp() }
            Text(text = emoji, fontSize = fontSize, maxLines = 1)
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_add_reaction_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    }
}

@Preview
@Composable
private fun EmojiBadgePreview() {
    AppTheme {
        Surface {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(emoji = "😊", size = 32.dp)
                EmojiBadge(emoji = "🌱", modifier = Modifier.padding(start = 12.dp), onClick = {})
                EmojiBadge(emoji = null, modifier = Modifier.padding(start = 12.dp), onClick = {})
                EmojiBadge(emoji = "📔", modifier = Modifier.padding(start = 12.dp), size = 72.dp)
            }
        }
    }
}
