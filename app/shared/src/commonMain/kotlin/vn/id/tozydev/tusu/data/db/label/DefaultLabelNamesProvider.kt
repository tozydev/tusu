package vn.id.tozydev.tusu.data.db.label

/**
 * Source of the seeded label names. A seam rather than a direct resource lookup, so the JVM graph
 * can run without Compose Resources, whose desktop environment needs a native Skiko library.
 */
fun interface DefaultLabelNamesProvider {
    suspend fun names(): DefaultLabelNames
}
