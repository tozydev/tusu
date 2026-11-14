# Tusu — Agent Instructions

Tusu is a local-first multimedia journal app built with Kotlin Multiplatform and Compose
Multiplatform.

## Targets

| Target            | Status                                                                       |
|-------------------|------------------------------------------------------------------------------|
| Android           | Primary target; all features ship here first                                 |
| JVM (desktop)     | Set up with mock/no-op `actual`s for fast compile and tests; not shipped yet |
| iOS, macOS, Linux | Later phases                                                                 |

## Constraints

- **Offline-first.** Every feature works without network. Sync arrives in v1.1; `core/` and
  `server/`
  are placeholders until then.
- **Single user, no backend** in the MVP.
- **One app module.** All app code lives in `app/shared`; `app/androidApp` is the entry point only.
  Split by package, not by Gradle module.
- **No Android APIs in `commonMain`.** Platform code lives only in `platform/` and `ui/platform/`.
- **Multiplatform libraries first.** Use `expect`/`actual` only when no suitable library exists.
- **Kotlin stdlib first.** Use `kotlin.uuid.Uuid`, `kotlin.time.Instant`, `kotlin.time.Clock`;
  never `java.util.UUID` or the kotlinx-datetime `Instant`/`Clock`. Use kotlinx-datetime only for
  `LocalDate`, `TimeZone` and formatting.
- Use the latest stable, well-maintained libraries; declare every dependency in
  `gradle/libs.versions.toml`.

## Workflow (Linear)

All planning, backlog and progress live in Linear (team `tozydev`, issue prefix `TOZY`); never keep
progress files in the repo. Project documentation is kept in Linear documents and Claude artifacts,
not in the repo.

- **Read the issue first.** Before any work, read the issue, its parent, its sub-issues and its
  comments; the issue's "Xong khi" line is the definition of done.
- **Branch** from the issue's `gitBranchName`.
- **Commits** are written in English and follow Conventional Commits, scoped by feature, ending
  with the issue id: `feat(composer): auto-save after 500 ms (TOZY-123)`.
- **TODOs always carry an issue id:** `// TODO(TOZY-123): description`. No bare `TODO`; if no issue
  fits, create one in Backlog first. Find them with `grep -rn "TODO("`.
- **Status:** move the issue to *In Progress* when coding starts, *In Review* when the code is done
  and CI is running, *Done* once merged with green tests.
- **Nothing gets lost.** Work that is cut or deferred is moved to a later issue with the `Hoãn`
  label and a comment explaining why; deviations from the design get the `Lệch design` label. New
  bugs and ideas become Backlog issues right away.
- Hide unfinished features in the UI; never ship a control that does nothing.
- **Versions** are CalVer `YYYY.M.MICRO[-preview]`, set by `version` in `gradle.properties` and
  bumped by the release workflow. MICRO is at most 99 because `versionCode` is `YY MM MICRO 00`.

## Tech Stack

- **UI:** Compose Multiplatform, Material 3 Expressive, MaterialKolor
- **Navigation:** Navigation 3 + `lifecycle-viewmodel-navigation3`
- **DI:** Metro (`@ContributesBinding`, `metrox-viewmodel`)
- **Database:** Room 3 (`androidx.room3`) + `BundledSQLiteDriver`, FTS5, `room3-paging`
- **Settings:** DataStore Preferences
- **Files, pickers, camera:** FileKit
- **Images:** Coil 3
- **Video/audio playback:** Compose Media Player
- **Rich text:** Compose Rich Editor + `richeditor-compose-json` (same version)
- **Location:** Compass
- **Core:** Coroutines/Flow, kotlinx-serialization, kotlinx-datetime, kotlinx-io, kotlin-logging
- **Testing:** Kotest
- **Formatting:** ktfmt

## Project Structure

