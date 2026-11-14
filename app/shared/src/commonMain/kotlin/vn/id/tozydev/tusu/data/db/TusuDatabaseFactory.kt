package vn.id.tozydev.tusu.data.db

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.data.db.label.DefaultLabelSeedCallback
import vn.id.tozydev.tusu.data.db.migration.ALL_MIGRATIONS

/**
 * Applies the settings every platform shares. There is no destructive-migration fallback, so a
 * missing migration fails loudly instead of wiping user data.
 */
fun buildTusuDatabase(
    builder: RoomDatabase.Builder<TusuDatabase>,
    clock: Clock,
    labelNames: DefaultLabelNamesProvider,
): TusuDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*ALL_MIGRATIONS)
        .addCallback(DefaultLabelSeedCallback(clock) { labelNames.names() })
        .build()
