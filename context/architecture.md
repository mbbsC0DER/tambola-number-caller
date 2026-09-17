# Tambola Number Caller — Architecture

## Purpose

This document defines the technical structure and non-negotiable rules for the Tambola Android application. It is the authority for layer ownership, dependency direction, persistence, lifecycle behavior, concurrency, and access control. Product behavior is defined in `context/project-overview.md`; this file defines where and how that behavior is implemented.

## Architectural Style

Tambola is a single-activity, single-module native Android application. It uses a layered MVVM-style architecture with a dedicated domain controller for the calling state machine:

```text
Compose UI
    ↓ user actions / observable state
TambolaViewModel
    ↓ commands
SessionController
    ↓ ports
SessionRepository          SpeechPlayer
    ↓                          ↓
RoomSessionRepository      AndroidSpeechPlayer
    ↓                          ↓
Room / SQLite              Android TTS + AudioManager
```

The presentation layer renders state and forwards intent. `SessionController` owns all calling, timing, pause, readout, and completion rules. Repositories own durable data operations. Android-specific speech and audio behavior stays behind the `SpeechPlayer` domain interface.

## Technology Stack

| Layer | Technology | Role |
|---|---|---|
| Platform | Android 8.0+, minimum API 26 | Target mobile operating system and minimum supported runtime |
| Build | Gradle 8.14 with Kotlin DSL | Project configuration, dependency resolution, compilation, packaging, and tests |
| Android build plugin | Android Gradle Plugin 8.11.1 | Android resource processing, APK creation, lint, and build variants |
| Language | Kotlin 2.2.20, JVM target 17 | Application, domain, persistence, platform integration, and test code |
| UI | Jetpack Compose with Compose BOM 2025.10.01 | Declarative screen and component rendering |
| Design system | Material 3 | Colors, typography, shapes, controls, drawers, sheets, menus, and theming |
| Navigation | Navigation 3.0.0 | In-app Home, Caller, and Settings destination display |
| Presentation state | AndroidX ViewModel 2.9.4, StateFlow, lifecycle-aware Compose collection | Retained screen state and unidirectional state delivery |
| Concurrency | Kotlin Coroutines 1.10.2 | Cancellable delays, serialized transitions, asynchronous database work, and speech completion |
| Relational persistence | Room 2.7.2 over app-private SQLite | Unfinished sessions, shuffled sequences, draw state, speech acknowledgement, and session timing |
| Preferences | Preferences DataStore 1.1.7 | Small user preferences; currently theme selection only |
| Speech | Android `TextToSpeech` | Offline English number announcements and utterance completion callbacks |
| Audio coordination | Android `AudioManager` and audio focus | Exclusive transient speech playback and interruption handling |
| Dependency injection | Manual constructor injection from `TambolaApplication` | Creates and shares the database, repositories, controller, and platform adapters without a DI framework |
| Code generation | KSP 2.2.20-2.0.4 | Room DAO and database implementation generation |
| Unit testing | JUnit 4 and `kotlinx-coroutines-test` | Deterministic domain formatter, timing, concurrency, and recovery tests |
| Android testing | AndroidX Test, Room testing, Compose UI testing | Database constraints, persistence, expiry, completion cleanup, and UI behavior |
| Static analysis | Android Lint | Android correctness, resources, manifests, and dependency diagnostics |

The application compiles with SDK 36 and targets SDK 36. Runtime user data must not depend on network access; the manifest intentionally declares no Internet permission.

## Module Boundary

The project has one deployable Gradle module: `app`.

This app is small enough that separate Gradle modules would add build and navigation overhead without improving ownership. Architectural separation is enforced through Kotlin packages and interfaces. A new Gradle module should be introduced only when a boundary needs independent compilation, reuse, ownership, or substantially different dependencies.

## System Boundaries

