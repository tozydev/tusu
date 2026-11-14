package vn.id.tozydev.tusu.data.db.label

import org.jetbrains.compose.resources.getString
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.label_default_achievement
import vn.id.tozydev.tusu.generated.resources.label_default_gratitude
import vn.id.tozydev.tusu.generated.resources.label_default_lesson
import vn.id.tozydev.tusu.generated.resources.label_default_memory
import vn.id.tozydev.tusu.generated.resources.label_default_reflection
import vn.id.tozydev.tusu.generated.resources.label_group_default_kind
import vn.id.tozydev.tusu.generated.resources.label_group_default_people
import vn.id.tozydev.tusu.generated.resources.label_group_default_trips

/** Reads the seeded names in the device language from the composeResources strings. */
class ComposeDefaultLabelNamesProvider : DefaultLabelNamesProvider {
    override suspend fun names(): DefaultLabelNames =
        DefaultLabelNames(
            kindGroup = getString(Res.string.label_group_default_kind),
            peopleGroup = getString(Res.string.label_group_default_people),
            tripsGroup = getString(Res.string.label_group_default_trips),
            achievement = getString(Res.string.label_default_achievement),
            lesson = getString(Res.string.label_default_lesson),
            reflection = getString(Res.string.label_default_reflection),
            memory = getString(Res.string.label_default_memory),
            gratitude = getString(Res.string.label_default_gratitude),
        )
}
