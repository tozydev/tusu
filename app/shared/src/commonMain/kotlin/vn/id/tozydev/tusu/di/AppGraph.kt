package vn.id.tozydev.tusu.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.data.db.EntryDao
import vn.id.tozydev.tusu.data.db.TagDao
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.ui.formatter.DateTimeFormatter

interface AppGraph : ViewModelGraph {
    val appScope: CoroutineScope
    val tusuDatabase: TusuDatabase

    val datastore: DataStore<Preferences>

    val entryDao: EntryDao

    val tagDao: TagDao

    @AppDir val appDir: PlatformFile

    val timeZone: TimeZone
    val clock: Clock
    val dateTimeFormatter: DateTimeFormatter
}
