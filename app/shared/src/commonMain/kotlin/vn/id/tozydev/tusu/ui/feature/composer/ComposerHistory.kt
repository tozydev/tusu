package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.EntryLocation

/** What Undo brings back: everything in the composer the user edits, except the time. */
data class ComposerSnapshot(
    val body: String,
    val emoji: String?,
    val labelIds: Set<Uuid>,
    val location: EntryLocation?,
    /** Live photos and voice notes. */
    val mediaIds: Set<Uuid>,
)

/** Undo and redo stacks of one composer session; it ends with the session. */
class ComposerHistory(private val limit: Int = DEFAULT_LIMIT) {
    private val undoStack = ArrayDeque<ComposerSnapshot>()
    private val redoStack = ArrayDeque<ComposerSnapshot>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    /** Starts a new step from [before], the state just before the change. */
    fun record(before: ComposerSnapshot) {
        undoStack.addLast(before)
        if (undoStack.size > limit) undoStack.removeFirst()
        redoStack.clear()
    }

    /** The state to go back to, or null; [current] becomes the redo step. */
    fun undo(current: ComposerSnapshot): ComposerSnapshot? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(current)
        return previous
    }

    /** The state to go forward to, or null; [current] becomes the undo step. */
    fun redo(current: ComposerSnapshot): ComposerSnapshot? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(current)
        return next
    }

    private companion object {
        const val DEFAULT_LIMIT = 100
    }
}
