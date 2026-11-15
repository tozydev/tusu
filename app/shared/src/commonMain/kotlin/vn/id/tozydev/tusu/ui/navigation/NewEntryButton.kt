package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_add_24px
import vn.id.tozydev.tusu.generated.resources.nav_new_entry
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The "+" button next to the navigation pill. */
@Composable
internal fun NewEntryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(FloatingBarHeight),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = FloatingBarShadow,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(Res.drawable.ic_add_24px),
                contentDescription = stringResource(Res.string.nav_new_entry),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview
@Composable
private fun NewEntryButtonPreview() {
    AppTheme { NewEntryButton(onClick = {}) }
}
