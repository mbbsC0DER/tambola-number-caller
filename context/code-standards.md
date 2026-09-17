# Tambola Number Caller — Code Standards

## Purpose

This document defines how code must be written, organized, reviewed, and tested in the Tambola Android project. It applies to production code, tests, build configuration, Room schemas, and Android resources. `context/project-overview.md` defines product behavior, while `context/architecture.md` defines system boundaries and invariants. This file defines the implementation conventions used inside those boundaries.

## Language and Framework Context

This project is a native Android application written in Kotlin. TypeScript, JavaScript framework patterns, CSS conventions, and server API-route structures do not apply.

Do not add a web framework, HTTP API layer, Retrofit, Ktor client, backend route directory, or TypeScript toolchain unless the product scope and architecture documents are updated first. The current app has no network permission and must run a complete game offline.

## Required Baseline

| Area | Standard |
|---|---|
| Language | Kotlin using the official Kotlin code style |
| JVM target | Java 17 bytecode |
| UI | Jetpack Compose and Material 3 |
| State | Immutable data classes exposed through read-only `StateFlow` or `Flow` |
| Concurrency | Structured Kotlin coroutines |
| Persistence | Room for session data; DataStore for preferences only |
| Navigation | Navigation 3 |
| Speech | Android TTS behind the domain `SpeechPlayer` interface |
| Tests | JUnit 4, coroutine virtual time, Room Android tests, and Compose UI tests |
| Build scripts | Gradle Kotlin DSL and the version catalog |

## Formatting

1. Use four spaces for indentation. Do not use tabs.
2. Use UTF-8 source files and Unix line endings.
3. Follow Kotlin's official formatting and import-order rules.
4. Use trailing commas in multiline declarations, calls, constructors, collections, and parameter lists.
5. Put one expression or declaration per line when combining them would reduce readability.
6. Do not use semicolons to place multiple expressions on one line.
7. Prefer multiline formatting when a declaration or call no longer reads comfortably on one line.
8. Keep imports explicit and remove unused imports. Wildcard imports are prohibited in production code and discouraged in tests.
9. End every source file with a newline.
10. Do not align code with manual spaces; let normal indentation express structure.

No formatter is currently enforced by Gradle. Code must still follow these rules during review. If a formatter is added, use a single repository-wide configuration and format the entire affected file.

## Naming

| Construct | Convention | Example |
|---|---|---|
| Packages | Lowercase, no underscores | `com.pratham.tambola.domain` |
| Classes and interfaces | PascalCase, nouns or role names | `SessionController`, `SpeechPlayer` |
| Functions | camelCase, verb-first | `createSession`, `setForeground` |
| Properties and local variables | camelCase, concrete nouns | `drawnCount`, `playbackJob` |
| Boolean properties | Positive question or state | `isPlaying`, `announcementPending`, `complete` |
| Constants | Upper snake case | `REPEAT_GAP_MS` |
| Composable screens | PascalCase ending in `Screen` | `CallerScreen` |
| Other composables | PascalCase describing the UI element | `NumberBoard`, `PreviousNumbersDrawer` |
| ViewModels | Feature name ending in `ViewModel` | `TambolaViewModel` |
| Repository interfaces | Domain noun ending in `Repository` | `SessionRepository` |
| Repository implementations | Technology or source prefix | `RoomSessionRepository` |
| Room entities | Domain noun ending in `Entity` | `SessionEntity` |
| DAOs | Domain noun ending in `Dao` | `SessionDao` |
| Test classes | Production subject ending in `Test` | `SessionControllerTest` |
| Test methods | Backticked behavior statement | `` `pause stops speech without advancing the draw` `` |
| Time values | Include the unit in the name | `gapMs`, `startedAtEpochMs` when ambiguity exists |

Avoid generic names such as `data`, `item`, `manager`, `helper`, `util`, `handle`, or `process` when a domain-specific name is available. `Manager` is acceptable only for an Android resource owner such as audio focus, and the class must state exactly what it manages.

## File Organization

Production code follows the package boundaries in `context/architecture.md`:

```text
app/src/main/java/com/pratham/tambola/
├── MainActivity.kt
├── TambolaApplication.kt
├── domain/
├── data/
├── platform/
└── presentation/
```

