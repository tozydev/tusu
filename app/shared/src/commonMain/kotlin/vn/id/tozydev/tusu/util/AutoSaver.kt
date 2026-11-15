package vn.id.tozydev.tusu.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.domain.error.Outcome

/**
 * Debounces submitted values and saves them one at a time.
 *
 * A single worker coroutine owns all bookkeeping and processes commands in order. Saves run inside
 * that worker, so they cannot overlap, and no lock is needed. Public calls only enqueue commands,
 * which makes them safe to call from any thread.
 */
class AutoSaver<T, E>(
    private val scope: CoroutineScope,
    private val debounce: Duration = 500.milliseconds,
    private val save: suspend (T) -> Outcome<Unit, E>,
) {
    private val _state = MutableStateFlow<SaveState<E>>(SaveState.Idle)
    val state: StateFlow<SaveState<E>> = _state.asStateFlow()

    private val commands = Channel<Command<T>>(Channel.UNLIMITED)

    // Only the worker reads or writes the fields below.
    private var latest: Latest<T>? = null
    private var pending = false
    private var timer: Job? = null
    private var timerGeneration = 0

    init {
        scope.launch { runWorker() }
    }

    /** Remembers [value] as the latest one and restarts the debounce timer. */
    fun submit(value: T) {
        // Fails only after the saver has stopped, when there is nothing left to save.
        commands.trySend(Command.Submit(value))
    }

    /** Saves the pending value now instead of waiting for the debounce. */
    fun flush() {
        commands.trySend(Command.Flush)
    }

    /** Saves the kept value again after a failed save. */
    fun retry() {
        commands.trySend(Command.Retry)
    }

    /**
     * Suspends until no value is pending and no save is running; pending values are saved first.
     */
    suspend fun awaitIdle() {
        val done = CompletableDeferred<Unit>()
        if (commands.trySend(Command.Await(done)).isFailure) return
        done.await()
    }

    private suspend fun runWorker() {
        try {
            while (true) handle(commands.receive())
        } finally {
            // Release callers still waiting in awaitIdle, since no worker will answer them now.
            commands.close()
            while (true) {
                val command = commands.tryReceive().getOrNull() ?: break
                if (command is Command.Await) command.done.complete(Unit)
            }
            timer?.cancel()
        }
    }

    private suspend fun handle(command: Command<T>) {
        when (command) {
            is Command.Submit -> accept(command.value)
            is Command.TimerFired -> {
                if (pending && command.generation == timerGeneration) saveLatest()
            }
            Command.Flush -> if (pending) saveLatest()
            Command.Retry -> if (_state.value is SaveState.Failed) saveLatest()
            is Command.Await -> {
                if (pending) saveLatest()
                command.done.complete(Unit)
            }
        }
    }

    private fun accept(value: T) {
        latest = Latest(value)
        pending = true
        startTimer()
        _state.value = SaveState.Pending
    }

    private suspend fun saveLatest() {
        val value = latest ?: return
        pending = false
        cancelTimer()
        _state.value = SaveState.Saving
        val outcome =
            try {
                save(value.value)
            } catch (e: CancellationException) {
                // Cancellation is not a failed save: the value is still unsaved, and the saver
                // stops.
                pending = true
                _state.value = SaveState.Pending
                throw e
            }
        _state.value =
            when (outcome) {
                is Outcome.Success -> SaveState.Saved
                is Outcome.Failure -> SaveState.Failed(outcome.error)
            }
    }

    private fun startTimer() {
        cancelTimer()
        val generation = timerGeneration
        timer = scope.launch {
            delay(debounce)
            commands.trySend(Command.TimerFired(generation))
        }
    }

    // Bumping the generation makes a timer that already queued its event harmless.
    private fun cancelTimer() {
        timer?.cancel()
        timer = null
        timerGeneration++
    }

    private class Latest<V>(val value: V)

    private sealed interface Command<out V> {
        data class Submit<V>(val value: V) : Command<V>

        data object Flush : Command<Nothing>

        data object Retry : Command<Nothing>

        class Await(val done: CompletableDeferred<Unit>) : Command<Nothing>

        data class TimerFired(val generation: Int) : Command<Nothing>
    }
}
