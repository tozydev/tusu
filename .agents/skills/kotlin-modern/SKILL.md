---
name: kotlin-modern
description: Comprehensive guide and patterns for modern Kotlin development (Kotlin 2.0 to 2.4). Use when writing new Kotlin code, upgrading dependencies, configuring build scripts, or using modern features like the K2 compiler, context parameters, explicit backing fields, when guards, multi-dollar string templates, smart casts, HexFormat/UUID/Base64/Clock/Instant standard libraries.
---

# Modern Kotlin (Kotlin 2.0 to 2.4) Guide

This skill provides guidelines, best practices, and code patterns for developing applications using modern Kotlin
features (from versions 2.0.0 through 2.4.0). It covers K2 compiler migration, language syntax updates, and standard
library changes.

## Feature Quick-Reference

Use the following table to identify which Kotlin version introduced a specific feature:

| Feature                               | Introduced (Preview) | Stable                 | Documentation Reference                                          |
|:--------------------------------------|:---------------------|:-----------------------|:-----------------------------------------------------------------|
| **K2 Compiler**                       | 1.9.0                | 2.0.0 (K2-Only in 2.4) | [K2 Migration Guide](references/migration-guide.md)              |
| **Smart Cast Enhancements**           | —                    | 2.0.0                  | [Smart Cast Improvements](references/smart-casts.md)             |
| **Guard Conditions in `when`**        | 2.1.0                | 2.2.0                  | [Control Flow Updates](references/control-flow.md)               |
| **Non-local `break`/`continue`**      | 2.1.0                | 2.2.0                  | [Control Flow Updates](references/control-flow.md)               |
| **Multi-dollar String Interpolation** | 2.1.0                | 2.2.0                  | [Control Flow Updates](references/control-flow.md)               |
| **Context Parameters**                | 2.2.0                | 2.4.0                  | [Context Parameters](references/context-parameters.md)           |
| **Explicit Backing Fields**           | 2.3.0                | 2.4.0                  | [Explicit Backing Fields](references/explicit-backing-fields.md) |
| **Common Clock & Instant APIs**       | 2.1.0                | 2.3.0                  | [Standard Library Guide](references/stdlib.md)                   |
| **Common UUID API**                   | 2.3.0                | 2.4.0                  | [Standard Library Guide](references/stdlib.md)                   |
| **HexFormat and Base64 APIs**         | 2.1.0                | 2.2.0                  | [Standard Library Guide](references/stdlib.md)                   |

## Detailed Reference Guides

Explore these guides for specific code examples, migration paths, and build setups:

- **K2 Compiler Migration:** Read the K2 compiler migration guide covering compiler behavior changes, compatibility
  updates, performance benchmarks, and rollback instructions in [K2 Migration Guide](references/migration-guide.md).
- **Smart Casts:** See how Kotlin 2.0 smart-casts local variables, catch blocks, function property calls, and logical OR
  checks in [Smart Cast Improvements](references/smart-casts.md).
- **Control Flow & Templates:** Learn about BDD-style `when` guard conditions, loops in inline lambdas, and
  double/triple dollar string templates in [Control Flow Updates](references/control-flow.md).
- **Context Parameters:** Master the new explicit named `context(name: Type)` syntax and understand how it replaces old
  context receivers in [Context Parameters Guide](references/context-parameters.md).
- **Explicit Backing Fields:** Simplify read-only public properties with private mutable storage using the new `field`
  syntax in [Explicit Backing Fields Guide](references/explicit-backing-fields.md).
- **Standard Library:** Review changes to stable `Clock`, `Instant`, `UUID` (v4/v7/non-monotonic), `HexFormat`,
  `Base64`, and monotonic time tracking in [Standard Library Guide](references/stdlib.md).
