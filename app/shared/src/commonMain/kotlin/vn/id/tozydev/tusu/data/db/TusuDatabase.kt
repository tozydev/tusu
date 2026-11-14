package vn.id.tozydev.tusu.data.db

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import vn.id.tozydev.tusu.data.db.entry.EntryDao
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.data.db.entry.EntryFtsEntity
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelDao
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelGroupEntity
import vn.id.tozydev.tusu.data.db.media.MediaDao
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.data.db.place.PlaceDao
import vn.id.tozydev.tusu.data.db.place.PlaceEntity

@Database(
    entities =
        [
            EntryEntity::class,
            EntryFtsEntity::class,
            MediaEntity::class,
            LabelGroupEntity::class,
            LabelEntity::class,
            EntryLabelEntity::class,
            PlaceEntity::class,
        ],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(TusuDatabaseConstructor::class)
abstract class TusuDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    abstract fun mediaDao(): MediaDao

    abstract fun labelDao(): LabelDao

    abstract fun placeDao(): PlaceDao
}

/** Room generates the per-platform `actual` that instantiates [TusuDatabase]. */
expect object TusuDatabaseConstructor : RoomDatabaseConstructor<TusuDatabase>
