package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.component.MessageHost
import vn.id.tozydev.tusu.ui.component.MessageHostState
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val BarBottomMargin = 20.dp
private val ContentGap = 16.dp

/**
 * Draws [content] edge to edge with the [FloatingNavigationBar] and the message host overlaid at
 * the bottom. [content] receives the bottom space the overlay covers so it can scroll under it.
 */
@Composable
internal fun AppFrame(
    selectedTab: AppRoute.TopLevel,
    barVisible: Boolean,
    onTabSelect: (AppRoute.TopLevel) -> Unit,
    onNewEntryClick: () -> Unit,
    messageHostState: MessageHostState,
    modifier: Modifier = Modifier,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding =
        PaddingValues(
            bottom = navigationBarInset + BarBottomMargin + FloatingBarHeight + ContentGap,
        )

    Box(modifier.fillMaxSize()) {
        content(contentPadding)
        Column(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = BarBottomMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MessageHost(messageHostState, Modifier.padding(horizontal = 16.dp))
            AnimatedVisibility(
                visible = barVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                FloatingNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelect = onTabSelect,
                    onNewEntryClick = onNewEntryClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppFramePreview() {
    AppTheme {
        AppFrame(
            selectedTab = AppRoute.Calendar,
            barVisible = true,
            onTabSelect = {},
            onNewEntryClick = {},
            messageHostState = remember { MessageHostState() },
        ) { contentPadding ->
            Surface(Modifier.fillMaxSize()) {
                EmptyState(
                    title = "Content",
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                )
            }
        }
    }
}
