package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The accent colour of the system (Material You wallpaper colour), or null where unsupported. */
@Composable expect fun systemAccentColor(): Color?
