package vn.id.tozydev.tusu.ui.navigation

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_calendar_24px
import vn.id.tozydev.tusu.generated.resources.ic_media_24px
import vn.id.tozydev.tusu.generated.resources.ic_timeline_24px
import vn.id.tozydev.tusu.generated.resources.nav_calendar
import vn.id.tozydev.tusu.generated.resources.nav_media
import vn.id.tozydev.tusu.generated.resources.nav_timeline

/** The tabs of the floating navigation bar, in display order. */
internal enum class TopLevelTab(
    val route: AppRoute.TopLevel,
    val icon: DrawableResource,
    val label: StringResource,
) {
    Timeline(AppRoute.Timeline, Res.drawable.ic_timeline_24px, Res.string.nav_timeline),
    Calendar(AppRoute.Calendar, Res.drawable.ic_calendar_24px, Res.string.nav_calendar),
    Media(AppRoute.Media, Res.drawable.ic_media_24px, Res.string.nav_media),
}
