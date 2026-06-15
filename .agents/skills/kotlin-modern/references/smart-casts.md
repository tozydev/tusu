# Smart Cast Improvements in Kotlin 2.0

Kotlin 2.0.0 (K2 compiler) significantly enhances the smart-casting engine, allowing automatic casting in many scenarios
that previously required explicit casts or safe calls.

## 1. Local Variables in Further Scopes

The compiler can now analyze variable assignments across scopes and smart-cast local variables declared before control
flow blocks (`if`, `when`, `while`).

```kotlin
class Cat {
  fun purr() {
    println("Purr purr")
  }
}

fun petAnimal(animal: Any) {
  val isCat = animal is Cat
  if (isCat) {
    // Kotlin 2.0: Smart-cast to Cat works
    // Kotlin 1.9: Error (does not propagate smart-cast info through variable)
    animal.purr()
  }
}
```

## 2. Catch and Finally Blocks

Kotlin 2.0 preserves and re-evaluates smart cast information within `catch` and `finally` blocks, tracking modifications
made in the `try` block.

```kotlin
fun testString() {
  var stringInput: String? = null
  stringInput = "" // Smart-cast to String
  try {
    println(stringInput.length) // 0

    stringInput = null // Reset smart-cast, now String?

    if (2 > 1) throw Exception()
    stringInput = ""
  } catch (exception: Exception) {
    // Kotlin 2.0: Correctly knows stringInput can be null, requires safe call
    println(stringInput?.length)

    // Kotlin 1.9: Erroneously assumed safe call wasn't needed and led to runtime issues
  }
}
```

## 3. Class Properties with Function Types

Properties with nullable function types can now be called directly within null-checks, without using `.invoke()`.

```kotlin
class Holder(val provider: (() -> Unit)?) {
  fun process() {
    if (provider != null) {
      // Kotlin 2.0: Smart-cast works, called directly
      provider()

      // Kotlin 1.9: Error - Reference has a nullable type '(() -> Unit)?', 
      // had to use provider.invoke() or provider?.invoke()
    }
  }
}
```

## 4. Type Checks with Logical OR (`||`)

When type checks are combined with `||`, the compiler smart-casts the variable to their common supertype.

```kotlin
interface Status
class Active : Status { fun doWork() {} }
class Pending : Status { fun doWork() {} }

fun process(status: Any) {
  if (status is Active || status is Pending) {
    // Smart-cast to Status (common supertype)
    println(status.toString())
  }
}
```

## 5. Inline Functions

Inline functions passing lambdas preserve smart-cast information because the compiler knows the lambda is executed in
place.

```kotlin
inline fun <T> runLogged(block: () -> T): T = block()

fun printLength(str: String?) {
  if (str != null) {
    runLogged {
      // Kotlin 2.0: Smart-casts to String inside the lambda
      println(str.length)
    }
  }
}
```