| Path | Owns | Must not own |
|---|---|---|
| `app/src/main/java/com/pratham/tambola/domain/` | Pure session models, playback state, constants, announcement formatting, repository and speech interfaces, and `SessionController` | Compose, Android UI classes, Room entities, DataStore keys, `Context`, `TextToSpeech`, or SQL |
| `domain/SessionController.kt` | The only live-calling state machine: new/resumed playback, delays, draws, acknowledgement, pause, readouts, interval changes, completion, cancellation, and errors | Screen layout, Room queries, Android lifecycle callbacks, or direct TTS calls |
| `domain/SessionRepository.kt` | Persistence contract used by the domain | Room annotations or database implementation details |
| `domain/SpeechPlayer.kt` | Speech preparation, one utterance, stop, audio-focus release, and disposal contract | Android TTS implementation details |
| `app/src/main/java/com/pratham/tambola/data/` | Room schema, DAO, transactions, entity/domain mapping, session TTL cleanup, and DataStore preferences | Compose rendering, navigation, TTS, or caller timing loops |
| `data/RoomSessionRepository.kt` | The durable session source of truth and all multi-table transaction boundaries | UI state, speech playback, or lifecycle decisions |
| `data/SettingsRepository.kt` | Theme preference serialization and observation | Session progress or caller state |
| `app/src/main/java/com/pratham/tambola/platform/` | Android framework adapters; currently TTS, installed-voice selection, utterance callbacks, audio focus, and noisy-output events | Session ordering, draw selection, readout ordering, or storage policy |
| `app/src/main/java/com/pratham/tambola/presentation/` | Compose screens, Material theme, navigation state, view-model coordination, transient messages, previews, and accessibility semantics | SQL, direct DAO access, persistence rules, randomization, or speech formatting |
| `presentation/TambolaViewModel.kt` | Connects lifecycle and UI intent to the controller, exposes flows, runs foreground cleanup, and converts failures into user-visible messages | Reimplementing controller state transitions or database transactions |
| `presentation/Screens.kt` | Stateless or locally stateful rendering and user-event callbacks | Launching database jobs, choosing numbers, managing the playback coroutine, or mutating domain state directly |
| `MainActivity.kt` | Hosts Compose and obtains the app-scoped ViewModel | Business logic or data storage |
| `TambolaApplication.kt` | Composition root and app-lifetime dependencies | Caller behavior or UI state |
| `app/src/main/res/` | Android resources, theme bridge, icon, strings, and backup exclusion rules | Business rules |
| `app/schemas/` | Version-controlled Room schema exports for migration review and testing | Runtime user data |
| `app/src/test/` | JVM domain tests with fake repositories, speech, and virtual time | Device-dependent integration assumptions |
| `app/src/androidTest/` | Room and Compose tests requiring an Android runtime | Pure domain behavior already testable on the JVM |

## Dependency Rules

| Source layer | May depend on | Must not depend on |
|---|---|---|
| Domain | Kotlin standard library, coroutines, domain interfaces and models | Android framework, Compose, Room, DataStore, presentation, concrete data or platform classes |
| Data | Domain contracts and models, Room, DataStore, coroutines | Presentation or platform speech implementation |
| Platform | Domain platform contracts and Android framework APIs | Data persistence or Compose screens |
| Presentation | Domain contracts/models, platform status exposed through the ViewModel, Compose, lifecycle, navigation | Room DAO/entities, SQL, DataStore keys, or direct `TextToSpeech` control |
| Composition root | Concrete data and platform implementations plus presentation factory | Product behavior |

Dependency direction points inward toward domain contracts. Concrete adapters implement domain interfaces; domain code never imports the adapters.

## Runtime Ownership

| State or resource | Owner | Lifetime |
|---|---|---|
| Room database | `TambolaApplication` | Application process |
| `RoomSessionRepository` | `TambolaApplication` | Application process |
| `SettingsRepository` | `TambolaApplication` | Application process |
| `TambolaViewModel` | Activity/ViewModel store | Until its navigation host/activity is destroyed permanently |
| `SessionController` | `TambolaViewModel` | Same as ViewModel |
| `AndroidSpeechPlayer` | `TambolaViewModel` | Same as ViewModel; explicitly closed in `onCleared` |
| Current `CallerState` | `SessionController` StateFlow | In memory while the ViewModel exists |
| Navigation route | `TambolaApp` saveable Compose state | UI instance/configuration recreation |
| Drawer, menu, sheet, and scroll state | Owning composable | Screen composition |
| Readout snapshot and progress | `SessionController` | One readout operation |
| Live playback coroutine | `SessionController` | One active Play operation |

## Command and State Flow

1. A composable emits a user intent through a callback, such as Play, Pause, Start, Resume, Repeat, or Set interval.
2. `TambolaViewModel` starts the command in `viewModelScope` and handles presentation-level busy or error state.
3. `SessionController` serializes the transition through its mutex and cancels any incompatible playback job.
4. The controller calls `SessionRepository` or `SpeechPlayer` through domain interfaces.
5. The repository completes its Room transaction before returning a domain `Session`.
6. The controller publishes a new immutable `CallerState` through StateFlow.
7. Compose observes the flow using lifecycle-aware collection and redraws from the new state.

