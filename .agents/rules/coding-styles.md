---
trigger: always_on
---

# Coding Conventions

## Kotlin Source Code Organization

- **File Naming:** * Single class/interface: Name must match the class name (`ClassName.kt`).
    * Multiple classes/Top-level declarations: Use **UpperCamelCase** describing the content (e.g.,
      `ProcessDeclarations.kt`). Avoid generic suffixes like `Util`.
- **Multiplatform Suffixes:** Platform-specific source sets require a suffix (e.g.,
  `Platform.jvm.kt`, `Platform.ios.kt`). Common source sets do not (`Platform.kt`).
- **File Grouping:** Multiple declarations (classes, functions, properties) in one file are
  encouraged if semantically related and under a few hundred lines. Put general extension functions
  in the same file as their class.
- **Class Member Order:**
    1. Property declarations & initializer blocks
    2. Secondary constructors
    3. Method declarations (Group by logic/flow, NOT alphabetically or by visibility)
    4. Companion object
    5. Nested classes (Place next to where they are used, or at the very end if used externally)
- **Implementations & Overloads:**
    * **Interfaces:** Maintain the exact member order defined in the interface.
    * **Overloads:** Always place overloaded methods adjacent to each other.

## Naming Rules

- **Packages**: lowercase, no underscores (`org.example.project`).
- **Classes / Objects**: UpperCamelCase (`DeclarationProcessor`).
- **Functions / Properties / Variables**: lowerCamelCase (`processDeclarations`,
  `declarationCount`).
- **Factory Functions**: may use the same name as the returned type (`fun Foo(): Foo`).
- **Constants** (`const val`, immutable top-level/object `val`): SCREAMING_SNAKE_CASE (`MAX_COUNT`).
- **Mutable or behavioral properties**: lowerCamelCase (`mutableCollection`).
- **Singleton references**: may use UpperCamelCase (`PersonComparator`).
- **Enum Constants**: either SCREAMING_SNAKE_CASE or UpperCamelCase.
- **Backing Properties**: private implementation fields use `_` prefix (`_elementList`).
- **Naming Semantics**:
    - Classes → nouns (`PersonReader`, `User`).
    - Functions → verbs (`readPersons`, `close`).
    - Avoid vague names (`Manager`, `Wrapper`).
- **Acronyms**:
    - 2 letters → uppercase (`IOStream`).
    - 3+ letters → capitalize first letter only (`HttpClient`, `XmlFormatter`).

## Formatting

- Use **ktfmt** for code format enforcement.
- Prefer expression bodies for single-expression functions:
  ```kotlin
  fun foo() = 1
  ```
- Simple getters may be one-line:
  ```kotlin
  val isEmpty: Boolean get() = size == 0
  ```
- Complex getters/setters should be multiline.
- Always use braces for multiline `if`/`when` conditions.

## Documentation Comments

- Use **KDoc** (`/** ... */`) for public APIs and non-obvious behavior.
- Short comments may be written on a single line:
  ```kotlin
  /** Short description. */
  ```
- Avoid `@param` and `@return` tags unless the explanation is lengthy or cannot naturally fit into
  the main description.
- Prefer referencing parameters directly using links:
  ```kotlin
  /** Returns the absolute value of the given [number]. */
  fun abs(number: Int): Int
  ```
- Focus documentation on **purpose, behavior, side effects, constraints, and usage**, not obvious
  implementation details.

## Idiomatic Kotlin

### Immutability

- Prefer `val` over `var`.
- Expose immutable collection types (`List`, `Set`, `Map`, `Collection`).
- Prefer immutable factories (`listOf`, `setOf`, `mapOf`) over mutable ones.

### Default Parameters

- Prefer default parameter values over overloads.
  ```kotlin
  fun foo(value: String = "default")
  ```

### Type Aliases

- Create `typealias` for frequently used complex types or function types.
- Use `import ... as ...` instead of aliases solely for name collision avoidance.

### Lambdas

- Use `it` for short, simple lambdas.
- Explicitly name parameters in nested or complex lambdas.
- Avoid multiple labeled returns; prefer a single exit point.

### Named Arguments

- Use named arguments when:
    - Multiple parameters share the same type.
    - Parameters are `Boolean`.
    - Meaning is not obvious from context.

### Conditionals

- Prefer expression forms:
  ```kotlin
  return if (condition) success() else failure()
  ```
- Use `if` for two-way branches.
- Use `when` for three or more cases.
- Use parentheses in `when` guard conditions.
- For nullable booleans, use:
  ```kotlin
  if (value == true)
  if (value == false)
  ```

### Loops & Collections

- Prefer collection operations (`map`, `filter`, `associate`, etc.) over manual loops.
- Prefer `for` loops over `forEach` unless chaining or handling nullable receivers.
- Consider performance when chaining many collection operations.

### Ranges

- Prefer open-ended ranges:
  ```kotlin
  for (i in 0..<n)
  ```

### Strings

- Prefer string templates:
  ```kotlin
  "Hello $name"
  ```
- Prefer multiline strings over `\n`.
- Use `trimIndent()` or `trimMargin()` for formatting.

### Functions vs Properties

Use a property instead of a parameterless function when it:

- Never throws.
- Is cheap (or cached).
- Returns the same result for the same state.

### Extension Functions

- Prefer extension functions for behavior centered around a type.
- Keep visibility as narrow as possible (`private`, local, internal).

### Infix Functions

- Use `infix` only when both operands have equal conceptual importance (`to`, `zip`, `and`).
- Never use `infix` for mutating operations.

### Factory Functions

- Prefer descriptive names over matching the class name:
  ```kotlin
  Point.fromPolar(...)
  ```
- Use factory functions when constructors become complex or overloaded.

### Platform Types (Java Interop)

- Explicitly declare Kotlin types for public APIs and properties receiving Java platform types.
  ```kotlin
  val name: String = javaApi.getName()
  ```

### Scope Functions

Choose based on intent:

- `let` → transformation / null handling.
- `run` → compute a result using receiver.
- `with` → operate on an object without extension syntax.
- `apply` → object configuration.
- `also` → side effects (logging, validation).
