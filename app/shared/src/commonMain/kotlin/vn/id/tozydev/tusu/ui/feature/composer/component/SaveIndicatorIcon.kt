package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_save_saved
import vn.id.tozydev.tusu.generated.resources.composer_save_saving
import vn.id.tozydev.tusu.generated.resources.ic_check_24px
import vn.id.tozydev.tusu.generated.resources.ic_error_24px
import vn.id.tozydev.tusu.ui.feature.composer.SaveIndicator
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.asString
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Shows the save state; keeps its space when hidden so the top bar does not shift. */
@Composable
fun SaveIndicatorIcon(
    indicator: SaveIndicator,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = indicator,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "save indicator",
        ) { state ->
            when (state) {
                SaveIndicator.Hidden -> Box(Modifier.size(24.dp))
                SaveIndicator.Saving -> SavingIndicator()
                SaveIndicator.Saved -> SavedIcon()
                is SaveIndicator.Failed -> FailedButton(message = state.message, onRetry = onRetry)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SavingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.composer_save_saving)
    LoadingIndicator(
        modifier = modifier.size(24.dp).semantics { contentDescription = description },
    )
}

@Composable
private fun SavedIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_check_24px),
        contentDescription = stringResource(Res.string.composer_save_saved),
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun FailedButton(message: UiText, onRetry: () -> Unit) {
    IconButton(onClick = onRetry) {
        Icon(
            painter = painterResource(Res.drawable.ic_error_24px),
            contentDescription = message.asString(),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview
@Composable
private fun SaveIndicatorIconPreview() {
    AppTheme {
        Box(Modifier.size(width = 200.dp, height = 60.dp)) {
            SaveIndicatorIcon(indicator = SaveIndicator.Saving, onRetry = {})
        }
    }
}

@Preview
@Composable
private fun SaveIndicatorIconSavedPreview() {
    AppTheme { SaveIndicatorIcon(indicator = SaveIndicator.Saved, onRetry = {}) }
}
