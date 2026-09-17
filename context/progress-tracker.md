# Tambola Number Caller — Progress Tracker

Last updated: 17 September 2026, Asia/Kolkata

Update this file after every meaningful implementation, product, architecture, testing, or release change. Keep the current sections accurate and add a dated note under Session Notes. Do not mark work complete until the relevant verification has passed.

## Current Phase

- MVP feature implementation is complete.
- The project is in validation, standards-alignment, and release-polish phase.

## Current Goal

- Bring the existing UI implementation into full alignment with `ui-context.md` and `code-standards.md`.
- Run instrumentation and physical-device validation, especially offline text-to-speech and interruption recovery.
- Produce a release-ready build after those checks pass.

## Status Snapshot

| Area | Status | Evidence or remaining work |
|---|---|---|
| Product requirements | Complete | Defined in `project-overview.md` |
| Architecture | Complete | Defined in `architecture.md` |
| Code standards | Complete | Defined in `code-standards.md` |
| Visual system | Complete as specification | Defined in `ui-context.md`; implementation alignment remains |
| Core calling engine | Implemented and unit-tested | 15 JVM tests pass |
| Local persistence | Implemented | Room Android tests compile; device execution remains |
| Compose UI | Implemented | Caller Compose tests compile; device execution and visual review remain |
| Offline speech integration | Implemented | Requires physical-device audio and voice validation |
| Debug build | Passing | Verified 17 September 2026 |
| Android test APK | Passing build | Verified 17 September 2026 |
| Android lint | Passing with warnings | 0 errors and 19 dependency-version warnings |
| Connected instrumentation tests | Not run | No emulator or device was connected during the latest verification |
| Release signing/distribution | Not configured | Signing material and distribution target have not been provided |

## Completed

### Project Foundation

- [x] Created a native Android application using Kotlin, Jetpack Compose, and Material 3.
- [x] Configured Java 17 bytecode, Gradle Kotlin DSL, version catalog dependencies, and Android SDK 26–36 support.
- [x] Added Navigation 3 destinations for Home, Caller, and Settings.
- [x] Kept the app offline-only with no Internet permission, cloud database, account, analytics, or backend.
- [x] Disabled Android cloud backup and device transfer for app data.

### Session and Calling Engine

- [x] Shuffle all integers from 1 through 90 once per session with no repetition.
- [x] Save the entire shuffled order before calling starts.
- [x] Start new sessions even when other unfinished sessions exist.
- [x] Automatically call numbers without a manual draw action.
- [x] Wait 1 second before the first number of every new session.
- [x] Default live playback gap to 2 seconds.
- [x] Allow live gaps from 1 through 6 seconds in 0.5-second increments.
- [x] Apply live gaps after speech completion.
- [x] Support Play, Pause, foreground pause, audio-interruption pause, and headphone-disconnection pause.
- [x] Keep the completed caller screen open after the 90th announcement until the user quits.
- [x] Prevent completed sessions from being persisted as history.

### Speech and Readouts

- [x] Implement Android text-to-speech behind the domain `SpeechPlayer` interface.
- [x] Prefer an installed offline English voice and prioritize Indian English when available.
- [x] Announce multi-digit live numbers digit by digit and then as a whole, for example “one three, thirteen.”
- [x] Announce live single digits as “single number one” through “single number nine.”
- [x] Announce readout numbers as whole numbers only.
- [x] Read called and remaining snapshots in ascending numeric order.
- [x] Use a fixed 600 ms pause after each readout announcement except the last.
- [x] Pause live calling before a readout and leave the session paused afterward.
- [x] Use a loop icon for the readout action.
- [x] Serialize speech so live calls, readouts, and voice tests cannot overlap.

### Persistence and Recovery

- [x] Store unfinished sessions locally using Room.
- [x] Store theme preference using DataStore.
- [x] Persist session creation time, 15-day expiry, live gap, complete shuffled order, draws, and speech acknowledgements.
- [x] Preserve multiple unfinished sessions independently.
- [x] Expire sessions exactly 15 days after creation.
- [x] Hide expired sessions immediately and remove them during foreground cleanup.
- [x] Persist each draw before display and speech.
- [x] Track speech completion separately from drawing.
- [x] Retry a persisted pending announcement before drawing a new number.
- [x] Delete the Room session only after the 90th live announcement is acknowledged.
- [x] Retain the completed 90-number snapshot in memory until the caller screen is left.
- [x] Export and commit Room schema version 1.