Rules:

1. Place a file in the package that owns its responsibility, not the package where it is first used.
2. Name a file after its primary public type or top-level function group.
3. Keep one primary responsibility per file.
4. Closely related Room entities, relationship projections, DAO, and database declarations may coexist in `AppDatabase.kt` while the schema remains small.
5. Small private composables may remain with their owning screen. Extract a reusable component when it has its own state contract, tests, preview, or use in more than one screen.
6. Keep Android framework adapters in `platform`; do not put them in `domain`.
7. Keep source-controlled Room schema JSON in `app/schemas`; never edit generated schema JSON manually.
8. Mirror production packages under `src/test` and `src/androidTest` so the tested boundary is obvious.
9. Do not create catch-all `common`, `helpers`, `utils`, or `misc` packages. Put shared behavior in the layer that owns the concept.
10. Do not introduce a new Gradle module solely to reduce file count. Module changes require an architectural reason.

## Dependency Direction

Code must preserve this dependency direction:

```text
presentation → domain ← data
       ↓          ↑      
    platform ─────┘
```

More precisely:

- `domain` depends only on Kotlin and coroutines.
- `data` implements domain storage interfaces.
- `platform` implements domain platform interfaces.
- `presentation` consumes domain state and commands and may coordinate platform status only through the ViewModel.
- `TambolaApplication` and the ViewModel factory are composition points where concrete implementations are connected.

Never import a Room entity, DAO, DataStore key, or `TextToSpeech` into a composable or domain file. Never import a composable, ViewModel, Room implementation, or Android speech implementation into `domain`.

## Kotlin Standards

### Immutability

1. Prefer `val` over `var`.
2. Use immutable domain data classes and publish new copies for state changes.
3. Keep mutable implementation details private.
4. Expose `StateFlow`, not `MutableStateFlow`.
5. Do not expose mutable collections across a layer boundary. Return `List`, `Set`, or a copied snapshot.
6. Do not mutate a `Session` or `CallerState` instance after publication.

### Nullability

1. Use nullable types only when absence is a valid domain state.
2. Use `requireNotNull` or `checkNotNull` when an invariant proves that a value must exist at that point.
3. Do not use `!!` in production code.
4. Use safe calls only when silently skipping the operation is correct. Do not hide an invariant failure with `?.`.
5. Tests may use `!!` only after an assertion or fixture construction guarantees the value.

### Validation

- Use `require` for invalid caller input or invalid value construction.
- Use `check` for an invalid internal or persisted state transition.
- Validate domain constraints in domain models and repository boundaries, not only in the UI.
- Validate all time intervals through shared domain rules such as `validGap`.
- Never trust a session ID to imply that the session exists or is valid.

### Functions and Classes

1. Give each function one clear responsibility.
2. Prefer early returns over deeply nested conditional blocks.
3. Use exhaustive `when` expressions for enums and sealed hierarchies. Do not add `else` when listing every case gives compiler protection.
4. Keep public APIs small. Default to `private` for implementation details.
5. Use named arguments when two or more adjacent arguments share a type or their meaning is not obvious.
6. Do not use boolean parameters when the call site is ambiguous. Prefer an enum or named argument. Existing compact boundary methods such as `repeat(called = true)` must always use a named argument at new call sites.
7. Avoid inheritance unless an Android framework type requires it. Prefer interfaces and composition.
8. Do not create a wrapper or abstraction with only speculative value. Add abstractions at a real boundary: domain persistence, speech, time, randomization, or another dependency that must be replaced in tests.

### Collections and Number Rules

- Preserve call order with `List<Int>`.
- Use `Set<Int>` only for membership or uniqueness checks, never as the canonical calling sequence.
- Never rely on database row order without an explicit position sort or SQL order.
- Represent Tambola numbers as `Int` in the domain and persistence layers.
- Apply display padding such as `01` only in presentation formatting.
- Generate spoken text through `NumberAnnouncementFormatter`; do not build number phrases in screens or the speech adapter.

### Time

- Store absolute timestamps as epoch milliseconds in `Long` values.
- Include `Ms` in duration names and constants.
- Do not store formatted dates in Room.
- Format dates only at the presentation boundary using the device's time zone.
- Inject the clock into repositories or services when a rule depends on time and must be tested.
- Do not call `System.currentTimeMillis()` throughout the codebase; keep it behind the repository's clock dependency.

