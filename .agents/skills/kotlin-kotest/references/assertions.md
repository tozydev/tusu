# Kotest Assertions Reference

Comprehensive reference guide for assertions, matchers, and testing helpers in Kotest.

## 1. Core & Common Assertions

Kotest provides extension and infix functions for standard Kotlin types.

### Equality & Nullability

```kotlin
val value = 10
value shouldBe 10          // Equality
value shouldNotBe 0        // Inequality

val name: String? = "Alice"
name.shouldNotBeNull()     // Asserts not null & smart-casts to non-nullable
name shouldBe "Alice"

val address: String? = null
address.shouldBeNull()     // Asserts null
```

### Types & Reference Comparison

| Signature                           | Description                                  |
|-------------------------------------|----------------------------------------------|
| `obj.shouldBeSameInstanceAs(other)` | Compares by reference identity.              |
| `obj.shouldBeTypeOf<T>()`           | Asserts exact type `T` (subclasses fail).    |
| `obj.shouldBeInstanceOf<T>()`       | Asserts type `T` or subclass of `T`.         |
| `obj.shouldBeNull()`                | Asserts reference is null.                   |
| `obj shouldNotBeNull { block }`     | Asserts not null and executes block with it. |

### Comparables & Numbers

```kotlin
// Comparables
value shouldBeLessThan 100
value shouldBeGreaterThanOrEqual 10
value shouldBeBetween (5..15) // Lower and upper bounds inclusive

// Floating Point Tolerance (Required for Float/Double)
val result = 0.1 + 0.2
result shouldBe (0.3 plusOrMinus 0.001) // Recommended tolerance matching
```

### Strings

```kotlin
val str = "Kotest Framework"
str shouldContain "Kotest"
str.shouldStartWith("Ko")
str.shouldEndWith("work")
str.shouldHaveLength(16)
str.shouldMatch("Kotest .*".toRegex())
str shouldBeEqualIgnoringCase "kotest framework"
```

### Collections & Maps

```kotlin
val list = listOf("apple", "banana")
list shouldHaveSize 2
list shouldContain "apple"
list shouldContainExactly listOf("apple", "banana") // Order matters
list shouldContainExactlyInAnyOrder listOf("banana", "apple") // Order irrelevant

val map = mapOf("key" to "value")
map shouldContainKey "key"
map shouldContain ("key" to "value")
```

### Files

```kotlin
val file = File("test.txt")
file.shouldExist()
file.shouldBeAFile()
file.shouldBeEmpty()
file.shouldHaveExtension("txt")
```

## 2. Matchers & Custom Matchers

A `Matcher<T>` performs a specific check and returns a `MatcherResult`.

### Custom Matchers

Implement custom matchers using the `Matcher` SAM interface:

```kotlin
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun haveLength(length: Int) = Matcher<String> { value ->
  MatcherResult(
    value.length == length,
    { "string had length ${value.length} but we expected length $length" },
    { "string should not have length $length" }
  )
}

// Infix Usage
"hello" should haveLength(5)
"hello" shouldNot haveLength(3)

// Extension Variant (Recommended for chaining and autocomplete)
fun String.shouldHaveLength(length: Int): String {
  this should haveLength(length)
  return this
}
"hello".shouldHaveLength(5)
```

### Composed Matchers

Combine multiple matchers logically using `Matcher.all` or `Matcher.any`:

```kotlin
// Logical AND composition
val strongPasswordMatcher = Matcher.all(
  containADigit(),
  contain(Regex("[a-z]")),
  contain(Regex("[A-Z]"))
)
"Password123" should strongPasswordMatcher
```

### Property-Specific Matchers

Assert on properties of complex objects using `havingProperty`:

```kotlin
data class Person(val name: String, val age: Int)

val adultNameMatcher = Matcher.all(
  havingProperty(nameMatcher("John") to Person::name),
  havingProperty(ageMatcher(18) to Person::age)
)
```

## 3. Clues

Clues add contextual messages to failed assertions.

### String Clues & Lazy Clues

Use `withClue` or the `asClue` extension. Use lambda expressions for lazy evaluation to avoid overhead.

```kotlin
// Simple String Clue
withClue("User must have active status") {
  user.status shouldBe Status.ACTIVE
}

// Lazy Clue (Computed only on failure)
withClue({ "User verification failed (id=${user.id})" }) {
  user.name shouldNotBe null
}

// Object as Clue
val response = HttpResponse(404, "Not Found")
response.asClue {
  it.status shouldBe 200 // Outputs response object string representation on failure
}
```

