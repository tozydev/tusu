package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_calendar_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme
import vn.id.tozydev.tusu.ui.theme.PillShape

/**
 * One tab of the [FloatingNavigationBar]: a labelled pill while [selected], an icon-only 48 dp
 * button otherwise.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun NavigationTabItem(
    selected: Boolean,
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val container by
        animateColorAsState(
            targetValue = if (selected) colors.primary else Color.Transparent,
            animationSpec = motion.defaultEffectsSpec(),
        )
    val content by
        animateColorAsState(
            targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
            animationSpec = motion.defaultEffectsSpec(),
        )
    val startPadding by
        animateDpAsState(if (selected) 14.dp else 12.dp, motion.defaultSpatialSpec())
    val endPadding by animateDpAsState(if (selected) 18.dp else 12.dp, motion.defaultSpatialSpec())

    Row(
        modifier =
            modifier
                .height(48.dp)
                .clip(PillShape)
                .background(container)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(start = startPadding, end = endPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            // The label already names a selected tab.
            contentDescription = if (selected) null else label,
            modifier = Modifier.size(24.dp),
            tint = content,
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(motion.defaultEffectsSpec()) + expandHorizontally(sizeSpec()),
            exit = fadeOut(motion.fastEffectsSpec()) + shrinkHorizontally(sizeSpec()),
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp),
                color = content,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

// Springs may overshoot below zero, which a size cannot do, so sizes use the non-bouncy effects.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun sizeSpec() = MaterialTheme.motionScheme.defaultEffectsSpec<IntSize>()

@Preview
@Composable
private fun NavigationTabItemPreview() {
    AppTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavigationTabItem(
                selected = true,
                icon = Res.drawable.ic_calendar_24px,
                label = "Calendar",
                onClick = {},
            )
            NavigationTabItem(
                selected = false,
                icon = Res.drawable.ic_calendar_24px,
                label = "Calendar",
                onClick = {},
            )
        }
    }
}