## Coroutines and Flow

1. Use structured scopes owned by a ViewModel or another lifecycle owner. `GlobalScope` is prohibited.
2. Repository methods that perform I/O must be `suspend` or return `Flow`.
3. Do not use `Thread.sleep`, blocking waits, or busy loops.
4. Use cancellable `delay` for playback timing.
5. Catch `CancellationException` before a broad `Exception` catch and rethrow it immediately.
6. Do not catch `Throwable` for ordinary error handling.
7. Keep mutable flows private and expose them with `asStateFlow` or as `Flow`.
8. Use lifecycle-aware collection in Compose through `collectAsStateWithLifecycle`.
9. Use `stateIn` only when a shared, replaying state stream is required and choose its sharing policy deliberately.
10. A class that launches a job owns its cancellation and cleanup.
11. Never launch more than one live playback job. Cancel and join the previous job before replacing it.
12. Do not hold a mutex while awaiting TTS playback or a timer. Locks protect transitions, not long-running work.
13. Use `NonCancellable` only for a small transition that must finish atomically, such as publishing state that corresponds to a database commit. Never wrap speech, delays, loops, or arbitrary cleanup in it.
14. Check cancellation between a non-cancellable database mutation and the next external side effect.
15. Flow collectors must not perform hidden session mutations.

## Domain and State-Machine Standards

1. `SessionController` is the only owner of playback mode transitions.
2. Add a new mode to `PlaybackMode` only when it represents an externally observable state with distinct legal actions.
3. Every new mode must update state-derived properties, UI rendering, transition tests, and architecture documentation.
4. Treat `CallerState` as a complete render model. A screen must not infer durable progress from local UI state.
5. Keep derived session values such as called, remaining, current, previous, and complete computed from the canonical order and progress.
6. Do not persist derived counts or lists that can be calculated from `session_numbers`.
7. Keep timing constants and validation in the domain package.
8. Keep live announcement wording and readout wording as separate formatter operations.
9. A readout must work from a copied snapshot and must not hold a live mutable reference to session progress.
10. A controller error must retain recoverable session state.

## Compose Standards

### State and Events

1. Follow state-down, events-up composition.
2. Route-level composables receive immutable state and event callbacks.
3. Only the app/navigation host obtains the ViewModel. Child screens do not look up repositories, the application container, or the ViewModel independently.
4. Store business state in the controller/ViewModel. Use `remember` or `rememberSaveable` only for UI state such as menus, sheets, drawers, selection previews, or navigation.
5. Do not launch business operations directly from composition. Use callbacks or a narrowly scoped effect tied to an explicit key.
6. Every `DisposableEffect` must reverse the resource change in `onDispose`.
7. Do not use composition as a timer or playback loop.

### Composable APIs

1. Use PascalCase names.
2. Place required state before optional parameters.
3. Put `modifier: Modifier = Modifier` before optional styling parameters on reusable components.
4. Do not accept a `Modifier` on an app-level screen merely to expose implementation details; accept one when the caller needs placement control or the component is reusable.
5. Use callbacks named for intent, such as `onPlay`, `onPause`, `onQuit`, or `onGapChange`.
6. Prefer specific callbacks over exposing controller methods or mutable state objects.
7. Keep composables free of Room, DataStore, TTS, and coroutine-scope ownership for business work.
8. Extract repeated UI into a component instead of copying layout and behavior.

### Recomposition

- Do not perform I/O, randomization, object registration, or mutation during ordinary composition.
- Use stable immutable values for screen state.
- Use `remember` for expensive UI-only objects created by composition.
- Key lazy-list items with stable domain identifiers.
- Do not optimize with `derivedStateOf`, custom stability annotations, or manual memoization without a measured recomposition problem.

### Navigation and Back Behavior

- Keep destination keys small and serializable/saveable where required.
- Route changes happen in the app navigation host.
- System Back and visible Back/Quit controls must invoke the same domain behavior for the caller screen.
- A modal drawer or sheet consumes Back before the app route does.
- Do not navigate away until the required session save/cleanup command finishes.

