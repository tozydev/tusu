package vn.id.tozydev.tusu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.id.tozydev.tusu.data.seedTestData
import vn.id.tozydev.tusu.di.AppGraph
import vn.id.tozydev.tusu.ui.integration.InitCoil3
import vn.id.tozydev.tusu.ui.navigation.AppNavigation
import vn.id.tozydev.tusu.ui.theme.AppTheme

val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("No AppGraph provided") }

@Composable
fun App(appGraph: AppGraph) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            seedTestData(appGraph.tusuDatabase)
        }
    }

    InitCoil3(appGraph.appDir)

    CompositionLocalProvider(
        LocalAppGraph provides appGraph,
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        AppTheme {
            AppNavigation()
        }
    }
}
