# Agent-Optimized Test Driven Development (TDD)

## The RED-GREEN-REFACTOR Cycle

The development cycle runs as: **Red (write failing test) → Green (write minimal code to pass) → Refactor (improve code
while keeping tests green) → Repeat (continue to next requirement)**.

- **Red**: Start by writing a test that defines the expected behavior of the feature you want to implement. This test
  should fail because the functionality is not yet implemented.
- **Green**: Write the minimum amount of code necessary to make the test pass. Focus on functionality, not on code
  quality or design at this stage.
- **Refactor**: Once the test is passing, review and improve the code. Refactor for readability, maintainability, and
  performance while ensuring that all tests still pass. This step is crucial for maintaining a clean codebase and
  preventing technical debt.
- **Repeat**: After refactoring, move on to the next requirement or feature and repeat the cycle. This iterative process
  allows you to build your application incrementally, ensuring that each piece of functionality is well-tested and
  integrated smoothly.

## Step-by-Step Example

We will design a discount calculator feature using these 6 steps.

### 1. Define interface/signature/contract

Declare the signature with a dummy return value so the compiler is satisfied.

```kotlin
// src/main/kotlin/DiscountCalculator.kt
class DiscountCalculator {
  fun calculate(price: Double, isVIP: Boolean): Double {
    return 0.0 // Dummy value
  }
}
```

### 2. Write failing tests

Write test cases using the `DiscountCalculator`.

```kotlin
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DiscountCalculatorTest : ShouldSpec() {
  init {
    lateinit var calculator: DiscountCalculator

    beforeTest {
      calculator = DiscountCalculator()
    }

    should("apply 10% discount for VIP users") {
      calculator.calculate(100.0, isVIP = true) shouldBe 90.0
    }

    should("not apply discount for non-VIP users") {
      calculator.calculate(100.0, isVIP = false) shouldBe 100.0
    }
  }
}
```

### 3. Run tests – verify FAIL

- **Command**: `./gradlew test` (or equivalent test runner)
- **Result**: Compiles successfully, but the test fails: `Expected 90.0 but was 0.0` and `Expected 100.0 but was 0.0`.

### 4. Implement minimal code (GREEN)

Implement the minimum code to pass.

```kotlin
// src/main/kotlin/DiscountCalculator.kt
class DiscountCalculator {
  fun calculate(price: Double, isVIP: Boolean): Double {
    return if (isVIP) price * 0.9 else price
  }
}
```

### 5. Run tests – verify PASS

- **Command**: `./gradlew test`
- **Result**: Tests pass (Green).

### 6. Refactor if needed, verify tests still pass

Clean up code (e.g., extracting the discount rate to a constant).

```kotlin
// src/main/kotlin/DiscountCalculator.kt
class DiscountCalculator {
  companion object {
    private const val VIP_DISCOUNT_RATE = 0.9
  }

  fun calculate(price: Double, isVIP: Boolean): Double {
    return if (isVIP) price * VIP_DISCOUNT_RATE else price
  }
}
```

- **Verify**: Re-run `./gradlew test` and confirm it is still passing (Green). Repeat the process for the next
  requirement.
