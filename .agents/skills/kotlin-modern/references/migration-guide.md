# K2 Compiler Migration Guide

The Kotlin K2 compiler, default from Kotlin 2.0.0, is a complete rewrite of the frontend compiler architecture. It
improves type inference, compilation speed, and is the sole compiler frontend in Kotlin 2.4.0+ (K1 support and language
version 1.9 or older are removed).

## Key Behavior Changes

### 1. Immediate Initialization of Open Properties

All `open` properties with backing fields must be immediately initialized; otherwise, a compilation error is triggered.
Previously, this only applied to `open var`, but now extends to `open val`.

- **Bad (error in 2.0):**
  ```kotlin
  open class Base {
      open val a: Int
      init { this.a = 1 } // Error: open val must have initializer
  }
  ```
- **Best Practice:** Initialize open properties immediately, or make them `final`, or use private backing properties.

### 2. Deprecated Synthetic Setters on Projected Receivers

Using a synthetic setter property on a Java projected receiver type triggers a compile-time error.

- **Bad (error in 2.0):**
  ```kotlin
  fun example(starProjected: Container<*>, str: String) {
      starProjected.foo = str // Error: synthetic setter cannot resolve conflicting projected types
  }
  ```
- **Best Practice:** Avoid type projections or call raw methods explicitly.

### 3. Forbidden Usages of Inaccessible Generic Types

You cannot call or declare function literals with inaccessible generic types, preventing silent runtime failures. Check
build configurations if warnings or errors arise.

### 4. Resolution Order: Properties vs Java Fields

If subclasses and superclasses duplicate names between Kotlin properties and Java fields, the subclass always takes
precedence.

- **Subclass (Kotlin) takes precedence:**
  ```kotlin
  class Derived : JavaBase() {
      val a = "derived" // Derived.a is resolved instead of JavaBase.a
  }
  ```

### 5. Null Safety for Java Primitive Arrays

Kotlin K2 preserves `TYPE_USE` nullability annotations (`@Nullable`, `@NotNull`) imported from Java primitive arrays:

- Passing `null` to a non-nullable Java array or accessing a `@Nullable` Java array without null checks fails
  compilation.
- Unnecessary safe calls on `@NotNull` arrays emit warnings.

### 6. Abstract Members in Expected Classes

For multiplatform code, expected non-abstract classes inheriting abstract functions must explicitly override them so the
compiler knows they are implemented.

- **Example:**
  ```kotlin
  // Common
  expect open class PlatformFileSystem() : FileSystem {
      expect override fun listFiles() // Explicit override required in 2.0
  }
  ```

## Performance & Build Settings

- **Speed:** Clean build times reduce up to 94%, with up to 376% faster code analysis phase.
- **Build Reports:** Enable reports in `gradle.properties` to evaluate build performance:
  ```properties
  kotlin.build.report.output=file
  ```

## Rolling Back (Kotlin 2.0–2.3 Only)

To rollback to the old K1 compiler in Kotlin versions 2.0 through 2.3:

- Set `-language-version 1.9` in compiler options.
- **Note:** Starting in Kotlin 2.4.0, rollback is no longer supported.
