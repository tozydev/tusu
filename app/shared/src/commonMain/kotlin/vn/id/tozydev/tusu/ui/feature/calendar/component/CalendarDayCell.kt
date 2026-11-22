package vn.id.tozydev.tusu.ui.feature.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * One day of the month grid: the day number, the emoji of its latest entry and a dot when it has
 * more than one entry. A day without entries shows only its number, with no reminder.
 */
@Composable
fun CalendarDayCell(
    day: Int,
    emoji: String?,
    entryCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(if (isSelected) Modifier.background(colors.primaryContainer) else Modifier)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday || entryCount > 0) FontWeight.Bold else null,
                color =
                    when {
                        isSelected -> colors.onPrimaryContainer
                        isToday -> colors.primary
                        else -> colors.onSurface
                    },
            )
            if (emoji != null) {
                Text(text = emoji, fontSize = 18.sp)
            }
            if (entryCount > 1) {
                Row(Modifier.padding(top = 2.dp)) {
                    Box(Modifier.size(4.dp).clip(CircleShape).background(colors.primary))
                }
            }
        }
    }
}

@Preview
@Composable
private fun CalendarDayCellPreview() {
    AppTheme {
        Row {
            CalendarDayCell(
                day = 4,
                emoji = null,
                entryCount = 0,
                isSelected = false,
                isToday = false,
                onClick = {},
                modifier = Modifier.size(48.dp),
            )
            CalendarDayCell(
                day = 5,
                emoji = "🌙",
                entryCount = 2,
                isSelected = true,
                isToday = false,
                onClick = {},
                modifier = Modifier.size(48.dp),
            )
            CalendarDayCell(
                day = 6,
                emoji = "☀️",
                entryCount = 1,
                isSelected = false,
                isToday = true,
                onClick = {},
                modifier = Modifier.size(48.dp),
            )
        }
    }
}
