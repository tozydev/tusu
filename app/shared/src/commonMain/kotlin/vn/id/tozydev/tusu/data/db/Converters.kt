package vn.id.tozydev.tusu.data.db

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Room has no built-in converter for `kotlin.uuid.Uuid` (only `java.util.UUID`), so ids are stored
 * as their canonical 36-character text. Enums need no converter: Room stores them by name.
 */
object Converters {
    @ColumnTypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilliseconds()

    @ColumnTypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

    @ColumnTypeConverter fun localDateToIso(value: LocalDate?): String? = value?.toString()

    @ColumnTypeConverter
    fun isoToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @ColumnTypeConverter fun uuidToText(value: Uuid?): String? = value?.toString()

    @ColumnTypeConverter fun textToUuid(value: String?): Uuid? = value?.let(Uuid::parse)

    /** An empty waveform is stored as the empty string so the column stays NOT NULL. */
    @ColumnTypeConverter
    fun waveformToText(value: List<Float>?): String? = value?.joinToString(separator = ",")

    @ColumnTypeConverter
    fun textToWaveform(value: String?): List<Float>? = value?.let {
        if (it.isEmpty()) emptyList() else it.split(",").map(String::toFloat)
    }
}
