# Kotlin Standard Library Guide

This guide reviews standard library enhancements introduced between Kotlin 2.1.0 and Kotlin 2.4.0.

## 1. Physical Time Tracking (`Clock` and `Instant`)

Kotlin 2.1.0 introduced `kotlin.time.Instant` and `kotlin.time.Clock` to the Kotlin standard library, serving as the
core solution for tracking physical time across JVM, JS, Native, and Wasm targets.

**Stability:** These APIs became fully stable in **Kotlin 2.3.0**. They replace the need for the external
`kotlinx-datetime` library when performing simple timestamping and interval tracking.

### Getting the Current Time

You can obtain the current physical timestamp using `Clock.System.now()`:

```kotlin
import kotlin.time.Clock
import kotlin.time.Instant

fun main() {
  // Get current Instant
  val now: Instant = Clock.System.now()
  println("Current timestamp: $now") // e.g., 2026-06-11T11:15:00Z

  // Parsing an Instant
  val parsed: Instant = Instant.parse("2026-06-11T11:15:00Z")

  // Check if an instant is after another
  if (now > parsed) {
    println("We are in the future!")
  }
}
```

### `kotlin.time` vs `kotlinx-datetime`

- **Use `kotlin.time`:** For core physical time tracking, intervals, benchmarks, and simple timestamps (`Instant`,
  `Clock`, `Duration`). It is built directly into the standard library.
- **Use `kotlinx-datetime`:** For civil time operations (time zones, dates like `LocalDate`, calendar calculations,
  formatting). Note that `kotlinx-datetime` (from version 0.7.0) builds on top of the standard library's
  `kotlin.time.Instant` and `kotlin.time.Clock`.

## 2. UUID API (Stable in 2.4.0)

The `Uuid` API is stable in the common Kotlin standard library, simplifying UUID usage across KMP without
platform-specific dependencies:

```kotlin
import kotlin.uuid.Uuid

val randomUuid = Uuid.random()
println(randomUuid.toString())

val parsedUuid = Uuid.parse("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
```

### Generating UUID Version 4 (Random)

You can also explicitly use the V4 generation algorithm (functionally equivalent to `Uuid.random()`):

```kotlin
import kotlin.uuid.Uuid

val v4Uuid = Uuid.generateV4()
```

### Generating UUID Version 7 (Time-Ordered)

*Note: Generating UUID v7 is currently experimental and requires the `@OptIn(ExperimentalUuidApi::class)` annotation.*

UUID v7 is prefixed with a 48-bit Unix timestamp in milliseconds, followed by random bits. It guarantees **strict
monotonicity** within the application's lifetime (sequential IDs are guaranteed to be sorted chronologically).

```kotlin
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
val v7Uuid = Uuid.generateV7()
```

### Generating Non-Monotonic UUID Version 7 at a Specific Time

*Note: Generating non-monotonic UUID v7 is currently experimental and requires the `@OptIn(ExperimentalUuidApi::class)`
annotation.*

If you need to generate a UUID v7 for a specific point in time (past or future), use
`generateV7NonMonotonicAt(timestamp: Instant)`. This function **does not provide monotonicity guarantees**.

```kotlin
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.time.Instant

@OptIn(ExperimentalUuidApi::class)
fun createPastId() {
  val pastInstant = Instant.parse("2026-01-01T00:00:00Z")
  val pastUuid = Uuid.generateV7NonMonotonicAt(pastInstant)
}
```

## 3. HexFormat API (Stable in 2.2.0)

The `HexFormat` class provides a unified, customizable API for formatting and parsing hexadecimal values.

```kotlin
import kotlin.text.HexFormat

val format = HexFormat {
  upperCase = true
  bytes {
    byteSeparator = ":"
  }
}

val bytes = byteArrayOf(10, 20, 30)
println(bytes.toHexString(format)) // "0A:14:1E"
```

## 4. Base64 API (Stable in 2.2.0)

The Base64 encoding/decoding API is stable in the common Kotlin standard library:

```kotlin
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun encodeData() {
  val encoded = Base64.encode("Hello Kotlin".encodeToByteArray())
  println(encoded) // "SGVsbG8gS290bGlu"
}
```

## 5. High-Precision Time Tracking (Stable in 2.3.0)

Introduces stable high-precision monotonic time tracking and duration measurement APIs:

```kotlin
import kotlin.time.TimeSource

val timeSource = TimeSource.Monotonic
val mark = timeSource.markNow()

// Do work...

val elapsed = mark.elapsedNow()
println("Work took: $elapsed")
```