## Material Styling Standards

1. Use `MaterialTheme.colorScheme`, `typography`, and `shapes` for ordinary UI.
2. Define brand colors centrally in `Theme.kt`.
3. Hard-coded colors are permitted only for a deliberately fixed visual surface, such as the dark previous-number drawer, and must remain readable in both app themes.
4. Use `dp` for layout and `sp` for typography.
5. Reuse spacing values within a component. Extract design tokens if the same nonstandard value appears across multiple files.
6. Maintain a minimum 48 dp touch target for primary interactive controls unless Material supplies an accessible target internally.
7. Do not communicate called/current/remaining state by color alone; provide semantics and sufficient shape, emphasis, or text context.
8. Caller controls must remain usable with large font settings and on narrow supported screens.
9. Avoid decorative captions and subtitles that repeat information already visible in the control or state.
10. Use native vector paths or Android vector resources for simple icons. Do not add a large icon dependency for a handful of symbols.

## Text, Strings, and Accessibility

1. User-facing copy intended for release belongs in Android string resources.
2. Dynamic text should use formatted string resources rather than manual concatenation when localization is relevant.
3. Domain speech phrases remain in `NumberAnnouncementFormatter` because exact English phrasing is a business rule, not UI copy.
4. Every icon-only button must have a concise content description.
5. Decorative icons must use a null content description.
6. Number-board cells must expose the number and its state: remaining, called, or current.
7. Drawer rows must expose both call position and number.
8. Use `testTag` only when a semantic query cannot reliably identify a structural UI element, such as the scrollable history list.
9. Do not encode essential meaning only in an icon or color.
10. Keep visible messages concise and action-oriented. Error messages must say what stopped and what the user can do next.

Hard-coded Compose strings already in the prototype should be moved to resources when their screen is next materially edited. New reusable or release-facing copy must use resources immediately.

## ViewModel Standards

1. ViewModels coordinate UI intent, lifecycle, controller calls, and presentation state.
2. Do not implement draw, speech-formatting, readout-ordering, TTL, or completion rules in a ViewModel.
3. Launch ViewModel work in `viewModelScope`.
4. Expose immutable flows and simple intent functions.
5. Keep one-time transient messages separate from durable session state.
6. Prevent duplicate navigation mutations with an explicit busy state when an operation must complete before navigation.
7. Cancel incompatible voice tests or playback before switching screens or sessions.
8. Release owned platform resources in `onCleared`.
9. Do not pass an Android `Activity` or composable scope into a ViewModel.
10. Use application context only in platform or data implementations that require it.

## Room and Repository Standards

### Entities and DAO

1. Room entities represent storage, not domain models.
2. Map Room projections to validated domain objects inside the repository.
3. Every entity has an explicit table name and primary key.
4. Add foreign keys and unique indices that enforce domain constraints at the database level.
5. Use cascade deletion only when child rows have no meaning without the parent.
6. DAO methods describe one query or mutation and do not contain presentation policy.
7. Queries that return ordered domain data must use explicit ordering or be sorted during mapping.
8. Do not expose a DAO outside the data package.

### Transactions

- Use `withTransaction` for operations that must commit as one unit.
- Create a session and all 90 sequence rows atomically.
- Keep draw, acknowledgement, completion deletion, and domain reconstruction in a consistent repository transaction where required.
- Do not call TTS or UI code inside a database transaction.
- Make state transitions idempotent where duplicate commands are possible.
- Check affected-row counts when exactly one mutation is required.

### Schema Changes

1. Keep Room schema export enabled.
2. Commit every generated schema version.
3. Never edit generated schema JSON manually.
4. Provide an explicit migration and migration test for every version increase.
5. Never use destructive migration fallback for production data.
6. Preserve pending-announcement recovery and 15-day expiry semantics through migrations.

## DataStore Standards

- Use DataStore only for small preferences with one clear owner.
- Create exactly one DataStore instance per file in the process.
- Keep keys private to the repository.
- Expose preferences as typed flows or domain values, never raw key maps.
- Provide safe defaults for missing or corrupt preference values.
- Catch storage I/O errors narrowly; do not hide programming errors.
- Do not put session progress, number order, TTL, or pending speech in DataStore.