Composable functions never become sources of truth for session progress. UI-local state is restricted to presentation concerns such as whether a menu, drawer, or bottom sheet is open.

## Calling State Machine

The supported playback modes are:

| Mode | Meaning | Permitted exit |
|---|---|---|
| `IDLE` | No session is loaded | Create or restore a session |
| `PREPARING` | Speech and audio focus are being prepared | Begin calling, Pause, lifecycle cancellation, or Error |
| `PAUSED` | An unfinished session is loaded with no active audio job | Play, start a readout, change interval, switch session, or Quit |
| `CALLING` | The live draw/speech loop is active | Pause, readout, lifecycle interruption, completion, switch session, or Error |
| `READING_CALLED` | A sorted snapshot of called numbers is being spoken | Stop, completion, another readout, lifecycle interruption, or Error |
| `READING_REMAINING` | A sorted snapshot of remaining numbers is being spoken | Stop, completion, another readout, lifecycle interruption, or Error |
| `ALL_CALLED` | All 90 live announcements completed; the snapshot remains on screen | Called-number readout or Quit |
| `ERROR` | Persistence or speech failed without discarding progress | Retry with Play, use another action, or Quit |

`SessionController` is main-dispatcher confined by its owner. Its mutex serializes commands, but it is never held for the duration of speech or a timer. This lets Pause cancel active work promptly.

Only one `playback` job can exist. Switching modes cancels and joins the old job, calls `SpeechPlayer.stop()`, and releases audio focus before starting another operation.

## Timing Model

| Timing | Value | Owner |
|---|---:|---|
| New-session initial delay | 1,000 ms after speech preparation | Domain controller constant |
| Default live gap | 2,000 ms after an announcement completes | Domain default and Room session row |
| Allowed live gap | 1,000–6,000 ms in 500 ms increments | Domain validation |
| Readout gap | 600 ms after each readout announcement except the last | Domain controller constant |
| TTS initialization timeout | 15,000 ms | Android speech adapter |
| Individual utterance timeout | 30,000 ms | Android speech adapter |
| Foreground expiry-maintenance interval | 30,000 ms | ViewModel |
| Unfinished-session TTL | 15 days from creation | Domain constant and persisted `expiresAt` |

All call and readout gaps begin after the preceding utterance reports completion. Speech duration is additional to the configured gap.

## Concurrency and Cancellation Model

- UI commands run in `viewModelScope`; leaving the ViewModel cancels them.
- The controller mutex serializes state-changing commands such as New, Restore, Play, Pause, Readout, Set interval, and Quit.
- Long speech and delay work runs in the single `playback` job outside the transition mutex.
- Pause cancels the job, stops TTS synchronously, joins the cancelled job, and releases audio focus.
- Database mutations that have started are completed in a non-cancellable section so in-memory state cannot diverge from a partially completed durable transition.
- A cancelled operation checks cancellation again before starting speech or selecting another number.
- Speech callbacks are marshalled onto the main thread and matched to a unique utterance ID. Callbacks for stale utterances are ignored.
- A readout copies its number list before playback. Session changes cannot mutate the sequence being read.
- Readout completion returns to `PAUSED` or `ALL_CALLED`; it never resumes live calling automatically.

## Storage Model

### Room Database

The app-private Room database is named `tambola.db`. It is the durable source of truth for unfinished sessions.

#### `sessions`

| Column | Type | Meaning |
|---|---|---|
| `id` | Text, primary key | Random UUID identifying one session |
| `startedAt` | Integer | UTC epoch milliseconds when the session was created |
| `expiresAt` | Integer | `startedAt + 15 days`; immutable session expiry |
| `gapMs` | Integer | Current live gap for this session; must be an allowed 500 ms step |

#### `session_numbers`

| Column | Type | Meaning |
|---|---|---|
| `sessionId` | Text, foreign key | Parent session identifier |
| `position` | Integer | One-based position in the session's shuffled sequence |
| `number` | Integer | Tambola number from 1 through 90 |
| `drawnAt` | Nullable integer | UTC epoch milliseconds when the number became the live number |
| `announcementCompletedAt` | Nullable integer | UTC epoch milliseconds when its live TTS utterance completed |

The composite primary key is `(sessionId, position)`. `(sessionId, number)` has a unique index. Deleting a session cascades to all 90 number rows.