```
tusu/
├── app/
│   ├── androidApp/            Entry point only: MainActivity, TusuApplication, AndroidAppGraph
│   └── shared/src/
│       ├── commonMain/kotlin/vn/id/tozydev/tusu/
│       │   ├── di/            AppGraph, Qualifiers
│       │   ├── domain/
│       │   │   ├── model/     Entry, Media, Label, LabelGroup, Place, EntryFilter
│       │   │   ├── error/     Outcome, typed error hierarchies
│       │   │   ├── repository/ Repository interfaces
│       │   │   └── usecase/   Shared business logic only
│       │   ├── data/
│       │   │   ├── db/        TusuDatabase, Converters, migration/, one folder per table group
│       │   │   │              (entry/, media/, label/, place/): Entity, Dao, Mapper
│       │   │   ├── repository/ Repository implementations
│       │   │   ├── media/     MediaStore, MediaJobQueue
│       │   │   └── backup/    BackupPayload, ZipExporter, ZipImporter
│       │   ├── platform/      Interfaces + expect: AudioRecorder, ImageCompressor, AppLock, Reminder…
│       │   ├── text/          Rich text JSON ↔ plain text, diacritic folding, #hashtag parsing
│       │   ├── util/
│       │   └── ui/
│       │       ├── navigation/  theme/  component/  format/
│       │       ├── platform/  Composable expect: CameraLauncher, Coil setup
│       │       └── feature/   timeline/ composer/ calendar/ media/ viewer/ entry/ search/
│       │                      location/ labels/ trash/ settings/ lock/ onboarding/
│       ├── androidMain/       actual implementations, DatabaseBuilder
│       ├── jvmMain/           Mock/no-op actuals
│       └── commonTest/        Mirrors commonMain packages; sample data lives here
├── core/                      Models and sync protocol shared by app and server (v1.1)
└── server/                    Ktor E2E sync server (v1.1)
```

## Architecture Rules

- **The database is the single source of truth.** UI observes Room through `Flow`; never keep a
  separate in-memory copy or update UI state optimistically after a write.
- **Repositories:** interface in `domain/repository`, implementation `XxxRepositoryImpl` in
  `data/repository` bound with `@ContributesBinding`. Implementations own data consistency
  (transactions, FTS updates).
- **ViewModels call repositories directly.** Add a use case only when logic is called from two or
  more
  places or spans several repositories in one transaction (`AttachMedia`, `MoveToTrash`,
  `RestoreFromTrash`, `ApplyHashtags`).
- **No side effects inside `MutableStateFlow.update {}`.**
- `ui/feature/*` packages must not import each other; navigate through routes in `ui/navigation`.
- `domain` must not import `data` or `ui`.
- **No Save button.** Debounce writes ~500 ms and flush immediately when the app goes to background.
- Long-running media work goes through `MediaJobQueue` (WorkManager on Android) and must be
  resumable
  from `media.status`.

## Error Handling

- **Typed errors, not exceptions.** Expected failures (I/O, permissions, missing data, invalid
  input)
  are returned as values: `Outcome<T, E>` with a `sealed interface` error per area (`MediaError`,
  `BackupError`…), defined in `domain/error/`.
- Exceptions only signal bugs (`require`, `check`, `error`). Catch third-party exceptions at the
  boundary (`data/`, `platform/`) and map them to a typed error immediately.
- Never swallow `CancellationException`; no bare `runCatching` around suspend code.
- **Every error reaches the user.** ViewModels map errors to `UiState` or a one-off message with
  localized text and, where possible, a retry action. No empty `catch`, no log-and-continue; logging
  is in addition to surfacing, never instead of it.
- Background failures are persisted (e.g. `media.status = failed`) and shown in the UI with retry.

## Data Conventions

- Table names are plural snake_case: `entries`, `media`, `label_groups`, `labels`, `entry_labels`,
  `places`, `entries_fts`. Entity classes are singular (`EntryEntity`) with
  `@Entity(tableName = …)`.
