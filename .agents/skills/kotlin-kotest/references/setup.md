# Project Setup & Configuration (Kotest 6.0+)

This guide explains how to configure Kotest 6.0+ and MockK in Kotlin JVM and Multiplatform (KMP) projects using Gradle's
Version Catalog.

> [!IMPORTANT]
> **Check Version Availability First**: Before configuring dependencies, always use the `context7` CLI (or search tools)
> to resolve the latest stable release versions for Kotest and MockK. Avoid hardcoding outdated versions from training
> data.

## 1. Gradle Version Catalog (`gradle/libs.versions.toml`)

Define the Kotest 6.0+ dependencies, KSP, and MockK in the version catalog:

```toml
[versions]
# Verify the latest versions via context7
kotest = "6.1.11"
ksp = "2.3.9"
mockk = "1.14.11"

[libraries]
# Kotest
kotest-runner-junit6 = { module = "io.kotest:kotest-runner-junit6", version.ref = "kotest" }
kotest-framework-engine = { module = "io.kotest:kotest-framework-engine", version.ref = "kotest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
# More kotest modules can be added as needed (e.g., kotest-assertions-json, kotest-assertions-arrow, etc.)

# MockK
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }

[plugins]
kotest = { id = "io.kotest", version.ref = "kotest" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

## 2. Kotlin JVM Setup

```kotlin
plugins {
  alias(libs.plugins.kotlin.jvm)
  // Apply Kotest plugin
  alias(libs.plugins.kotest)
}

dependencies {
  // Kotest Runner
  testImplementation(libs.kotest.runner.junit6)

  // MockK Mocking framework (JVM only)
  testImplementation(libs.mockk)
}

// Ensure Gradle uses the JUnit Platform to run tests
tasks.withType<Test> {
  useJUnitPlatform()
}
```

## 3. Kotlin Multiplatform (KMP) Setup

```kotlin
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  // Apply KSP plugin and Kotest plugin
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotest)
}

kotlin {
  // Target declarations
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonTest.dependencies {
      // Kotest multiplatform test engine
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.assertions.core)
    }

    jvmTest.dependencies {
      // Runner and MockK are JVM-only
      implementation(libs.kotest.runner.junit6)
      implementation(libs.mockk)
    }
  }
}

// Allow Kotest to run tests on the JVM target using JUnit Platform
tasks.withType<org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest> {
  useJUnitPlatform()
}
```
