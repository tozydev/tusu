package vn.io.tozyworks.tusu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import vn.io.tozyworks.tusu.ui.DemoScreenA
import vn.io.tozyworks.tusu.ui.DemoScreenB
import vn.io.tozyworks.tusu.ui.LocalAppGraph

@OptIn(ExperimentalSerializationApi::class)
private val savedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<AppRoutes>()
        }
    }
}

@Composable
fun AppNavigation(startRoute: AppRoutes = AppRoutes.DemoA) {
    val backStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val appGraph = LocalAppGraph.current

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider =
            entryProvider {
                entry<AppRoutes.DemoA> {
                    DemoScreenA(
                        greetingText = appGraph.greeting.greet(),
                        onNavigateToB = {
                            backStack.add(AppRoutes.DemoB("Hello from Screen A!"))
                        },
                    )
                }

                entry<AppRoutes.DemoB> { key ->
                    DemoScreenB(
                        message = key.message,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}