### Nested Clues

Clues can be nested to build hierarchical failure contexts:

```kotlin
{ "User ID: ${user.id}" }.asClue {
  { "Profile settings mismatch" }.asClue {
    user.profile.theme shouldBe Theme.DARK
  }
}
// Failure output:
// User ID: 42
// Profile settings mismatch
// expected: DARK but was: LIGHT
```

## 4. Inspectors

Inspectors assert on elements within collections.

| Inspector                                  | Assertion Criteria                                |
|--------------------------------------------|---------------------------------------------------|
| `forAll { ... }`                           | Asserts every element passes.                     |
| `forNone { ... }`                          | Asserts no element passes.                        |
| `forOne { ... }`                           | Asserts exactly one element passes.               |
| `forAtMostOne { ... }`                     | Asserts 0 or 1 elements pass.                     |
| `forAtLeastOne { ... }` / `forAny { ... }` | Asserts 1 or more elements pass.                  |
| `forAtLeast(k) { ... }`                    | Asserts $k$ or more elements pass.                |
| `forAtMost(k) { ... }`                     | Asserts $k$ or fewer elements pass.               |
| `forSome { ... }`                          | Asserts between 1 and $n-1$ elements pass.        |
| `forExactly(k) { ... }`                    | Asserts exactly $k$ elements pass.                |
| `filterMatching { ... }`                   | Filters collection elements that pass assertions. |

```kotlin
val names = listOf("sam", "gareth", "timothy")
names.forAtLeast(2) {
  it.shouldHaveMinLength(6)
}
```

## 5. Exceptions

Assert expected exception behavior using `shouldThrow` variants.

```kotlin
// Subclass-compatible throw check
val exception = shouldThrow<IllegalArgumentException> {
  user.setAge(-5)
}
exception.message shouldContain "negative"

// Exact type throw check (ignores subclasses)
shouldThrowExactly<FileNotFoundException> {
  openFile("missing.txt")
}

// Any Throwable check
shouldThrowAny {
  riskyOperation()
}

// Assert no Throwable is thrown
shouldNotThrowAny {
  safeOperation()
}
```

## 6. Soft Assertions

Soft assertions group multiple checks, executing all of them and aggregating all failures at the end of the block.

```kotlin
import io.kotest.assertions.assertSoftly

// Standalone block
assertSoftly {
  user.name shouldBe "Alice"
  user.age shouldBe 30
}

// Receiver block
assertSoftly(user) {
  name shouldBe "Alice"
  age shouldBe 30
}
```

### Compatibility & Integrations

- **Non-Kotest Assertions**: Standard JUnit or MockK assertions (e.g. `verify { ... }`) fail immediately and bypass soft
  assertion collection. Wrap them in `shouldNotThrowAnyUnit` to participate in soft assertions:
  ```kotlin
  assertSoftly {
      shouldNotThrowAnyUnit {
          verify(exactly = 1) { service.save(any()) }
      }
      user.status shouldBe Status.SAVED
  }
  ```
- **Soft-Incompatible Matchers**: Assertions that block execution or catch exceptions themselves (e.g., `shouldThrow`,
  `shouldTimeout`, `shouldCompleteWithin`) do not work directly inside `assertSoftly`. Use their soft-compatible
  equivalents:
  - Use `shouldThrowSoftly<T>` instead of `shouldThrow<T>`.
  - Use `failSoftly("Message")` instead of `fail("Message")`.

## 7. Matcher Modules

Kotest provides specialized matcher modules for various ecosystems.

| Module                           | Purpose                                                  | Platform      |
|----------------------------------|----------------------------------------------------------|---------------|
| `kotest-assertions-core`         | Core Kotlin standard library types.                      | Multiplatform |
| `kotest-assertions-arrow`        | Arrow functional programming types (`Option`, `Either`). | Multiplatform |
| `kotest-assertions-json`         | JSON string structure validation.                        | Multiplatform |
| `kotest-assertions-ktor`         | Ktor client/server HTTP verification.                    | Multiplatform |
| `kotest-assertions-kotlinx-time` | Kotlinx-datetime library.                                | Multiplatform |
| `kotest-assertions-yaml`         | YAML file parsing and contents.                          | Multiplatform |
| `kotest-assertions-compiler`     | Kotlin compiler verification and diagnostics.            | JVM           |

### Third-Party Community Matchers

- **Android**: `io.kotest.extensions:kotest-android`
- **Http4k**: `org.http4k:http4k-testing-kotest`
- **Micronaut**: `io.micronaut.test:micronaut-test-kotest`
