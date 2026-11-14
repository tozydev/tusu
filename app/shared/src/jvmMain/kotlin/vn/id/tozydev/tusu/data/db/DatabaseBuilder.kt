package vn.id.tozydev.tusu.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.io.files.Path

/** Opens the database file at [file]; the JVM has no platform database folder to defer to. */
fun tusuDatabaseBuilder(file: Path): RoomDatabase.Builder<TusuDatabase> =
    Room.databaseBuilder<TusuDatabase>(
        name = file.toString(),
        factory = TusuDatabaseConstructor::initialize,
    )
