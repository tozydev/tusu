package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import vn.id.tozydev.tusu.ui.component.LocalMessageHostState
import vn.id.tozydev.tusu.ui.feature.calendar.CalendarRoute
import vn.id.tozydev.tusu.ui.feature.composer.ComposerRoute
import vn.id.tozydev.tusu.ui.feature.media.MediaRoute
import vn.id.tozydev.tusu.ui.feature.timeline.TimelineRoute

/** The navigation host and app frame. Needs [LocalMessageHostState] to be provided. */
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AppRouteSavedStateConfiguration, AppRoute.Timeline)
    val currentTab = backStack.currentTab
    // Keeps the bar's selection while it animates out over a non-tab destination.
    var lastTab by remember { mutableStateOf<AppRoute.TopLevel>(AppRoute.Timeline) }
    LaunchedEffect(currentTab) { currentTab?.let { lastTab = it } }

    AppFrame(
        selectedTab = currentTab ?: lastTab,
        barVisible = backStack.showsNavigationBar,
        onTabSelect = { backStack.selectTab(it) },
        onNewEntryClick = { backStack.openComposer() },
        messageHostState = LocalMessageHostState.current,
        modifier = modifier,
    ) { contentPadding ->
        AppNavDisplay(backStack = backStack, contentPadding = contentPadding)
    }
}

@Composable
private fun AppNavDisplay(
    backStack: MutableList<NavKey>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val entries =
        remember(backStack, contentPadding) {
            entryProvider<NavKey> {
                entry<AppRoute.Timeline> { TimelineRoute(contentPadding) }
                entry<AppRoute.Calendar> { CalendarRoute(contentPadding) }
                entry<AppRoute.Media> { MediaRoute(contentPadding) }
                entry<AppRoute.Composer>(metadata = ComposerMetadata) { route ->
                    ComposerRoute(entryId = route.entryId, onClose = { backStack.navigateBack() })
                }
            }
        }
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.navigateBack() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        transitionSpec = fadeThroughTransition(),
        popTransitionSpec = fadeThroughTransition(),
        entryProvider = entries,
    )
}

private val ComposerMetadata =
    NavDisplay.transitionSpec(composerEnterTransition()) +
        NavDisplay.popTransitionSpec(composerExitTransition()) +
        NavDisplay.predictivePopTransitionSpec { composerExitTransition<Scene<*>>().invoke(this) }
