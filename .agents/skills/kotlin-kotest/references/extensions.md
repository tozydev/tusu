# Kotest Extensions Reference Guide

This reference guide provides configuration, syntax, and examples for standard Kotest extensions, writing custom
extensions, and creating mountable extensions.

## 1. Decoroutinator Extension

The Decoroutinator extension integrates Stacktrace Decoroutinator to clean up stack traces in coroutine-based tests by
stripping internal coroutine machinery.

### Dependency Configuration

Add the dependency to your Version Catalog (`gradle/libs.versions.toml`):

```toml
[libraries]
kotest-extensions-decoroutinator = { module = "io.kotest:kotest-extensions-decoroutinator", version.ref = "kotest" }
```

### Usage

Register `DecoroutinatorExtension` at the spec level or globally in `AbstractProjectConfig`.

#### Spec-Level Registration

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.decoroutinator.DecoroutinatorExtension

class MyCoroutineTest : ShouldSpec() {
  init {
    extension(DecoroutinatorExtension())

    should("do something in a coroutine") {
      // Coroutine-based test code
    }
  }
}
```

#### Global Registration

```kotlin
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.decoroutinator.DecoroutinatorExtension

class ProjectConfig : AbstractProjectConfig() {
  override fun extensions() = listOf(DecoroutinatorExtension())
}
```

## 2. Spring Extension

The Spring extension enables Kotest to perform dependency injection (DI) using the Spring Context in your tests.

### Dependency Configuration

```toml
[libraries]
kotest-extensions-spring = { module = "io.kotest:kotest-extensions-spring", version.ref = "kotest" }
```

### Registration

You can register `SpringExtension` globally or per-spec.

#### Global Registration

```kotlin
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class ProjectConfig : AbstractProjectConfig() {
  override val extensions = listOf(SpringExtension())
}
```

#### Per-Spec Registration

```kotlin
import io.kotest.core.extensions.ApplyExtension
import io.kotest.extensions.spring.SpringExtension

@ApplyExtension(SpringExtension::class)
@ContextConfiguration(classes = [AppConfig::class])
class MyServiceSpec(private val userService: UserService) : ShouldSpec() {
  init {
    should("autowire constructor parameters") {
      userService.findUser().name shouldBe "John Doe"
    }
  }
}
```

### Lifecycle & Transactions

By default, the Spring extension runs in **Leaf Test Lifecycle Mode** with transaction rollback:

- Each leaf test runs in its own transaction, which is **rolled back by default** at the end of the test.
- To change this behavior to root-level transactions, pass `SpringTestLifecycleMode.Root` to the extension or use
  `@ApplyExtension(SpringRootTestExtension::class)`.
- Use the `@Commit` annotation on the spec class to commit transaction changes at the end of each test instead of
  rolling back.

```kotlin
@ContextConfiguration(classes = [AppConfig::class])
@Transactional
@Commit
@ApplyExtension(SpringExtension::class)
class TransactionalSpec(private val repository: UserRepository) : ShouldSpec() {
  init {
    should("persist data between tests") {
      repository.save(User(name = "Alice"))
    }
  }
}
```

### Accessing Spring Test Context

To access Spring's `TestContextManager` within your tests:

```kotlin
import io.kotest.extensions.spring.testContextManager

should("log the current test context") {
  val context = testContextManager().testContext
  println("Spring context: $context")
}
```

> [!WARNING]
> **Final Classes**: If you use `SpringExtension` on a final class (Kotlin classes are final by default), Kotest will
> issue a warning. If DI fails to work properly, open the class: `open class MySpec : ShouldSpec()`. You can suppress
> this warning using the system property `kotest.listener.spring.ignore.warning=true`.

## 3. Ktor Extension

The `kotest-assertions-ktor` module provides response matchers for testing Ktor servers and clients.

### Dependency Configuration

```toml
[libraries]
kotest-assertions-ktor = { module = "io.kotest:kotest-assertions-ktor", version.ref = "kotest" }
```

### Usage Examples

#### Server Testing

```kotlin
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.ktor.client.shouldHaveHeader
import io.kotest.assertions.ktor.client.shouldNotHaveContent
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication

withTestApplication({ module(testing = true) }) {
  handleRequest(HttpMethod.Get, "/").apply {
    response shouldHaveStatus HttpStatusCode.OK
    response shouldNotHaveContent "error"
    response.shouldHaveHeader("Content-Type", "application/json")
  }
}
```

#### Client HTTP Testing

```kotlin
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.ktor.client.shouldHaveHeader
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get

val client = HttpClient(CIO)
val response = client.get("https://api.example.com/health")
response.shouldHaveStatus(HttpStatusCode.OK)
response.shouldHaveHeader("X-Health-Status", "UP")
```

## 4. Testcontainers Extension

The Testcontainers extension provides lifecycle integration for managing Docker containers within Kotest specifications
or globally.

### Dependency Configuration

```toml
[libraries]
kotest-extensions-testcontainers = { module = "io.kotest:kotest-extensions-testcontainers", version.ref = "kotest" }
```

### Generic Containers

Use `TestContainerSpecExtension` (spec-scoped) or `TestContainerProjectExtension` (project-scoped) to manage lifecycle.

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerSpecExtension
import org.testcontainers.containers.GenericContainer

class RedisSpec : FunSpec({
  val redisContainer = install(TestContainerSpecExtension(GenericContainer("redis:6.2-alpine"))) {
    withExposedPorts(6379)
  }

  test("ping redis") {
    val host = redisContainer.host
    val port = redisContainer.firstMappedPort
    // Initialize Redis client and run tests
  }
})
```

