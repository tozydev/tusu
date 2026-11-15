package vn.id.tozydev.tusu

import android.content.Context
import vn.id.tozydev.tusu.di.AndroidAppGraph

/** Release builds have no debug tools; the debug variant fills the journal with sample data. */
@Suppress("UNUSED_PARAMETER")
object DebugTools {
    fun onAppCreated(context: Context, appGraph: AndroidAppGraph) = Unit
}
