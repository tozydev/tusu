# Context Parameters

Context parameters (introduced in preview in Kotlin 2.2.0 and stabilized in Kotlin 2.4.0) replace the older experimental
*context receivers* design. They allow functions or properties to declare contextual requirements that must be present
in the calling scope.

## Declaring and Using Context Parameters

Context parameters are declared before a function or property using the `context` keyword. Crucially, they can be named,
allowing you to reference them directly in the body without using implicit `this` or labeled `this@Type`.

### Syntax Example

```kotlin
interface Logger {
  fun info(message: String)
}

interface DatabaseConnection {
  fun execute(query: String)
}

// Declaring named context parameters
context(log: Logger, conn: DatabaseConnection)
fun saveUser(userId: String, name: String) {
  log.info("Saving user $userId")
  conn.execute("INSERT INTO users VALUES ('$userId', '$name')")
}
```

### Context Properties

You can also use context parameters on properties:

```kotlin
context(log: Logger)
val String.logged: String
get() {
  log.info("Accessing string: $this")
  return this
}
```

## Naming Parameters vs Context Receivers

In the deprecated *context receivers* syntax (Kotlin 1.6.20 - 1.9.x), context receivers could not be named:

```kotlin
// Deprecated Context Receivers Syntax
context(Logger)
fun logMessage(msg: String) {
  // Had to use implicit this or labeled this
  info(msg)
}
```

Kotlin 2.4.0's **Context Parameters** syntax allows explicit names `context(log: Logger)` which makes the code much
safer and avoids naming clashes when having multiple contexts of the same type or similar interface signatures.

## Calling Functions with Context Parameters

To call a function that requires context parameters, you must provide the context in one of two ways:

### 1. Providing Context (Via `context(...)` block - Preferred)

You can bring the context objects into scope using a `context(...)` block, which is the preferred and cleanest way to
supply context:

```kotlin
fun executeSave(logger: Logger, connection: DatabaseConnection) {
  context(logger, connection) {
    // Both contexts are in scope, function can be called
    saveUser("123", "Alice")
  }
}
```

*(Note: You can also bring them into scope using traditional `with` or `run` blocks, but `context(...)` blocks are
preferred).*

### 2. Explicit Context Arguments (Experimental in Kotlin 2.4)

Kotlin 2.4 introduces the ability to pass context arguments explicitly by name at the call site. This is useful when
resolving ambiguities or when you do not want to open a context scope.

*(Note: Explicit context arguments are experimental in Kotlin 2.4.0 and require the compiler
flag `-Xexplicit-context-arguments`)*

```kotlin
fun executeSave(logger: Logger, connection: DatabaseConnection) {
  // Explicitly passing context arguments by name
  saveUser("123", "Alice", log = logger, conn = connection)
}
```

## Best Practices

- **Use Named Parameters:** Always name your context parameters (`context(db: Database)`) instead of using unnamed
  parameters to prevent reference ambiguity.
- **Limit Scope Complexity:** Do not stack too many context parameters (keep it to 2-3 max). If a function needs more,
  consider wrapping them in a single context configuration class.