### User Interface

- [x] Implement Home, Caller, Settings, speed bottom sheet, readout menu, and snackbar/error feedback.
- [x] Show the current number, previous number, playback control, pace, status, counts, and 1–90 board on the Caller screen.
- [x] Apply distinct current, called, and remaining board states.
- [x] Open a masked dark drawer from the previous-number side control.
- [x] Show previous calls in original call order from top to bottom.
- [x] Exclude the current number from the previous-number list.
- [x] Make the previous-number list independently scrollable and observational only.
- [x] Support system, light, and dark theme preferences.
- [x] Add native vector icons without a separate icon-font dependency.
- [x] Remove previously rejected decorative caller captions and subtitles.
- [x] Add light and dark caller previews.

### Automated Verification

- [x] Cover calling order, uniqueness, completion, duplicate Play prevention, pending recovery, readout isolation, timing, speed changes, foreground behavior, session switching, and failures with JVM tests.
- [x] Cover number wording and valid speed values with formatter tests.
- [x] Add Room instrumentation tests for multiple sessions, pending recovery, expiry cascade, and final cleanup.
- [x] Add Compose instrumentation tests for completed-session controls and the previous-number drawer.
- [x] Build the debug APK successfully.
- [x] Run 15 JVM tests with zero failures.
- [x] Build the Android instrumentation-test APK successfully.
- [x] Run Android lint with zero errors.

### Project Documentation

- [x] Create `project-overview.md` with scope, rules, flows, features, and success criteria.
- [x] Create `architecture.md` with the stack, boundaries, storage, lifecycle, recovery, and invariants.
- [x] Create `code-standards.md` with Kotlin, Compose, coroutine, persistence, speech, test, and review rules.
- [x] Create `ui-context.md` with complete light/dark palettes, typography, shapes, spacing, components, layouts, icons, motion, and accessibility rules.
- [x] Create this progress tracker with the current implementation and verification status.
- [x] Create root `AGENTS.md` as the mandatory context entry point and workflow guide.

## In Progress

- [ ] Align `Theme.kt` with the complete light and dark token tables in `ui-context.md`; several unspecified Material tokens currently fall back to library defaults.
- [ ] Define the documented typography roles and reusable semantic UI tokens in code.
- [ ] Move release-facing hard-coded Compose strings into Android string resources as screens are standardized.
- [ ] Normalize existing Kotlin formatting and imports to `code-standards.md` without changing behavior.
- [ ] Perform emulator or physical-device execution of Room and Compose instrumentation tests.

## Next Up

1. Complete theme-token and typography alignment without changing the approved visual direction.
2. Replace reusable and release-facing hard-coded UI strings with resources and preserve all accessibility descriptions.
3. Run `connectedDebugAndroidTest` on an emulator or Android device and repair any Room or Compose failures.
4. Perform the physical-device speech and lifecycle checklist in airplane mode.
5. Test the complete 90-number flow, including staying on the caller screen and repeating called numbers after completion.
6. Verify narrow-screen, large-font, light-theme, dark-theme, scroll, and TalkBack behavior.
7. Run a release build, configure signing outside the repository, and decide the distribution channel.
8. Commit the currently untracked `context/` documentation directory.

## Open Questions

- Should workflow guidance remain in root `AGENTS.md`, or should it later be extracted into a separate `context/ai-workflow-rules.md` file?
- What is the intended first distribution channel: local APK, private testing, or Google Play?
- What release signing configuration will be used? No keystore or credential should be committed to this repository.
- Is `Tambola` the final public app name and is `com.pratham.tambola` the final application ID?
- Is the current generated launcher icon acceptable, or will final brand artwork be supplied before release?
- Which physical Android devices and TTS engines must be included in the acceptance matrix?

