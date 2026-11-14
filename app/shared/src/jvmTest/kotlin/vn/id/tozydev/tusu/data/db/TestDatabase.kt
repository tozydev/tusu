package vn.id.tozydev.tusu.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** Fresh, empty in-memory database on the bundled SQLite driver, so FTS5 is available. */
fun createTestDatabase(): TusuDatabase =
    Room.inMemoryDatabaseBuilder<TusuDatabase>(factory = TusuDatabaseConstructor::initialize)
        .setDriver(BundledSQLiteDriver())
        .build()

/** Runs [block] against a fresh database and closes it afterwards, even when [block] fails. */
suspend fun <R> withTestDatabase(block: suspend (TusuDatabase) -> R): R {
    val db = createTestDatabase()
    try {
        return block(db)
    } finally {
        db.close()
    }
}