- Primary keys are `Uuid` generated on device; timestamps are `Instant`.
- Every table has `updated_at` and `deleted_at` (soft delete). Trash keeps items for 30 days.
- **Rich text:** `entries.body` stores the Compose Rich Editor JSON from `RichTextState.toJson()`;
  load with `setJson()`. Keep the stored JSON as-is so unknown marks survive. Map
  `UnsupportedRichTextJsonVersionException` and `MalformedRichTextJsonException` to a typed error.
- `text_plain` and `text_search` (diacritics removed, `đ` → `d`) are derived from `body` on save.
  Search folds the query the same way. Markdown is generated only for ZIP export.
- Every schema change ships with a migration and a migration test. Schemas are exported to
  `app/shared/schemas`.

## Code Style

- Format with **ktfmt using Kotlin official style** (`--kotlinlang-style`); run it before
  committing.
- One public top-level class per file; file name matches the class.
- Naming: `XxxScreen`, `XxxViewModel`, `XxxUiState`, `XxxRepository`, `XxxRepositoryImpl`, `XxxDao`,
  `XxxEntity`, `XxxUseCase` (called via `operator fun invoke`).
- A feature folder holds its screen, ViewModel, UI state and a `component/` subfolder.
- `UiState` is an immutable `data class` exposed as `StateFlow`; events are plain ViewModel
  functions.

### UI Composition

- Split each screen into `XxxRoute` (gets the ViewModel, collects state) and `XxxScreen` (takes
  state
  and callbacks only, previewable).
- `XxxScreen` only arranges sections; each visual section is its own composable. Split a composable
  as soon as it does more than one thing or grows past ~80 lines.
- Feature-only components go in `feature/<name>/component/`; components used by two or more features
  move to `ui/component/`.
- Composables are stateless where possible, take `modifier: Modifier = Modifier` as the first
  optional parameter and receive only the data they render, not the whole `UiState`.
- Add a `@Preview` for every component and screen.

### Context Parameters

Context parameters are stable since Kotlin 2.4; use them for ambient values that many functions need
but that are not the function's subject:

- **Time and zone:** `context(clock: Clock)`, `context(timeZone: TimeZone)` for domain logic and
  mappers (e.g. `moveToTrash()`, `toLocalDate()`), so tests supply a fixed clock without threading
  it everywhere.
- **Typed-error scope:** `context(raise: Raise<E>)` (a small project type in `domain/error/`) so
  functions can fail with a typed error without nesting `Outcome` checks.
- **Transaction scope:** DAO/repository helpers that must run inside a transaction take
  `context(tx: TransactionScope)`, making it impossible to call them outside one.

Do not use them for:

- Repositories, DAOs or services: inject those through constructors with Metro.
- Hiding a function's real inputs; at most two context parameters per function.
- Explicit context arguments or callable references to context functions (still experimental).

### Kotlin

- Prefer `suspend` and `Flow` over callbacks; never block the main thread.
- No `!!`; handle nullability explicitly.
- All user-facing strings go in `composeResources` with Vietnamese and English; never hard-code UI
  text.
- Log with kotlin-logging; never log entry content.
- **API docs:** KDoc only for public APIs whose name does not say enough. One or two sentences; no
  restating the signature, no `@param`/`@return` for obvious values.
- Comments explain *why*, not *what*.

## Testing

- Follow TDD: write tests before implementing business logic.
- Test repositories against an in-memory Room database with `BundledSQLiteDriver`.
- Cover repositories, filters/queries, text processing, auto-save timing (`TestDispatcher`),
  migrations and the ZIP export → import round trip.
- Skip tests for UI components, simple models and trivial getters/setters.

## Gradle Tasks

Prefer targeted tasks: `./gradlew -q --console=plain <task>` (drop the flags to debug a build).

| Task                            | Purpose                     |
|---------------------------------|-----------------------------|
| `:app:shared:compileKotlinJvm`  | Compile shared code quickly |
| `:app:shared:allTests`          | Run all shared tests        |
| `:app:androidApp:assembleDebug` | Build the Android app       |
| `ktfmtFormat` / `ktfmtCheck`    | Format / check formatting   |
| `tasks --all`                   | List available tasks        |
