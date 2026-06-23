package vn.io.tozydev.tusu

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import vn.io.tozydev.tusu.di.AndroidAppGraph
import vn.io.tozydev.tusu.ui.App

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey
class MainActivity(private val appGraph: AndroidAppGraph) : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appGraph)
        }
    }
}
