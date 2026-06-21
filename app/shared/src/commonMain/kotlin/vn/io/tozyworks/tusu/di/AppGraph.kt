package vn.io.tozyworks.tusu.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import vn.io.tozyworks.tusu.data.db.EntryDao
import vn.io.tozyworks.tusu.data.db.TagDao
import vn.io.tozyworks.tusu.data.db.TusuDatabase
import vn.io.tozyworks.tusu.ui.formatter.DateTimeFormatter

interface AppGraph : ViewModelGraph {
    val tusuDatabase: TusuDatabase

    val entryDao: EntryDao

    val tagDao: TagDao

    @AppDir val appDir: PlatformFile

    val timeZone: TimeZone
    val clock: Clock
    val dateTimeFormatter: DateTimeFormatter
}
