package vn.id.tozydev.tusu.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.setSingletonImageLoaderFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import vn.id.tozydev.tusu.di.AppGraph
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.ui.platform.newAppImageLoader
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun App(appGraph: AppGraph, modifier: Modifier = Modifier) {
    setSingletonImageLoaderFactory { context -> newAppImageLoader(context, appGraph.cacheDir) }
    val settings by appGraph.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
        AppTheme(themeMode = settings.themeMode, seedColor = settings.seedColor?.let(::Color)) {
            // TODO(TOZY-54): replace with the navigation host and the three-tab app frame.
            Surface(modifier = modifier.fillMaxSize()) {}
        }
    }
}
