package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_back
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Back button on the left, an optional [title] (shown once the content is scrolled), [actions] on
 * the right.
 */
@Composable
fun EntryDetailTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable RowScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back_24px),
                contentDescription = stringResource(Res.string.action_back),
            )
        }
        if (title != null) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { title() }
        } else {
            Spacer(Modifier.weight(1f))
        }
        actions()
    }
}

@Preview
@Composable
private fun EntryDetailTopBarPreview() {
    AppTheme { EntryDetailTopBar(onBack = {}) }
}
