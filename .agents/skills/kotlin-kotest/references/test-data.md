# Test Data Management

Leveraging language features allows us to create concise, readable test data builders that are easy to maintain and
understand. Below are patterns and best practices for managing test data in Kotlin tests.

## Rules

1. **Keep it Clean & Manageable**: Ensure test data templates are reusable but represent valid states by default. Overly
   verbose arrangements in test bodies clutter tests.
2. **Never Leak Test Code to Production**: Keep all test data builders, factory extension functions, and DSL constructs
   under `src/test/kotlin`. Never declare them in `src/main/kotlin`.
3. **Hide Irrelevant Details**: A test should only show the data inputs that directly influence the test assertion. All
   other fields should be populated with default sensible values.

## Decision Matrix

Use the matrix below to decide which pattern (or mix of patterns) fits your scenario:

| Pattern                                    | Best For                                                | Typical Scope          | Complexity |
|:-------------------------------------------|:--------------------------------------------------------|:-----------------------|:-----------|
| **Companion Extensions** (`User.sample()`) | Creating baseline, valid, or standard invalid instances | All scope              | Low        |
| **Instance Extensions** (`user.withX()`)   | Tweaking specific domains on a flat object              | All scope              | Low        |
| **DSL / Function Builders**                | Constructing complex nested trees or hierarchies        | Integration/Functional | High       |
| **Data Class `.copy()`**                   | Tweaking a few fields on an existing template or state  | All scopes             | Very Low   |

## 1. Extension Function Pattern

Use extension functions on companion objects for static defaults, and extension functions on instances for semantic
modifications. This is best suited for unit testing simple, flat objects.

### Companion & Instance Extensions

```kotlin
// Production Class
data class User(val id: Long, val name: String, val email: String) {
  companion object // Required to hook extension functions
}

// Test Extensions (in test source set)
fun User.Companion.sample(
  id: Long = 1L,
  name: String = "John Doe",
  email: String = "john@example.com"
): User = User(id, name, email)

fun User.Companion.invalid(
  id: Long = -1L
): User = User(id, name = "", email = "invalid-email")

// Instance Extension for semantic modifications
fun User.withInvalidEmail(): User = this.copy(email = "bad-email")
```

### DO and DON'T

```kotlin
// DO: Use semantic names and default parameters to keep test setup concise
should("fail when email is invalid") {
  val user = User.sample().withInvalidEmail() // Clearly communicates intent

  validator.validate(user) shouldBe false
}

// OR
should("fail when email is invalid") {
  val user = User.sample(email = "bad-email") // Still clear, but less semantic than the instance extension

  validator.validate(user) shouldBe false
}

// DON'T: Over-configure every property manually in the test body
should("fail when email is invalid") {
  val user = User(
    id = 1L,
    name = "John Doe",
    email = "bad-email" // Obscures why the user is invalid
  )

  validator.validate(user) shouldBe false
}
```

## 2. DSL / Function Builder Pattern

For integration tests or complex domain models containing deep nesting (e.g., an `Order` containing `LineItem`s, which
contain `Product`s), write type-safe builders using Kotlin's function literals with receiver.

### DSL Builder Example

```kotlin
// Production Classes
data class Product(val name: String, val price: Double)
data class LineItem(val product: Product, val quantity: Int)
data class Order(val id: Long, val items: List<LineItem>)

// DSL Builders (in src/test/kotlin)
class ProductBuilder {
  var name: String = "Standard Product"
  var price: Double = 10.0
  fun build() = Product(name, price)
}

class LineItemBuilder {
  var quantity: Int = 1
  private var product: Product = ProductBuilder().build()

  fun product(block: ProductBuilder.() -> Unit) {
    product = ProductBuilder().apply(block).build()
  }
  fun build() = LineItem(product, quantity)
}

class OrderBuilder {
  var id: Long = 1L
  private val items = mutableListOf<LineItem>()

  fun item(block: LineItemBuilder.() -> Unit = {}) {
    items.add(LineItemBuilder().apply(block).build())
  }
  fun build() = Order(id, items)
}

// DSL Entrypoint
fun createOrder(block: OrderBuilder.() -> Unit = {}): Order =
  OrderBuilder().apply(block).build()
```

### DO and DON'T

```kotlin
// DO: Use the DSL to clearly construct hierarchical nested test data
should("calculate total price of nested items") {
  val order = createOrder {
    item {
      quantity = 2
      product { price = 50.0 }
    }
    item {
      quantity = 1
      product { price = 10.0 }
    }
  }

  order.items.size shouldBe 2
}

// DON'T: Write nested constructors which are hard to read and maintain
should("calculate total price of nested items") {
  val order = Order(
    id = 1L,
    items = listOf(
      LineItem(Product("Standard Product", 50.0), 2),
      LineItem(Product("Standard Product", 10.0), 1)
    )
  )

  order.items.size shouldBe 2
}

// DON'T: Use the DSL for simple flat objects where it adds unnecessary complexity
should("create a simple user") {
  val user = createUser { // Overkill for a simple flat object
    name = "John Doe"
    email = "jjohn@example.com"
  }

  user.name shouldBe "John Doe"
}
```

## 3. Leveraging Data Class `.copy()`

Kotlin's native `.copy()` is perfect for modifying fields on initiailized instances. This pattern will rarely be used in
initial test data setup but is ideal for tweaking a few fields on an existing template or state.

### DO and DON'T

```kotlin
// DO: Use .copy() to tweak only the relevant fields while keeping the rest of the data intact and clear
should("allow login for active users") {
  val activeUser = existingInitialUser.copy(isActive = true) // Clearly shows we're only changing the isActive flag

  authService.canLogin(activeUser) shouldBe true
}
```