### Database Transactions

- Session creation inserts the session and all 90 shuffled rows in one Room transaction.
- A live draw updates only the next undrawn position, then reloads the complete domain session.
- Announcement acknowledgement updates the current drawn row, then reloads the session.
- Acknowledging position 90 returns the completed snapshot to memory and deletes the database session in the same repository transaction.
- Loading a completed or expired session deletes it and returns no session.
- Expiry cleanup deletes expired sessions except the active on-screen session ID supplied as temporary protection.
- The repository maps database rows into the domain model and rejects non-contiguous draws or invalid acknowledgement order.

### DataStore

Preferences DataStore stores only small application preferences. It currently contains:

| Key | Values | Default |
|---|---|---|
| `theme` | `SYSTEM`, `LIGHT`, or `DARK` | `SYSTEM` |

Session data must never be moved into DataStore. The relational constraints and partial progress updates require Room.

### File Storage

The application does not store runtime user data in general-purpose files, external storage, media storage, or shared storage. It does not export audio files. Room's SQLite files and DataStore's private preferences file are implementation details managed inside the Android app sandbox.

`app/schemas/` contains generated Room schema JSON checked into the source tree. Those files describe database versions for review and migration tests; they never contain user sessions.

### In-Memory State and Cache

There is no general-purpose cache layer. The following state is intentionally memory-only:

- Current immutable `CallerState`.
- Playback mode and active coroutine job.
- Current readout snapshot, index, and displayed readout number.
- TTS initialization state and pending utterance completion.
- Audio-focus ownership.
- Open menu, drawer, bottom sheet, scroll, and navigation state.
- The completed 90-number session snapshot after its database rows have been deleted.

Memory-only state must be reconstructible from Room when the session is unfinished. A completed session is intentionally not reconstructible after process death.

### Cloud and Backup

There is no remote database, API, analytics upload, or synchronization service. `android:allowBackup` is false, and both backup-rule formats exclude root files, databases, preferences, and device transfer. App data remains on the current installation only.

## Source-of-Truth Rules

| Data | Source of truth |
|---|---|
| Unfinished session order and progress | Room |
| Whether a drawn number still needs its live announcement | `drawnAt` versus `announcementCompletedAt` in Room |
| Current session live gap | Room session row; mirrored in current `CallerState` |
| Saved-session list | Room Flow filtered to unexpired, incomplete sessions |
| Current playback/readout mode | `SessionController` StateFlow |
| Theme preference | DataStore |
| Current navigation destination | Compose saveable state |
| Installed voice and audio focus | Android speech/audio subsystem through `AndroidSpeechPlayer` |

No data may have two independent durable owners.

## Authentication and Access Model

### Authentication

The app has no accounts, sign-in, tokens, server sessions, or user identities. Authentication is not applicable because all functionality and data are local to one Android app installation.

### Authorization and Ownership

Android's application sandbox is the access boundary. Every session belongs implicitly to the current installation; there is no multi-user ownership column because there is no authenticated user model.

- The Room database and DataStore files use app-private storage.
- No `ContentProvider`, file-sharing provider, exported service, or exported receiver exposes session data.
- `MainActivity` is exported only as the launcher entry point.
- The app declares no Internet permission.
- Session IDs are internal UUIDs, not security credentials.
- Repository mutations operate only on sessions that exist in the private database and validate domain constraints before mutation.

If remote sync, sharing, accounts, or multiple principals are introduced later, this access model is no longer sufficient. That work requires explicit authenticated identities, ownership fields, authorization at every repository/API mutation boundary, migration of existing device-local data, and a separate threat model. It must not be added as an incidental extension of the current session ID.

## AI Model

The application contains no generative AI, machine-learning model, remote inference, prompt, embedding, or AI job. Number ordering uses an ordinary in-process shuffle. Android text-to-speech is a platform speech synthesizer, not an application-owned AI workflow.

An AI SDK or network inference dependency must not be added for number selection, formatting, speech ordering, or recovery. These operations are deterministic domain logic.

## Foreground and Background Task Model

The app performs no background calling.

- Live calling and readouts run only in the ViewModel's coroutine scope while the application is in the foreground.
- `ON_PAUSE` stops speech and pauses the controller.
- Returning to the foreground does not automatically resume playback.
- No foreground service, background service, WorkManager job, alarm, media session, or notification owns calling.
- The screen is kept awake only while live calling or a readout is active on the Caller screen.
- TTL cleanup runs on foreground access, periodically every 30 seconds while foregrounded, and at relevant create/quit/load boundaries.
- Expiry cleanup is opportunistic. No background scheduler is required because expired sessions are filtered from user-visible results immediately.

