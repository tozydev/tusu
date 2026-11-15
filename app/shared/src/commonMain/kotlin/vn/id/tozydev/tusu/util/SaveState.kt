package vn.id.tozydev.tusu.util

/** What the save indicator shows. */
sealed interface SaveState<out E> {
    /** Nothing has been submitted since the saver was created. */
    data object Idle : SaveState<Nothing>

    /** A value waits for the debounce or a flush. */
    data object Pending : SaveState<Nothing>

    /** The save function is running. */
    data object Saving : SaveState<Nothing>

    /** The latest submitted value is stored. */
    data object Saved : SaveState<Nothing>

    /** The latest save failed; the value is kept so it can be retried. */
    data class Failed<out E>(val error: E) : SaveState<E>
}
