package vn.id.tozydev.tusu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.di.AndroidAppGraph
import vn.id.tozydev.tusu.domain.error.Outcome

private val logger = KotlinLogging.logger {}

/** Developer conveniences that exist only in debug builds; the release variant is a no-op. */
object DebugTools {
    /** Fills an empty journal with sample entries so the timeline has something to scroll. */
    fun onAppCreated(context: Context, appGraph: AndroidAppGraph) {
        appGraph.appScope.launch {
            when (val result = appGraph.sampleDataSeeder.seedIfEmpty()) {
                is Outcome.Success ->
                    logger.info { "Sample data: ${result.value.entries} entries added" }
                is Outcome.Failure -> {
                    // A silent failure would leave an empty timeline with no hint why.
                    logger.error { "Could not seed the sample data: ${result.error}" }
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                                context,
                                "Sample data failed: ${result.error::class.simpleName}",
                                Toast.LENGTH_LONG,
                            )
                            .show()
                    }
                }
            }
        }
    }
}
