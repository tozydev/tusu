package vn.id.tozydev.tusu

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.data.seedTestData
import vn.id.tozydev.tusu.di.AndroidAppGraph
import vn.id.tozydev.tusu.ui.App

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey
class MainActivity(private val appGraph: AndroidAppGraph) : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            if (BuildConfig.DEBUG) {
                seedTestData(appGraph.tusuDatabase)
            }
        }

        setContent {
            App(appGraph)
        }
    }
}
