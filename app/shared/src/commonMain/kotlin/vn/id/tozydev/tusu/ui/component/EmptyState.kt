package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Centered placeholder for empty lists and unfinished screens. [illustration] is usually an
 * [EmojiBadge] or an icon; the button shows only when both [actionLabel] and [onActionClick] are
 * set.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    illustration: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (illustration != null) {
            illustration()
            Spacer(Modifier.height(20.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onActionClick != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onActionClick) { Text(actionLabel) }
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            EmptyState(
                title = "No entries yet",
                description = "Write your first memory and it will show up here.",
                illustration = { EmojiBadge(emoji = "📔", size = 72.dp) },
                actionLabel = "New entry",
                onActionClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun EmptyStateMinimalPreview() {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            EmptyState(title = "Nothing here", modifier = Modifier.fillMaxSize())
        }
    }
}
