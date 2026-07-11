package vn.id.tozydev.tusu.ui.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.back_desc
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.generated.resources.settings_about_title
import vn.id.tozydev.tusu.generated.resources.settings_appearance_title
import vn.id.tozydev.tusu.generated.resources.settings_backup_restore_title
import vn.id.tozydev.tusu.generated.resources.settings_title
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import dev.zacsweers.metrox.viewmodel.metroViewModel
import vn.id.tozydev.tusu.ui.feature.settings.screens.SettingsAboutScreen
import vn.id.tozydev.tusu.ui.feature.settings.screens.SettingsAppearanceScreen
import vn.id.tozydev.tusu.ui.feature.settings.screens.SettingsBackupScreen
import vn.id.tozydev.tusu.ui.feature.settings.screens.SettingsMainScreen

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(savedStateConfiguration, SettingsRoutes.Main)
    val currentRoute = backStack.lastOrNull()

    val handleBack: () -> Unit = {
        if (currentRoute == SettingsRoutes.Main) {
            onNavigateBack()
        } else {
            backStack.removeLastOrNull()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SettingsTopBar(currentRoute = currentRoute, onNavigateBack = handleBack)
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            SettingsNavigation(backStack)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsTopBar(currentRoute: NavKey?, onNavigateBack: () -> Unit) {
    LargeFlexibleTopAppBar(
        title = {
            SettingsTitle(currentRoute)
        },
        navigationIcon = {
            FilledIconButton(
                onClick = onNavigateBack,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back_24px),
                    contentDescription = stringResource(Res.string.back_desc),
                )
            }
        },
    )
}

@Composable
private fun SettingsTitle(currentRoute: NavKey?) {
    var titleRes by remember { mutableStateOf(Res.string.settings_title) }
    LaunchedEffect(currentRoute) {
        titleRes =
            when (currentRoute) {
                SettingsRoutes.Appearance -> Res.string.settings_appearance_title
                SettingsRoutes.Backup -> Res.string.settings_backup_restore_title
                SettingsRoutes.About -> Res.string.settings_about_title
                else -> Res.string.settings_title
            }
    }

    Text(stringResource(titleRes))
}

@Composable
private fun SettingsNavigation(backStack: NavBackStack<NavKey>) {
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
                entry<SettingsRoutes.Main> {
                    SettingsMainScreen(
                        onNavigateToAppearance = {
                            backStack += SettingsRoutes.Appearance
                        },
                        onNavigateToBackupAndRestore = {
                            backStack += SettingsRoutes.Backup
                        },
                        onNavigateToAbout = {
                            backStack += SettingsRoutes.About
                        },
                    )
                }
                entry<SettingsRoutes.Appearance> {
                    SettingsAppearanceScreen()
                }
                entry<SettingsRoutes.Backup> {
                    SettingsBackupScreen(viewModel = metroViewModel())
                }
                entry<SettingsRoutes.About> {
                    SettingsAboutScreen()
                }
            },
    )
}

@OptIn(ExperimentalSerializationApi::class)
private val savedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<SettingsRoutes>()
        }
    }
}
