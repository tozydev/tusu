package vn.io.tozyworks.tusu.util

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate(timeZone: TimeZone) = toLocalDateTime(timeZone).date
