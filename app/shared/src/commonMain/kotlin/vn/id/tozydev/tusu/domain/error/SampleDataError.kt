package vn.id.tozydev.tusu.domain.error

/**
 * Why the debug sample data could not be written; each case wraps the error of the failing step.
 */
sealed interface SampleDataError {
    data class Entry(val error: EntryError) : SampleDataError

    data class Media(val error: MediaError) : SampleDataError

    data class Label(val error: LabelError) : SampleDataError

    /** The database could not be read to find out whether it is empty. */
    data object StorageFailed : SampleDataError
}
