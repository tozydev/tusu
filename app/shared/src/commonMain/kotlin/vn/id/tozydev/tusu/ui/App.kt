package vn.id.tozydev.tusu.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import vn.id.tozydev.tusu.di.AppGraph
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun App(appGraph: AppGraph, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
        AppTheme {
            // TODO(TOZY-54): replace with the navigation host and the three-tab app frame.
            Surface(modifier = modifier.fillMaxSize()) {}
        }
    }
}
