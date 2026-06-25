package vn.id.tozydev.tusu.ui.feature.feed.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TagFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = isSelected,
        onClick = { onClick() },
        label = {
            Text(
                text,
                style = MaterialTheme.typography.labelLargeEmphasized,
                fontWeight = if (isSelected) FontWeight.Bold else null,
            )
        },
        border = null,
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier = modifier,
    )
}
