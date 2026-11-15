package vn.id.tozydev.tusu.ui.platform

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private val systemLocale: Locale = Locale.getDefault()

actual object AppLocale {
    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(languageTag: String?): ProvidedValue<*> {
        val locale = languageTag?.let(Locale::forLanguageTag) ?: systemLocale
        Locale.setDefault(locale)
        val configuration = Configuration(LocalConfiguration.current)
        configuration.setLocale(locale)
        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration provides configuration
    }
}