## Speech and Platform Adapter Standards

1. Only `AndroidSpeechPlayer` may call Android TTS and audio-focus APIs.
2. The adapter accepts already formatted text and does not decide number wording.
3. Use one unique utterance ID per speech attempt.
4. Match completion, error, and stop callbacks to the active utterance ID.
5. Ignore stale callbacks after cancellation or replacement.
6. Suspend until the utterance completes, fails, stops, or times out.
7. Explicitly stop TTS on cancellation.
8. Request audio focus before speaking and release it after playback ends.
9. Treat audio-focus loss and noisy-output events as pause requests.
10. Prefer installed offline English voices and do not make calling depend on network synthesis.
11. Shut down the TTS engine and unregister receivers when the owner is cleared.
12. Do not synthesize announcements to files or queue an entire readout with `QUEUE_ADD`; speak one awaited number at a time.
13. Keep platform timeout values named and documented.

## Error Handling

1. Never silently swallow an exception.
2. Rethrow coroutine cancellation immediately.
3. Convert expected operational failures into a stable UI error state or message at the boundary that can explain recovery.
4. Keep session progress intact after speech or database failures.
5. Do not announce a number when its draw persistence failed.
6. Do not advance past a pending announcement after a speech failure.
7. Use specific exception messages that describe the failed capability and the next action.
8. Avoid exposing raw SQL, paths, stack traces, or internal IDs to users.
9. Do not use exceptions for ordinary UI branching.
10. Use `runCatching` only for a small operation where both success and failure are handled immediately. Do not build long chains that obscure cancellation.

## Authentication, Privacy, and Security Standards

The app has no authentication because it has no accounts or remote principals.

- Do not add ownership fields that imply a user model without updating the architecture.
- Treat the Android application sandbox as the current access boundary.
- Keep the database and preferences in app-private storage.
- Do not log complete session sequences, internal IDs, or storage contents in release builds.
- Do not add Internet permission, analytics, crash-data upload, sharing providers, or backup support without an explicit product and architecture decision.
- A session UUID is an identifier, never an authentication credential.
- Do not export Android components unless the feature requires it and the component has a documented permission and input-validation model.

## API and Network Standards

There are currently no HTTP APIs, request handlers, endpoints, DTOs, or remote services.

If a network feature is approved later:

1. Update `project-overview.md` and `architecture.md` before implementation.
2. Define authentication, authorization, ownership, offline behavior, retry semantics, and data migration first.
3. Put transport DTOs at the network boundary; do not expose them to domain or Compose code.
4. Map transport errors into domain results at the repository boundary.
5. Never perform network calls directly from composables.
6. Never make the core calling loop depend on network availability.

Do not create an `api`, `network`, or `routes` package until an approved feature needs one.

## Comments and Documentation

1. Prefer clear names and small functions over comments that restate code.
2. Add comments for non-obvious ordering, race prevention, recovery guarantees, Android platform quirks, or intentional tradeoffs.
3. Explain why a `NonCancellable` section, timeout, custom callback bridge, or hard-coded visual value is necessary.
4. Keep KDoc for public contracts whose lifecycle, threading, cancellation, or error behavior is not obvious from the signature.
5. Do not leave commented-out code, temporary debugging output, TODOs without context, or stale implementation notes.
6. A TODO must include a concrete missing behavior and, when available, an issue reference.
7. Update project documentation in the same change when behavior, storage, boundaries, timing, or invariants change.

## Testing Standards

### General

1. Test behavior and contracts, not private implementation details.
2. Every bug fix includes a regression test when the behavior is deterministic and testable.
3. Every change to a documented invariant includes a test that would fail if the invariant were broken.
4. Tests must be deterministic and independent of execution order.
5. Do not use production clocks, real random shuffles, real delays, or device TTS in JVM tests.
6. Do not weaken an assertion to make a failing implementation pass.
7. Avoid tests that merely repeat a data class getter or the implementation's exact lines.

### JVM Tests

- Use fake `SessionRepository` and `SpeechPlayer` implementations at domain boundaries.
- Use `runTest`, the test scheduler, `advanceTimeBy`, and `runCurrent` for timing behavior.
- Inject time and shuffle functions where deterministic control is required.
- Assert both state and side effects for concurrency and recovery tests.
- Cover cancellation at boundaries, especially during database commits and speech.
- Use backticked test names that state the trigger and expected result.

