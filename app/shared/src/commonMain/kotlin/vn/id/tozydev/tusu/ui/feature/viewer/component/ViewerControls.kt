package vn.id.tozydev.tusu.ui.feature.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_close
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.generated.resources.viewer_page_indicator
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** White close icon for the black viewer; it is the only way out besides back and the drag. */
@Composable
fun ViewerCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_close_24px),
            contentDescription = stringResource(Res.string.action_close),
            tint = Color.White,
        )
    }
}

/** "2 / 5" pill shown above the photos when the entry has more than one. */
@Composable
fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .background(Color.Black.copy(alpha = 0.45f), shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.viewer_page_indicator, current, total),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun ViewerControlsPreview() {
    AppTheme {
        Box(Modifier.background(Color.Black).padding(8.dp)) { ViewerCloseButton(onClick = {}) }
    }
}

@Preview
@Composable
private fun PageIndicatorPreview() {
    AppTheme {
        Box(Modifier.background(Color.Black).padding(8.dp)) {
            PageIndicator(current = 2, total = 5)
        }
    }
}
