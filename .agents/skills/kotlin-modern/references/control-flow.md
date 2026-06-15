# Control Flow and String Interpolation Updates

This guide covers language features introduced in Kotlin 2.1.0 (preview) and stabilized in Kotlin 2.2.0: guard
conditions in `when`, non-local `break` and `continue`, and multi-dollar string interpolation.

## 1. Guard Conditions in `when` Expressions

You can now use `if` guard conditions directly in `when` branches with a subject. This avoids nesting `if` expressions
within when branches.

### Example

```kotlin
sealed interface Animal {
  data class Cat(val mouseHunter: Boolean) : Animal
  data class Dog(val breed: String) : Animal
}

fun feedAnimal(animal: Animal) {
  when (animal) {
    // Standard type branch
    is Animal.Dog -> println("Feeding the dog")

    // Type branch with a guard condition
    is Animal.Cat if !animal.mouseHunter -> println("Feeding the lazy cat")
    is Animal.Cat if animal.mouseHunter -> println("No feeding needed; it catches mice")

    else -> println("Unknown animal")
  }
}
```

### Combining Guards

Guards can be combined using logical operators `&&` or `||`:

```kotlin
when (animal) {
  is Animal.Cat if (!animal.mouseHunter && animal.hungry) -> feedCat()
}
```

## 2. Non-Local `break` and `continue`

Kotlin 2.2.0 stabilizes support for using `break` and `continue` inside inline lambdas (such as `forEach`, `run`,
`let`), referencing the outer loop.

### Example

```kotlin
fun findFirstEven(numbers: List<Int>) {
  for (i in 1..10) {
    numbers.forEach { num ->
      if (num % 2 == 0) {
        println("Found even: $num")
        // Non-local break exits the outer for-loop
        break
      }
    }
  }
}
```

*Note: This only works within inline lambdas, as they are compiled as inlined code blocks in the caller loop scope.*

## 3. Multi-Dollar String Interpolation

Using a literal `$` sign in template strings (e.g. JSON templates, LaTeX, financial reports) historically required
escaping via `${'$'}`. Multi-dollar string interpolation allows specifying the number of dollar signs required to
trigger interpolation.

### Double-Dollar (`$$`) Example

Using `$$` means literal `$` is ignored, and only `$${expression}` triggers interpolation.

```kotlin
val jsonSchema = $$"""
    {
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "title": "$${simpleName}",
        "type": "object"
    }
    """
```

### Triple-Dollar (`$$$`) Example

Using `$$$` allows both `$` and `$$` to be literal characters, and only `$$$expression` or `$$${expression}` triggers
interpolation.

```kotlin
val productName = "apple"
val data = $$$"""{
  "currency": "$",
  "enteredAmount": "10.00 $$",
  "product": "$$$productName"
}
"""
```
