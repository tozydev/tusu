package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Overrides the locale that Compose Resources and the platform resolve strings with.
 *
 * Provide it above the content whose language should change and re-key that content, because the
 * locale is read while resources are loaded.
 */
expect object AppLocale {
    /** Language tag such as `vi`, or `null` to follow the device language. */
    @Composable infix fun provides(languageTag: String?): ProvidedValue<*>
}
