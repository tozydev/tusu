package vn.id.tozydev.tusu.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities =
        [
            EntryEntity::class,
            TagEntity::class,
            EntryTagCrossRef::class,
            MediaEntity::class,
        ],
    version = 1,
)
@TypeConverters(DatabaseConverters::class)
abstract class TusuDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    abstract fun tagDao(): TagDao

    abstract fun mediaDao(): MediaDao

    abstract fun entryTagDao(): EntryTagDao

    companion object {
        internal const val DATABASE_NAME = "tusu.sqlite3"

        internal fun create(builder: Builder<TusuDatabase>): TusuDatabase =
            builder.setDriver(BundledSQLiteDriver()).build()
    }
}
