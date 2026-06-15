---
name: kotlin-kotest
description: Comprehensive guide and patterns for Kotlin testing using the Kotest framework. Use when needs to write unit tests, integration tests, design test suites, configure test lifecycle, or manage test data in Kotlin. Features ShouldSpec syntax, AAA (Arrange-Act-Assert) pattern, TDD cycle, assertions, test data builders, and optional mock/stub setup using MockK.
---

# Kotlin Testing with Kotest

This skill provides guidelines and best practices for writing clean, readable, and maintainable unit tests in Kotlin
using **Kotest** and optionally **MockK**.

## General Testing Principles & Best Practices

1. **Test Isolation**: Every test must be completely independent. Never let state from one test leak into another.
   Use lifecycle hooks (`beforeTest`/`afterTest`) to reset databases, mocks, or shared state.
2. **Readability is Paramount**: Tests serve as executable documentation. Write tests so that a developer can
   immediately understand the system's expected behavior without digging into the implementation.
3. **Determinism**: Tests must yield identical results every time they run. Avoid relying on the system clock, random
   number generators, or network calls unless they are mocked/stubbed.
4. **Single Responsibility**: Each test should focus on a single behavior or aspect of the system. Do not group
   unrelated assertions into a single test case.
5. **Test-Driven Development (TDD)**: Build features iteratively in Red-Green-Refactor cycles. Write tests first to
   define the contract and behavior, implement the minimal code to pass, and refactor for cleanliness and quality.
6. **Test Data Management**: Keep test data clean and relevant. Override only details relevant to a specific test case,
   using builders or copy constructors, and prevent leaking test helper code into production.
7. **Test Coverage**: Don't focus only on happy paths. Ensure you cover edge cases, error handling, and boundary
   conditions to build confidence in your code's robustness.

## Prefer Kotest ShouldSpec

Kotest supports multiple testing styles. **ShouldSpec** is preferred because it offers a clean, BDD-style layout that is
highly readable and matches natural language expectations.

### ShouldSpec Layout & Nesting

You can group related tests using `context` blocks to organize your test suite by method, state, or scenario.

```kotlin
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class UserServiceTest : ShouldSpec() {
  init {
    // Lifecycle setup
    beforeTest {
      // Reset shared mock behavior or test database before every single test
    }

    context("User Login") {
      should("allow valid login") {
        // Test implementation
      }

      should("fail login with incorrect password") {
        // Test implementation
      }
    }
  }
}
```

## The AAA (Arrange-Act-Assert) Pattern

Structure every test body into three distinct, separated phases:

1. **Arrange**: Initialize the class under test, set up its inputs, stub dependencies, and prepare test data.
2. **Act**: Invoke the specific method or function being tested. Keep this phase to a single line where possible.
3. **Assert**: Verify that the actual outcome (return value, state change, or mock interaction) matches the expected
   outcome.

Keep these phases clearly separated by blank lines and no labeled comments:

```kotlin
should("calculate total price including tax") {
  val taxCalculator = TaxCalculator(rate = 0.1)
  val cart = Cart(items = listOf(Item(price = 100.0)))

  val total = taxCalculator.calculateTotal(cart)

  total shouldBe 110.0
}
```

## The should-when Naming Pattern

Test descriptions must clearly describe the expected behavior and the trigger conditions. Follow the **should-when**
pattern:

- **Template**: `should [expected behavior] [when condition/trigger]`
- **Examples**:
  - `should return user profile when user ID exists`
  - `should throw Exception when payment is declined`
  - `should not update status when user is inactive`

Avoid vague names like `testLogin` or implementation details in descriptions like
`should call save method on repository`.

## Progressive Disclosure Reference Guides

For detailed implementations, code examples, and API references, check the following files:

- **Project Setup**: See the [setup.md](references/setup.md) reference for JVM and Multiplatform configuration using
  Gradle Version Catalogs.
- **TDD Cycle**: See the [tdd.md](references/tdd.md) reference for a guide on Red-Green-Refactor workflows.
- **Assertions & Matchers**: See the [assertions.md](references/assertions.md) reference for common Kotest matchers (
  equality, collection checks, exceptions, soft assertions).
- **Test Data Management**: See the [test-data.md](references/test-data.md) reference for creating test builders, using
  `.copy()` customization, and companion extensions.
- **Mocking & Verification (Optional)**: See the [mockk.md](references/mockk.md) reference for optional MockK syntax,
  argument matchers, spies, and coroutine support.
- **Framework & Libraries Extensions**: See the [extensions.md](references/extensions.md) reference for integrating Decoroutinator, Spring, Ktor, Testcontainers, custom lifecycle listeners, and mountable extensions.
