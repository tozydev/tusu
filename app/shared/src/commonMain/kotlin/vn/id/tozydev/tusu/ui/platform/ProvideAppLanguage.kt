package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import vn.id.tozydev.tusu.domain.model.AppLanguage

/** Applies [language] to every string resolved inside [content]; `SYSTEM` follows the device. */
@Composable
fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    val tag =
        when (language) {
            AppLanguage.SYSTEM -> null
            AppLanguage.VIETNAMESE -> "vi"
            AppLanguage.ENGLISH -> "en"
        }
    CompositionLocalProvider(AppLocale provides tag) {
        // Resources are cached per composition node; a fresh key forces them to reload.
        key(language) { content() }
    }
}