No product-rule question currently blocks implementation. The session, timing, persistence, readout, completion, and background behavior requirements are resolved in `project-overview.md`.

## Architecture Decisions

- Use a single Android application module with `domain`, `data`, `platform`, and `presentation` package boundaries.
- Use MVVM with one `TambolaViewModel`; `SessionController` owns the calling state machine.
- Use manual constructor composition rather than a dependency-injection framework at the current project size.
- Use immutable `CallerState` over `StateFlow` and structured coroutines for state and playback.
- Keep Android speech and audio APIs behind `SpeechPlayer`; domain code contains no Android framework types.
- Use Room as the sole durable source for unfinished session state and DataStore only for preferences.
- Use a pre-generated shuffled sequence plus positional rows so order and crash recovery are deterministic.
- Commit a draw before speech, then acknowledge speech completion separately; a pending number may repeat but must never be skipped or counted twice.
- Allow multiple unfinished sessions and always permit a new session.
- Delete completed sessions after the 90th acknowledgement; do not create permanent completed-session history.
- Expire unfinished sessions 15 days from creation, not from last access.
- Keep the active expired session protected until the host leaves it, while hiding and cleaning other expired rows.
- Do not run calling in the background and do not add a foreground service.
- Pause on app backgrounding and audio interruption; never resume automatically.
- Keep readouts observational, sorted ascending, fixed at a 600 ms post-speech gap, and isolated from session progress.
- Use fixed Tambola light/dark palettes rather than Material dynamic color.
- Use native vector icons and Material components; avoid new UI libraries unless a documented need appears.
- Keep all user data app-private, local-only, and excluded from backup and transfer.

## Known Risks and Gaps

- Android TTS behavior varies by engine, installed voice, manufacturer, speaker route, and OS version; automated tests cannot prove audibility.
- A successful TTS callback confirms engine completion, not that a listener heard the output or that device volume was nonzero.
- Instrumentation tests have compiled but have not run on a connected target in the latest session.
- Physical-device interruption, Bluetooth, speaker, offline-voice, process-death, and TalkBack behavior remain unverified.
- The current Compose theme does not yet explicitly assign every color token specified by `ui-context.md`.
- Current screens contain hard-coded English strings and compact formatting that predate `code-standards.md`.
- Gradle reports deprecation compatibility warnings for Gradle 9 and dependency-update suggestions. These are non-blocking; upgrades should be handled deliberately rather than mixed into feature work.
- The entire `context/` directory is currently untracked in Git.

## Verification Commands

Use JDK 17 and run:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

With an emulator or device connected:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Before release:

```sh
./gradlew :app:assembleRelease
```

## Session Notes

### 17 September 2026 — Documentation baseline and verification

- Audited the implementation, build configuration, Room schema, tests, README, and all five context documents.
- Added the initial progress tracker based on actual repository state rather than an empty project template.
- Verified `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest`, and `lintDebug` together using JDK 17.
- Gradle completed successfully: 79 actionable tasks, 18 executed, and 61 up to date.
- Test reports contain 15 JVM tests: 13 `SessionControllerTest` cases and 2 `NumberAnnouncementFormatterTest` cases, with zero failures or errors.
- Lint completed with 0 errors and 19 warnings. The warnings are dependency or tool-version update notices, not correctness failures.
- Did not run connected instrumentation tests because no emulator or physical device was connected.
- No application source code was changed during this tracker update.
- The local shell does not currently expose Java through `PATH` or `JAVA_HOME`; verification used `/home/pratham/.bubblewrap/jdk/jdk-17.0.11+9`.
- Resume from **In Progress**, beginning with complete theme-token alignment, unless the user chooses a different Next Up item.

### 17 September 2026 — Agent entry point

- Added root `AGENTS.md` and made it the entry point for product, architecture, UI, code-standard, and progress context.
- Added concrete scoping, documentation-maintenance, implementation, verification, and completion rules.
- Did not reference a nonexistent sixth context file. The proposed `context/ai-workflow-rules.md` has not been created; essential workflow rules remain directly in `AGENTS.md` and extraction is recorded as an open question.
- No application source code changed, so the previously passing build and test status remains applicable.
