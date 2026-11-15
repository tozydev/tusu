package vn.id.tozydev.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Non-Android targets cannot find route serializers by reflection, so every route is listed. */
val AppRouteSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Timeline::class)
            subclass(AppRoute.Calendar::class)
            subclass(AppRoute.Media::class)
            subclass(AppRoute.Composer::class)
            subclass(AppRoute.EntryDetail::class)
            subclass(AppRoute.Viewer::class)
        }
    }
}
