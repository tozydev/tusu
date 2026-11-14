package vn.id.tozydev.tusu.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

/** Android keeps the database in the platform's own databases folder, so backups include it. */
fun tusuDatabaseBuilder(context: Context): RoomDatabase.Builder<TusuDatabase> =
    Room.databaseBuilder<TusuDatabase>(
        context = context,
        name = DATABASE_FILE_NAME,
        factory = TusuDatabaseConstructor::initialize,
    )

private const val DATABASE_FILE_NAME = "tusu.db"
