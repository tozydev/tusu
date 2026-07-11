package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import vn.id.tozydev.tusu.ui.LocalAppGraph
import vn.id.tozydev.tusu.ui.feature.entryeditor.EntryEditorScreen
import vn.id.tozydev.tusu.ui.feature.entryeditor.EntryEditorViewModel
import vn.id.tozydev.tusu.ui.feature.feed.FeedScreen
import vn.id.tozydev.tusu.ui.feature.settings.SettingsScreen

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
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<AppRoutes.Feed> {
                    context(appGraph.dateTimeFormatter) {
                        FeedScreen(
                            viewModel = metroViewModel(),
                            onNavigateToEditor = { id -> backStack += AppRoutes.EntryEditor(id) },
                            onNavigateToSettings = { backStack += AppRoutes.Settings },
                        )
                    }
                }

                entry<AppRoutes.Settings> {
                    SettingsScreen(onNavigateBack = { backStack.removeLastOrNull() })
                }

                entry<AppRoutes.EntryEditor> { route ->
                    context(appGraph.dateTimeFormatter) {
                        EntryEditorScreen(
                            viewModel =
                                assistedMetroViewModel<
                                    EntryEditorViewModel,
                                    EntryEditorViewModel.Factory,
                                > {
                                    create(route.entryId)
                                },
                            onNavigateBack = { backStack.removeLastOrNull() },
                        )
                    }
                }
            },
    )
}
