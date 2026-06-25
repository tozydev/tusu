package vn.id.tozydev.tusu.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<TusuDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(TusuDatabase.DATABASE_NAME)
    return Room.databaseBuilder(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
