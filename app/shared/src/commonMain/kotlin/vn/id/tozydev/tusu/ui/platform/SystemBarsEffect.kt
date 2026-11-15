package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable

/** Keeps the status and navigation bar icons legible on the app theme, not just the system one. */
@Composable expect fun SystemBarsEffect(isDarkTheme: Boolean)
