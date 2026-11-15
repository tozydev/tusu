package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_back
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun EntryDetailScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            IconButton(onClick = onBack, modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back_24px),
                    contentDescription = stringResource(Res.string.action_back),
                )
            }
        }
    }
}

@Preview
@Composable
private fun EntryDetailScreenPreview() {
    AppTheme { EntryDetailScreen(onBack = {}) }
}
