# Tusu Agent Instructions

Tusu is a cross-platform journal app built with Kotlin Multiplatform and Compose Multiplatform.
It philosophically embraces a clean architecture with local-first data management,
and a rich editing experience with Markdown support.

## General Technical Stack

- **Language:** Kotlin (multiplatform)
- **Dependency Injection:** Metro
- **IO:** `kotlinx-io`
- **Testing:** Kotest
- **Build Tool:** Gradle with Kotlin DSL

## Project Structure

```
├── docs              # Project related documentation
├── app               # Main application module
│   ├── androidApp    # Android target
│   ├── shared        # Shared code for all platforms
├── core              # Core libraries and utilities (shared between app and server - FUTURE WORK - NOT IMPLEMENTED YET)
├── server            # Backend server module (FUTURE WORK - NOT IMPLEMENTED YET)
```

## Important Rules

- Follow test-driven development (TDD) practices. Write tests before implementing features.
- Skip unit tests for UI components, simple models, and trivial getters/setters. Focus on testing
  business logic and complex interactions.
- Prefer use multiplatform libraries and avoid platform-specific code when possible. Use
  expect/actual only when necessary.
- Always use latest libraries version and well-maintained libraries.
- Use `ctx7` or `tvly` to search for libraries guide or Internet information.

## Gradle Tasks Guide

- Avoid building the entire project when working on a specific module. Use targeted Gradle tasks to
  speed up development.
- Use `./gradlew -q --console=plain :moduleName:taskName` to run specific tasks for a module, some
  tasks include:
    - `:compileKotlinJvm`: Compile shared JVM sources.
    - `:allTests`: Run all tests in the shared module.
    - `:androidApp:assembleDebug`: Build the Android app.
    - `:tasks --all`: List all available Gradle tasks for the project.
- Remove `-q --console=plain` for more verbose output when needed, especially for debugging build
  issues.
