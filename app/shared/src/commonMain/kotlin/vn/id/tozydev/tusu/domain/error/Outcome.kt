package vn.id.tozydev.tusu.domain.error

/** Result of an operation that can fail in an expected way: a value or a typed error. */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>

    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, E, R> Outcome<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R =
    when (this) {
        is Outcome.Success -> onSuccess(value)
        is Outcome.Failure -> onFailure(error)
    }

inline fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> =
    when (this) {
        is Outcome.Success -> Outcome.Success(transform(value))
        is Outcome.Failure -> this
    }

inline fun <T, E, R> Outcome<T, E>.mapError(transform: (E) -> R): Outcome<T, R> =
    when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.Failure(transform(error))
    }

fun <T, E> Outcome<T, E>.getOrNull(): T? = (this as? Outcome.Success)?.value

fun <T, E> Outcome<T, E>.errorOrNull(): E? = (this as? Outcome.Failure)?.error

inline fun <T, E> Outcome<T, E>.onSuccess(action: (T) -> Unit): Outcome<T, E> {
    if (this is Outcome.Success) action(value)
    return this
}

inline fun <T, E> Outcome<T, E>.onFailure(action: (E) -> Unit): Outcome<T, E> {
    if (this is Outcome.Failure) action(error)
    return this
}