Any future requirement to continue audio with the screen locked or the app backgrounded is an architectural change. It requires moving playback ownership out of the ViewModel into an appropriate foreground media/service component, adding a notification and lifecycle contract, and revisiting the current pause invariants.

## Speech and Audio Boundary

`AndroidSpeechPlayer` owns all direct Android speech and audio APIs.

- It accepts fully formatted text; it does not decide how a number should be phrased.
- `NumberAnnouncementFormatter` owns live and readout wording in the domain layer.
- It selects an installed, offline English voice and prefers Indian English when available.
- It assigns a unique ID to every utterance and suspends until the matching completion callback.
- It uses `QUEUE_FLUSH` because only one domain-authorized utterance may be active.
- It requests transient audio focus immediately before playback and releases it after playback stops.
- It reports audio-focus loss and noisy-output events to the ViewModel/controller as a pause request.
- It explicitly stops TTS on cancellation and shuts the engine down when the ViewModel is cleared.
- It never writes synthesized speech to a file.

## Persistence and Recovery Protocol

A live number advances through two durable phases:

```text
Undrawn
  → Room commits drawnAt
  → UI displays number
  → TTS announces number
  → Room commits announcementCompletedAt
  → configured live gap
  → next draw
```

This ordering produces the following recovery behavior:

| Interruption point | Durable result | Recovery action |
|---|---|---|
| Before `drawnAt` commits | Number remains undrawn | Select it normally later according to sequence position |
| After `drawnAt`, before speech | Number is drawn and pending | Announce the same number before any new draw |
| During speech | Number is drawn and pending | Announce the same number again on Play |
| After speech, before acknowledgement commits | Number is drawn and pending | Announce it again; duplicate speech is safer than skipping it |
| After acknowledgement commits | Number is fully called | Wait the normal resume gap, then draw the next number |
| After position 90 acknowledgement | No durable session remains | Keep the completed snapshot only while the current screen/process survives |

Database state and audible sound cannot be committed atomically. The recovery guarantee is therefore exactly-once draw/count with at-least-once speech for an interrupted number.

## Expiry Model

- `expiresAt` is calculated once as `startedAt + 15 days`.
- Resume, Play, Pause, readouts, interval changes, and drawer access do not extend expiry.
- Room Flow results exclude expired sessions even before physical cleanup completes.
- The active on-screen session may be protected from periodic deletion so an interaction is not invalidated mid-screen.
- Once the user leaves an expired active session, unprotected cleanup removes it.
- TTL uses absolute epoch milliseconds. It does not depend on locale, daylight-saving changes, or formatted calendar dates.

## Error Model

- Expected platform and persistence failures become an `ERROR` playback state with a user-visible message.
- Cancellation is control flow and must always be rethrown; it must not be converted into an error message.
- A draw is never spoken if its persistence step fails.
- A speech failure leaves the current number pending.
- Retry reinitializes the speech engine when a voice or engine failure may have invalidated it.
- A missing or expired session returns the user to a recoverable navigation state instead of creating replacement progress silently.
- UI snackbars report operational failures; they do not become durable state.

## Database Evolution

- Room schema export remains enabled for every database version.
- Every schema version is committed under `app/schemas/`.
- Increasing the database version requires an explicit migration and an Android migration test.
- Destructive migration fallback is prohibited because it would silently erase unfinished sessions.
- Schema changes must preserve the 15-day TTL and pending-announcement recovery semantics unless the product specification is deliberately revised.

## Testing Boundaries

| Test type | Required coverage |
|---|---|
| JVM domain tests | Shuffle assumptions, number phrasing, first-call delay, live intervals, one playback job, Pause, readout ordering/timing, interrupted speech, database failure behavior through fakes, completion state, and session switching |
| Room Android tests | Atomic creation, 90 unique rows, pending-announcement reconstruction, cascade deletion, expiry, multiple sessions, and deletion after final acknowledgement |
| Compose Android tests | Caller controls, completed-state behavior, drawer order, drawer scrolling, disabled actions, and accessibility labels |
| Android lint | Manifest, resources, API usage, and Android correctness with zero errors |
| Physical-device checks | Offline voice selection, exact audible phrasing, speaker/Bluetooth routing, volume, phone-call interruption, headphone disconnection, background pause, process-death recovery, large text, and the complete 90-number flow |

