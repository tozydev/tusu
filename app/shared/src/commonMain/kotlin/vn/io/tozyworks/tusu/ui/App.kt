package vn.io.tozyworks.tusu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.io.tozyworks.tusu.data.seedTestData
import vn.io.tozyworks.tusu.di.AppGraph
import vn.io.tozyworks.tusu.ui.integration.InitCoil3
import vn.io.tozyworks.tusu.ui.navigation.AppNavigation
import vn.io.tozyworks.tusu.ui.theme.AppTheme

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
