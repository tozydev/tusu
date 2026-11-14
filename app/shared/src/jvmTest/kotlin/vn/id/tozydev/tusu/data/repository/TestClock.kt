package vn.id.tozydev.tusu.data.repository

import kotlin.time.Clock
import kotlin.time.Instant

/** Clock that tests move by hand, so timestamps are exact. */
internal class TestClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}