Tests should target observable rules and failure boundaries. They must not merely mirror implementation details.

## Invariants

The following rules must never be violated:

1. **A session sequence is a permutation of 1 through 90.** It contains exactly 90 entries, every value is in range, and no number appears twice.
2. **The full shuffled sequence is durable before live playback can begin.** A partially created session must never be visible or playable.
3. **Only the next sequence position can be drawn.** Drawn positions are contiguous from position 1 through `drawnCount`; callers cannot skip, reorder, undo, or manually select a position.
4. **A draw commits before display and speech.** The app must never announce a newly drawn number that is not recoverable from Room.
5. **Speech completion is acknowledged separately.** A drawn row with no `announcementCompletedAt` is pending and must be retried before another number is drawn.
6. **A live number is counted at most once.** Recovery may repeat its speech, but it must never increment `drawnCount` or advance the sequence twice.
7. **Only one playback pipeline exists.** Live calling, called readout, remaining readout, and voice testing must never speak concurrently.
8. **Readouts are observational.** They must never mutate `drawnAt`, `announcementCompletedAt`, sequence order, counts, or expiry.
9. **The previous-number drawer is observational.** Opening, scrolling, and closing it must never pause playback or mutate domain state.
10. **Pause is authoritative.** After Pause, lifecycle loss, audio-focus loss, or output disconnection, no new draw or utterance may begin until the user explicitly resumes.
11. **Calling never continues in the background.** The ViewModel must not delegate the live loop to WorkManager, an alarm, or a service under the current product contract.
12. **Only unfinished sessions are durable.** The repository deletes a session only after all 90 live announcements are acknowledged, and completed sessions must not appear in saved history.
13. **The 90th number remains recoverable until acknowledgement commits.** Completion must not be inferred from `drawnCount == 90` alone while speech is pending.
14. **Multiple unfinished sessions are valid.** No uniqueness constraint, navigation guard, or cleanup policy may enforce a single active database session.
15. **Expiry never slides.** `expiresAt` is fixed at creation and is not extended by resume, playback, interval changes, readouts, or UI access.
16. **Room is the sole durable owner of session state.** Compose state, ViewModel fields, DataStore, and files must not become competing session stores.
17. **DataStore contains preferences only.** It must not hold shuffled sequences, progress, pending speech, or session TTL data.
18. **The UI does not access persistence or TTS directly.** Composables emit intent and render state; all session behavior flows through the ViewModel and controller.
19. **Domain logic remains Android-independent.** Number rules, timing constants, formatting, state transitions, and ports must not import Android, Compose, Room, or DataStore APIs.
20. **Concrete dependencies point toward domain interfaces.** Domain code must never import `RoomSessionRepository`, `AndroidSpeechPlayer`, Compose screens, or `TambolaViewModel`.
21. **Database mutations are transactional and cancellation-safe.** A cancelled coroutine must not leave durable progress committed without publishing the corresponding recoverable domain state.
22. **No persistence failure can skip a number.** If the draw write fails, the number is neither displayed nor spoken and the session pauses with an error.
23. **Session data remains device-local.** No session, preference, audio, or usage data is uploaded, backed up, shared, or transferred under the current architecture.
24. **There is no implicit user identity.** Session UUIDs are identifiers, not authentication or authorization tokens. Remote features require a new explicit auth and ownership architecture.
25. **Room schema changes are migrated.** Destructive migration fallback must never be enabled for production data.
26. **Timestamps are stored as epoch milliseconds.** Locale-formatted strings are presentation only and must never drive ordering or expiry.
27. **Cancellation is not an error.** `CancellationException` must be rethrown so structured concurrency can stop work promptly and predictably.
28. **A completed in-memory snapshot is disposable.** Process death after completion may remove it; code must not recreate permanent completed-session history without a product decision and schema change.
29. **Timing semantics remain explicit.** The one-second startup delay, configurable live gap, and fixed 600 ms readout gap occur after speech preparation or completion as defined; they must not be conflated with TTS speaking duration.
30. **The app requires no network to run a game.** New runtime features must not make session creation, calling, pausing, readouts, recovery, or expiry dependent on connectivity.

Any change that conflicts with an invariant requires an explicit update to the product overview, this architecture document, the relevant schema or interfaces, and tests before implementation.
