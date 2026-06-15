package vn.io.tozyworks.tusu.data.local.db

import androidx.room.TypeConverter
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DatabaseConverters {
    @TypeConverter fun fromUuid(uuid: Uuid): ByteArray = uuid.toByteArray()

    @TypeConverter fun toUuid(bytes: ByteArray): Uuid = Uuid.fromByteArray(bytes)

    @TypeConverter fun fromInstant(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)
}
