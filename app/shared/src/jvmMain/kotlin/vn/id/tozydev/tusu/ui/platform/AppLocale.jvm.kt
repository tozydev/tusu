package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

private val LocalLanguageTag = staticCompositionLocalOf<String?> { null }

private val systemLocale: Locale = Locale.getDefault()

actual object AppLocale {
    @Composable
    actual infix fun provides(languageTag: String?): ProvidedValue<*> {
        Locale.setDefault(languageTag?.let(Locale::forLanguageTag) ?: systemLocale)
        // Changing the value makes readers recompose after the default locale changed.
        return LocalLanguageTag provides languageTag
    }
}
