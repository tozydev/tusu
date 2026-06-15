# Mocking with MockK

MockK is the premier mocking library built specifically for Kotlin (JVM). It provides a powerful and intuitive API for
creating mocks, stubs, and spies, making it easy to write unit tests that isolate dependencies and verify interactions.

## 1. Creating Mocks & Stubbing

By default, MockK uses **Strict Mocking** — calling an unstubbed method on a mock will throw a `MockKException`. Create
mock instances using `mockk<T>()` and define behavior using `every`:

```kotlin
import io.mockk.*

val repository = mockk<UserRepository>()

// Return a specific value
every { repository.findById(1L) } returns User(1L, "Alice")

// Throw an exception
every { repository.findById(-1L) } throws IllegalArgumentException("Invalid ID")

// For Unit-returning functions ONLY, use "just Runs"
every { repository.delete(any()) } just Runs

```

### Relaxed Mocks

If you want to avoid stubbing every single method, you can relax the constraints:

- `relaxed = true`: Automatically returns default values (e.g., empty strings, zero, empty lists, empty objects).
- `relaxUnitFun = true`: Only relaxes methods returning `Unit`.

```kotlin
val relaxedMock = mockk<UserRepository>(relaxed = true)
val unitRelaxedMock = mockk<UserRepository>(relaxUnitFun = true)

```

## 2. Argument Matchers

Use matchers to make stubbing and verification more flexible:

- `any()`: Matches any value. *Note: Use explicit type `any<T>()` if Kotlin cannot infer the type.*
- `eq(value)`: Matches a specific value (implicit when no matcher is specified).
- `match { predicate }`: Matches if the boolean predicate returns true.
- `isNull()` / `isNull(inverse = true)`: Matches null or non-null values.

```kotlin
every { repository.save(any()) } returns User(2L, "Saved User")
// Explicit type invocation when needed: any<Long>()
every { repository.deleteById(any<Long>()) } just Runs

every { repository.findByName(match { it.startsWith("Admin") }) } returns User(0L, "Admin")

```

## 3. Verification

Verify that interactions occurred using `verify`.

```kotlin
// Verify a call occurred
verify { repository.findById(1L) }

// Verify call counts
verify(exactly = 2) { repository.findById(any()) }
verify(atLeast = 1) { repository.delete(any()) }
verify(exactly = 0) { repository.save(any()) } // Never called

// Verify exact call order
verifyOrder {
  repository.findById(1L)
  repository.delete(1L)
}

```

> ⚠️ **Best Practice Warning on `confirmVerified**`:
> Use `confirmVerified(mock)` sparingly. Overusing it forces your tests to know about every single internal interaction,
> making the codebase brittle and hard to refactor. If you must use it, pair it with `excludeRecords` to ignore
> irrelevant
> calls.

```kotlin
// Optional: Ignore setup or logging calls before verification check
excludeRecords { repository.isAvailable() }
confirmVerified(repository)

```

## 4. Spying (Partial Mocking)

A Spy wraps a real object. It executes the actual logic for all methods unless explicitly stubbed.

```kotlin
val realService = UserServiceImpl()
val spy = spyk(realService)

// Override behavior
every { spy.getSystemStatus() } returns "MOCKED_STATUS"

// WARNING: In MockK, 'every { spy.realMethod() }' WILL execute the real method once during stubbing.
// Ensure 'getSystemStatus()' does not have critical side-effects (like network calls) during test setup.
```

## 5. Coroutines & Suspending Functions

To test asynchronous code using Kotlin Coroutines, replace `every/verify` with their coroutine equivalents (
`coEvery/coVerify`) inside a proper coroutine test scope (e.g., `runTest`, Kotest test block, etc.):

```kotlin
interface SuspendService {
  suspend fun fetchData(id: String): String
}

val service = mockk<SuspendService>()

// Stub suspending function
coEvery { service.fetchData(any()) } returns "Data"

// Verify suspending function call
coVerify { service.fetchData("123") }

```

## 6. Best Practices

- Mock only external dependencies (e.g., repositories, APIs) and avoid mocking the class under test.
- Use stub implementations or test doubles for complex behavior instead of overusing mocks.
- Avoid mock verification that couples tests to internal implementation details. Focus on verifying observable behavior,
  side effects, or outputs rather than internal interactions.
