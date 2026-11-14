package vn.id.tozydev.tusu.domain.model

/** How many labels of one group an entry can carry. */
enum class LabelSelection {
    MULTI,
    /** Picking a label replaces the entry's previous label from the same group. */
    SINGLE,
}
