package vn.id.tozydev.tusu.domain.error

sealed interface RichTextError {
    /** The stored body is not valid rich text JSON. */
    data object MalformedJson : RichTextError

    /** The body was written by a newer app version, so this version cannot read it. */
    data class UnsupportedVersion(val found: Int, val supported: Int) : RichTextError
}
