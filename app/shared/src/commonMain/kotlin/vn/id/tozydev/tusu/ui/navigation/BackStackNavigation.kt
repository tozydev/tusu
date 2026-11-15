package vn.id.tozydev.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.uuid.Uuid

// The back stack is [Timeline, <other tab>?, Composer?]: Timeline is always the root so that back
// from any tab lands there, and the other tabs replace each other instead of piling up.

val List<NavKey>.currentRoute: AppRoute?
    get() = lastOrNull() as? AppRoute

/** The selected tab, or null while a non-tab destination such as the composer is on top. */
val List<NavKey>.currentTab: AppRoute.TopLevel?
    get() = currentRoute as? AppRoute.TopLevel

val List<NavKey>.showsNavigationBar: Boolean
    get() = currentTab != null

/** Returns whether the stack changed; re-selecting the tab or selecting off a tab is a no-op. */
fun MutableList<NavKey>.selectTab(tab: AppRoute.TopLevel): Boolean {
    val current = currentTab ?: return false
    if (current == tab) return false
    clear()
    add(AppRoute.Timeline)
    if (tab != AppRoute.Timeline) add(tab)
    return true
}

/** Opens the composer over the current tab; returns false when it is already open. */
fun MutableList<NavKey>.openComposer(entryId: Uuid? = null): Boolean {
    if (currentTab == null) return false
    add(AppRoute.Composer(entryId))
    return true
}

/** Pops the top route; returns false when only the root is left, i.e. the app should close. */
fun MutableList<NavKey>.navigateBack(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}
