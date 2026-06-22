package vn.io.tozyworks.tusu.util

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate(timeZone: TimeZone) = toLocalDateTime(timeZone).date

fun Instant.withTime(timeZone: TimeZone, localTime: LocalTime) =
    LocalDateTime(toLocalDate(timeZone), localTime).toInstant(timeZone)

fun Instant.withDate(timeZone: TimeZone, localDate: LocalDate) =
    LocalDateTime(localDate, toLocalDateTime(timeZone).time).toInstant(timeZone)
