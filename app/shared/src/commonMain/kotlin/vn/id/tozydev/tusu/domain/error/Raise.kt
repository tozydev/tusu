package vn.id.tozydev.tusu.domain.error

import kotlin.coroutines.cancellation.CancellationException

/**
 * Scope in which a function can fail with a typed error [E]. Take it as `context(raise: Raise<E>)`
 * and open one with [outcome].
 */
class Raise<in E> @PublishedApi internal constructor() {
    fun raise(error: E): Nothing = throw RaisedError(this, error)
}

/**
 * Carries a raised error up to the [outcome] block that owns [scope]. It is a
 * [CancellationException] so that coroutine machinery, and handlers that rethrow cancellation, let
 * it through.
 */
@PublishedApi
internal class RaisedError(val scope: Raise<*>, val error: Any?) :
    CancellationException("Typed error raised outside of its outcome block")

/** Runs [block], turning a [raise] inside it into [Outcome.Failure]. */
inline fun <T, E> outcome(block: context(Raise<E>) () -> T): Outcome<T, E> {
    val scope = Raise<E>()
    return try {
        Outcome.Success(context(scope) { block() })
    } catch (e: RaisedError) {
        if (e.scope !== scope) throw e
        @Suppress("UNCHECKED_CAST") Outcome.Failure(e.error as E)
    }
}

context(raise: Raise<E>)
fun <E> raise(error: E): Nothing = raise.raise(error)

/** Returns the value of a success, or raises the error of a failure. */
context(raise: Raise<E>)
fun <T, E> Outcome<T, E>.bind(): T =
    when (this) {
        is Outcome.Success -> value
        is Outcome.Failure -> raise.raise(error)
    }

context(raise: Raise<E>)
inline fun <E> ensure(condition: Boolean, error: () -> E) {
    if (!condition) raise.raise(error())
}

context(raise: Raise<E>)
inline fun <T : Any, E> ensureNotNull(value: T?, error: () -> E): T = value ?: raise.raise(error())
