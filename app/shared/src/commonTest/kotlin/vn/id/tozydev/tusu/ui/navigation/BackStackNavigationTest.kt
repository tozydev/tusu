package vn.id.tozydev.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private fun stackOf(vararg routes: AppRoute): MutableList<NavKey> = routes.toMutableList()

class BackStackNavigationTest :
    FunSpec(
        {
            test("selecting Calendar from Timeline puts it on top of Timeline") {
                val stack = stackOf(AppRoute.Timeline)

                stack.selectTab(AppRoute.Calendar) shouldBe true

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Calendar)
            }

            test("switching between Calendar and Media never stacks duplicates") {
                val stack = stackOf(AppRoute.Timeline)

                stack.selectTab(AppRoute.Calendar)
                stack.selectTab(AppRoute.Media)
                stack.selectTab(AppRoute.Calendar)

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Calendar)
            }

            test("selecting Timeline resets the stack to Timeline alone") {
                val stack = stackOf(AppRoute.Timeline, AppRoute.Media)

                stack.selectTab(AppRoute.Timeline) shouldBe true

                stack shouldBe listOf(AppRoute.Timeline)
            }

            test("re-selecting the current tab changes nothing") {
                val onTimeline = stackOf(AppRoute.Timeline)
                val onMedia = stackOf(AppRoute.Timeline, AppRoute.Media)

                onTimeline.selectTab(AppRoute.Timeline) shouldBe false
                onMedia.selectTab(AppRoute.Media) shouldBe false

                onTimeline shouldBe listOf(AppRoute.Timeline)
                onMedia shouldBe listOf(AppRoute.Timeline, AppRoute.Media)
            }

            test("selecting a tab is ignored while the composer is open") {
                val stack = stackOf(AppRoute.Timeline, AppRoute.Composer())

                stack.selectTab(AppRoute.Media) shouldBe false

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Composer())
            }

            test("back from Calendar and Media returns to Timeline") {
                val stack = stackOf(AppRoute.Timeline, AppRoute.Calendar)

                stack.navigateBack() shouldBe true

                stack shouldBe listOf(AppRoute.Timeline)
            }

            test("back from Timeline leaves the app and keeps the stack") {
                val stack = stackOf(AppRoute.Timeline)

                stack.navigateBack() shouldBe false

                stack shouldBe listOf(AppRoute.Timeline)
            }

            test("the composer opens over the current tab and back returns to that tab") {
                val stack = stackOf(AppRoute.Timeline, AppRoute.Media)

                stack.openComposer() shouldBe true
                stack.currentRoute shouldBe AppRoute.Composer()

                stack.navigateBack() shouldBe true
                stack.currentRoute shouldBe AppRoute.Media
                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Media)
            }

            test("the composer can open an existing entry") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline)

                stack.openComposer(id)

                stack.currentRoute shouldBe AppRoute.Composer(id)
            }

            test("a second composer is not stacked on the first") {
                val stack = stackOf(AppRoute.Timeline)

                stack.openComposer()
                stack.openComposer() shouldBe false

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Composer())
            }

            test("the navigation bar shows on tabs only") {
                stackOf(AppRoute.Timeline).showsNavigationBar shouldBe true
                stackOf(AppRoute.Timeline, AppRoute.Calendar).showsNavigationBar shouldBe true
                stackOf(AppRoute.Timeline, AppRoute.Composer()).showsNavigationBar shouldBe false
            }

            test("the current tab is the top route when it is a tab") {
                stackOf(AppRoute.Timeline, AppRoute.Media).currentTab shouldBe AppRoute.Media
                stackOf(AppRoute.Timeline, AppRoute.Composer()).currentTab shouldBe null
            }

            test("an entry opens over a tab") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline, AppRoute.Calendar)

                stack.openEntry(id) shouldBe true

                stack shouldBe
                    listOf(AppRoute.Timeline, AppRoute.Calendar, AppRoute.EntryDetail(id))
            }

            test("an entry is not opened from another entry, the composer or the viewer") {
                val id = Uuid.random()
                val other = Uuid.random()
                val onEntry = stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id))
                val onComposer = stackOf(AppRoute.Timeline, AppRoute.Composer())
                val onViewer =
                    stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id), AppRoute.Viewer(id))

                onEntry.openEntry(other) shouldBe false
                onComposer.openEntry(other) shouldBe false
                onViewer.openEntry(other) shouldBe false

                onEntry shouldBe listOf(AppRoute.Timeline, AppRoute.EntryDetail(id))
                onComposer shouldBe listOf(AppRoute.Timeline, AppRoute.Composer())
                onViewer shouldBe
                    listOf(AppRoute.Timeline, AppRoute.EntryDetail(id), AppRoute.Viewer(id))
            }

            test("the composer can edit from the entry detail and back returns to the entry") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id))

                stack.openComposer(id) shouldBe true
                stack.currentRoute shouldBe AppRoute.Composer(id)

                stack.navigateBack() shouldBe true
                stack.currentRoute shouldBe AppRoute.EntryDetail(id)
            }

            test("the composer is refused over the viewer") {
                val id = Uuid.random()
                val stack =
                    stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id), AppRoute.Viewer(id))

                stack.openComposer(id) shouldBe false

                stack shouldBe
                    listOf(AppRoute.Timeline, AppRoute.EntryDetail(id), AppRoute.Viewer(id))
            }

            test("the viewer opens over a tab at the given index") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline)

                stack.openViewer(id, startIndex = 2) shouldBe true

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Viewer(id, startIndex = 2))
            }

            test("the viewer opens over the entry detail") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id))

                stack.openViewer(id) shouldBe true

                stack shouldBe
                    listOf(AppRoute.Timeline, AppRoute.EntryDetail(id), AppRoute.Viewer(id))
            }

            test("the viewer is refused over the viewer or the composer") {
                val id = Uuid.random()
                val onViewer = stackOf(AppRoute.Timeline, AppRoute.Viewer(id))
                val onComposer = stackOf(AppRoute.Timeline, AppRoute.Composer())

                onViewer.openViewer(id) shouldBe false
                onComposer.openViewer(id) shouldBe false

                onViewer shouldBe listOf(AppRoute.Timeline, AppRoute.Viewer(id))
                onComposer shouldBe listOf(AppRoute.Timeline, AppRoute.Composer())
            }

            test("back from the viewer returns to the entry detail") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id))
                stack.openViewer(id, startIndex = 1)

                stack.navigateBack() shouldBe true

                stack.currentRoute shouldBe AppRoute.EntryDetail(id)
                stack shouldBe listOf(AppRoute.Timeline, AppRoute.EntryDetail(id))
            }

            test("back from the entry detail returns to the tab") {
                val id = Uuid.random()
                val stack = stackOf(AppRoute.Timeline, AppRoute.Media)
                stack.openEntry(id)

                stack.navigateBack() shouldBe true

                stack.currentRoute shouldBe AppRoute.Media
                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Media)
            }

            test("the navigation bar is hidden on the entry detail and the viewer") {
                val id = Uuid.random()

                stackOf(AppRoute.Timeline, AppRoute.EntryDetail(id)).showsNavigationBar shouldBe
                    false
                stackOf(AppRoute.Timeline, AppRoute.Viewer(id)).showsNavigationBar shouldBe false
            }

            test("the label screen opens over the composer, once, and back returns to it") {
                val stack = stackOf(AppRoute.Timeline, AppRoute.Composer())

                stack.openLabels() shouldBe true
                stack.openLabels() shouldBe false

                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Composer(), AppRoute.Labels)
                stack.navigateBack()
                stack shouldBe listOf(AppRoute.Timeline, AppRoute.Composer())
            }
        },
    )
