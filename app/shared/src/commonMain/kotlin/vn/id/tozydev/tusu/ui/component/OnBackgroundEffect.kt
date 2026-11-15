package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** Runs [onBackground] each time the app leaves the foreground, e.g. to flush pending saves. */
@Composable
fun OnBackgroundEffect(onBackground: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { onBackground() }
}
