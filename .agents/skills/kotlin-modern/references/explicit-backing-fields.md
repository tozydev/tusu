# Explicit Backing Fields

Kotlin 2.3.0 (experimental) and Kotlin 2.4.0 (stable) introduce **Explicit Backing Fields**. This feature simplifies the
common Kotlin design pattern where a property exposes a read-only type publicly, but uses a mutable type internally.

## The Traditional Backing Property Pattern (Before Kotlin 2.3)

Historically, exposing a read-only StateFlow while keeping the mutable StateFlow private required two separate
properties:

```kotlin
class CityViewModel {
  // 1. Private backing property (mutable)
  private val _city = MutableStateFlow("New York")

  // 2. Public read-only property
  val city: StateFlow<String> get() = _city
}
```

This pattern is verbose and pollutes the class scope with private prefixed variables (`_city`).

## The New Explicit Backing Fields Pattern (Kotlin 2.3+)

With explicit backing fields, you can define the underlying field's type and initializer directly inside the public
property block using the `field` keyword.

### Example

```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CityViewModel {
  // Public read-only StateFlow, backed by MutableStateFlow
  val city: StateFlow<String>
    field = MutableStateFlow("New York")

  fun updateCity(newCity: String) {
    // Smart casting works automatically within the class.
    // The compiler knows that 'city''s backing field is MutableStateFlow,
    // so you can access the .value setter inside this class!
    city.value = newCity
  }
}
```

## How It Works

1. **Syntax:** Inside the property declaration, define `field = Initializer` directly below the property header.
2. **Access Rules:**
   - Outside the class: The property is accessed via its declared public type (`StateFlow<String>`).
   - Inside the class (private scope): The compiler smart-casts references to the property to the type of its backing
    field (`MutableStateFlow<String>`).
3. **Immutability Protection:** The mutable backing field remains completely hidden from external consumers, enforcing
   encapsulation.
