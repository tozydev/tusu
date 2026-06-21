package vn.io.tozyworks.tusu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import vn.io.tozyworks.tusu.ui.LocalAppGraph
import vn.io.tozyworks.tusu.ui.feature.editor.EditorScreen
import vn.io.tozyworks.tusu.ui.feature.feed.FeedScreen
import vn.io.tozyworks.tusu.ui.feature.settings.SettingsScreen

@OptIn(ExperimentalSerializationApi::class)
private val savedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<AppRoutes>()
        }
    }
}

@Composable
fun AppNavigation(startRoute: AppRoutes = AppRoutes.Feed) {
    val backStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val appGraph = LocalAppGraph.current

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider =
            entryProvider {
                entry<AppRoutes.Feed> {
                    context(appGraph.dateTimeFormatter) {
                        FeedScreen(
                            viewModel = metroViewModel(),
                            onNavigateToEditor = { id -> backStack += AppRoutes.Editor(id) },
                            onNavigateToSettings = { backStack += AppRoutes.Settings },
                        )
                    }
                }

                entry<AppRoutes.Settings> {
                    SettingsScreen()
                }

                entry<AppRoutes.Editor> { entryId ->
                    EditorScreen()
                }
            },
    )
}