### Room Android Tests

- Use an in-memory Room database.
- Close the database after every test.
- Test foreign-key cascades, unique constraints, relationship mapping, expiry, pending speech, multiple sessions, and completion deletion.
- Add migration tests whenever the schema version changes.
- Do not replace Room integration tests with a fake repository when database behavior is the subject.

### Compose Android Tests

- Query by visible text or accessibility semantics before using test tags.
- Test whether actions are enabled, disabled, displayed, and wired to the correct callback.
- Test scrollable content at both the first and last relevant entries.
- Verify that observational UI such as the previous-number drawer does not fire playback or mutation callbacks.
- Test critical screens in both unfinished and completed session states.

### Physical Device Checks

Automated tests do not replace checks for:

- Offline voice installation and selection.
- Exact audible number phrasing.
- Speaker and Bluetooth routing.
- Incoming-call and audio-focus interruption.
- Headphone disconnection.
- Background/foreground transitions.
- Process-death recovery.
- Large fonts and narrow-screen layout.
- The full 90-number calling flow.

## Build and Dependency Standards

1. Declare dependency versions in `gradle/libs.versions.toml`.
2. Use stable dependency releases unless an approved feature requires a prerelease API.
3. Do not use dynamic versions such as `+` or version ranges.
4. Add the smallest dependency that solves the requirement. Prefer platform APIs and small native vectors over large libraries for trivial functionality.
5. Explain any dependency that adds networking, code generation, native binaries, background execution, analytics, or permissions.
6. Keep `minSdk`, `targetSdk`, namespace, and application ID centralized in `app/build.gradle.kts`.
7. Do not commit signing keys, keystores, passwords, machine-specific SDK paths, or secrets.
8. `local.properties` remains ignored.
9. Release minification changes require a release build and startup check.
10. Do not suppress lint globally. Suppress one finding at the narrowest location with a written reason.

## Quality Gates

A change is ready to hand off only when the checks relevant to it pass:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

Run `./gradlew :app:connectedDebugAndroidTest` when a device or emulator is available and the change affects Room, Compose interaction, Android lifecycle, or platform integration.

Minimum acceptance:

- Compilation succeeds without errors.
- Unit tests pass.
- Android test sources compile.
- Lint reports zero errors.
- New behavior has proportionate tests.
- Documentation matches the implementation.
- No architectural invariant is violated.

## Prohibited Patterns

Do not introduce any of the following:

- Business logic in `Activity`, composables, Room DAO methods, or TTS callbacks.
- Direct DAO, DataStore, or TTS access from a screen.
- `GlobalScope`, raw threads, blocking sleeps, or unmanaged coroutine scopes.
- More than one live playback or speech queue.
- `LiveData` mixed into the existing Flow-based state model without an Android interoperability requirement.
- Mutable domain objects or mutable collections exposed from a repository.
- `!!` in production code.
- Broad exception handling that consumes coroutine cancellation.
- Destructive Room migrations.
- Session state duplicated across Room, DataStore, files, or Compose state.
- Manual number drawing, reordered calling, or UI mutation of the saved sequence.
- Network-dependent core game behavior.
- Permanent storage of completed sessions under the current product rules.
- Background calling under the current lifecycle contract.
- Hard-coded spoken number phrases outside `NumberAnnouncementFormatter`.
- Hard-coded database or preference access outside the owning repository.
- New Android permissions without a documented feature and privacy review.

## Review Checklist

Before approving a change, confirm:

- The code is in the owning package and depends only on allowed layers.
- Domain behavior remains independent of Android framework types.
- State flows in one direction and mutable state stays private.
- Cancellation, failure, and process-recovery behavior are defined.
- Database writes preserve the draw-before-speech protocol.
- Readouts and history UI remain observational.
- User-facing controls have accessibility semantics.
- Timing values have explicit units and use domain constants or validation.
- Tests cover the relevant success, failure, and interruption paths.
- Room schema and migrations are correct when storage changes.
- Product, architecture, and code-standard documents remain consistent.