### JDBC Database Containers

Kotest provides `JdbcDatabaseContainerSpecExtension` and `JdbcDatabaseContainerProjectExtension` to automatically
configure and return a HikariCP-backed `javax.sql.DataSource`.

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerSpecExtension
import org.testcontainers.containers.MySQLContainer

class DatabaseSpec : FunSpec({
  val mysql = MySQLContainer<Nothing>("mysql:8.0").apply {
    withDatabaseName("test_db")
  }

  // Install the database container extension, returning a DataSource
  val dataSource = install(JdbcDatabaseContainerSpecExtension(mysql)) {
    // Optional HikariCP Pool Configuration
    maximumPoolSize = 10
    idleTimeout = 10000
  }

  test("perform database query") {
    dataSource.connection.use { conn ->
      // Query database
    }
  }
})
```

### Compose Containers

Manage multiple services via docker-compose using `ComposeContainerSpecExtension` or `ComposeContainerProjectExtension`.

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.ComposeContainerSpecExtension
import java.io.File

class DockerComposeSpec : FunSpec({
  // From a file
  val compose = install(ComposeContainerSpecExtension(ComposeContainer(File("docker-compose.yml"))))

  // Or directly from the test classpath resources directory
  // val compose = install(ComposeContainerSpecExtension.fromResource("docker-compose.yml"))
})
```

### Container Logs

To redirect container outputs to the Kotest console:

```kotlin
import io.kotest.extensions.testcontainers.ContainerExtensionConfig
import io.kotest.extensions.testcontainers.StandardLogConsumer
import io.kotest.extensions.testcontainers.LogTypes

install(
  TestContainerSpecExtension(
    container,
    ContainerExtensionConfig(logConsumer = StandardLogConsumer(LogTypes.ALL))
  )
)
```

## 5. Custom Extensions

Custom extensions are implemented by subclassing lifecycle listener interfaces or interceptor extensions.

### Basic Lifecycle Listeners (Simple Extensions)

Implement listeners like `BeforeSpecListener`, `AfterSpecListener`, `BeforeEachListener`, or `AfterEachListener` to hook
into events.

```kotlin
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.spec.Spec

class KafkaCleanupListener : BeforeSpecListener, AfterSpecListener {
  override suspend fun beforeSpec(spec: Spec) {
    // Start or clear mock Kafka topic
  }

  override suspend fun afterSpec(spec: Spec) {
    // Clean up
  }
}

// Register per spec
class QueueSpec : FunSpec({
  extension(KafkaCleanupListener())
})
```

### Advanced Extensions (Interceptors)

For more advanced needs (intercepting execution, modifying results, or tweaking coroutine context), implement advanced
interfaces:

- `TestCaseExtension`: Intercept test cases (e.g. skip/retry).
- `SpecExtension`: Intercept specs.
- `ConstructorExtension`: Customize how specs are instantiated.

```kotlin
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class RetryFailedTestExtension(private val attempts: Int) : TestCaseExtension {
  override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
    var result = execute(testCase)
    var count = 1
    while (result.isErrorOrFailure && count < attempts) {
      result = execute(testCase)
      count++
    }
    return result
  }
}
```

## 6. Mountable Extensions

Mountables are a specialized type of extension that can be installed using `install(...)` at the top level of a spec.
They return a materialized control/client object (which can differ from the extension class itself) configured via a
custom DSL block.

### Anatomy of a Mountable

To create a Mountable, implement `MountableExtension<Config, MaterializedValue>`:

1. `Config`: Type that holds the user configuration options block.
2. `MaterializedValue`: Type returned by the `install` function.

### Example: Custom H2 Database Mountable

This custom mountable spins up an H2 connection, allows database configuration, and closes the connection after the spec
completes.

```kotlin
import io.kotest.core.extensions.MountableExtension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.spec.Spec
import java.sql.Connection
import java.sql.DriverManager

// 1. Define configuration settings
class H2DatabaseConfig {
  var databaseName: String = "test_db"
  var username: String = "sa"
}

// 2. Implement the MountableExtension and lifecycle listeners
class H2DatabaseMountableExtension : MountableExtension<H2DatabaseConfig, Connection>, AfterSpecListener {
  private var connection: Connection? = null

  override fun mount(configure: H2DatabaseConfig.() -> Unit): Connection {
    val config = H2DatabaseConfig().apply(configure)
    val url = "jdbc:h2:mem:${config.databaseName};DB_CLOSE_DELAY=-1"
    connection = DriverManager.getConnection(url, config.username, "")
    return connection!!
  }

  override suspend fun afterSpec(spec: Spec) {
    connection?.close()
  }
}

// 3. Install in a test spec
class UserServiceH2Test : FunSpec({
  // Install the database mountable, resulting in a connection object
  val dbConnection = install(H2DatabaseMountableExtension()) {
    databaseName = "users_test"
    username = "admin"
  }

  test("query user table") {
    dbConnection.createStatement().use { stmt ->
      val rs = stmt.executeQuery("SELECT 1")
      rs.next()
      rs.getInt(1) shouldBe 1
    }
  }
})
```

> [!CAUTION]
> **Non-Suspendable Mount Configuration**: The config block passed to `mount` cannot be suspendable because Kotest spec
> class initialization blocks are non-suspendable. If you need to make suspending calls inside `mount`, wrap them in
`runBlocking { ... }`.
