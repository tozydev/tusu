package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.placeholder_composer_description
import vn.id.tozydev.tusu.generated.resources.placeholder_composer_title
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerTopBar
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun ComposerScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ComposerTopBar(onClose = onClose, modifier = Modifier.fillMaxWidth())
            EmptyState(
                title = stringResource(Res.string.placeholder_composer_title),
                description = stringResource(Res.string.placeholder_composer_description),
                illustration = { EmojiBadge(emoji = "✏️", size = 72.dp) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ComposerScreenPreview() {
    AppTheme { ComposerScreen(onClose = {}) }
}
