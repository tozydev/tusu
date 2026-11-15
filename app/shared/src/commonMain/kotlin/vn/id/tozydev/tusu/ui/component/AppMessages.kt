package vn.id.tozydev.tusu.ui.component

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * App-wide queue of one-off messages; outlives screens, so a message can be posted while its screen
 * closes.
 */
@SingleIn(AppScope::class)
@Inject
class AppMessages {
    private val channel = Channel<UiMessage>(Channel.UNLIMITED)

    /** Collected once by the app root. */
    val messages: Flow<UiMessage> = channel.receiveAsFlow()

    fun post(message: UiMessage) {
        channel.trySend(message)
    }
}
