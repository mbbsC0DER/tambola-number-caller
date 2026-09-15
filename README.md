# Tambola

A native Android number caller built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Android text-to-speech. The caller follows the supplied sketch: back and speed controls above the previous/current number, playback button, readout menu, and 90-number board.

## Run

Open this directory in Android Studio, let Gradle sync, and run `app` on an Android 8.0+ device. Install an **offline English text-to-speech voice** in the device’s speech settings. The app prefers Indian English when available.

Command-line prerequisites:

- JDK 17 or later compatible with Gradle 8.14 (JDK 17 is used for verification).
- Android SDK platform 36 and build tools 35.0.0.
- Set `ANDROID_HOME` to the SDK directory, or let Android Studio create `local.properties`.
- Internet access for the first dependency download; the application itself has no Internet permission.

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

For the database and Compose tests, connect an Android device or start an emulator:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Release compilation: `./gradlew :app:assembleRelease`. Release signing keys are intentionally not included; configure your own signing before distribution.

## Game rules

- A new game shuffles 1–90 once, saving the complete order before playback.
- Once the voice is ready, each new session waits **1 second** before drawing and announcing its first number. This startup wait can be paused or cancelled.
- Live speech uses “one three, thirteen”; single digits use “single number one”.
- Live gaps default to **2 seconds**, adjustable from **1 to 6 seconds in 0.5-second steps**. The gap follows speech completion. Changes affect the next gap, not the gap already underway.
- Called/remaining readouts are sorted ascending, say whole numbers only, and always use a **0.6-second gap**. They never alter the draw order or called status.
- Tap the previous-number circle to open a dark, scrollable drawer from that side. Prior live numbers appear oldest first, excluding the current live number. Close it with Back, the drawer’s arrow, a swipe, or a tap outside; playback is unaffected.
- Readouts pause the game and leave it paused when stopped or finished.
- There is no manual draw action. Board cells are informational.
- Back quits the current screen. Unfinished sessions stay resumable, including manually quit sessions. A new game is always allowed, regardless of existing unfinished sessions.
- Completing all 90 announcements removes the persisted session but leaves the caller screen open. Repeat called numbers remains available until the user leaves.
- Expiry is **15 days from creation**, not from the last visit. Expired records are hidden and removed on launch/foreground access and during foreground maintenance. The active on-screen session is protected during maintenance; expired active data is removed when leaving it.
- No service continues calling in the background. Losing the foreground, an audio interruption, or headphone disconnection stops playback. Returning does not auto-resume. Rotation may pause the game.
- Database and preferences are excluded from Android cloud backup and device-transfer rules. Clearing app data or uninstalling removes local history.

## Recovery guarantee

Every draw is committed before display/speech. Speech completion is saved separately. A saved-but-unconfirmed announcement is retried before another number is drawn. An interrupted number can be **spoken again**, but is never **drawn or counted twice**. The 90th number is not discarded until its announcement completion is committed.

Database writes and audible speech cannot be one atomic operation. A successful TTS callback means that the engine completed playback; it cannot prove that a listener heard it (for example, device volume may be zero).

## Structure

```text
app/src/main/java/com/pratham/tambola/
  domain/          Session, formatter, repository/speech contracts, playback controller
  data/            Room entities/DAO/repository and DataStore theme preferences
  platform/        Offline TTS, utterance completion, audio focus, output interruptions
  presentation/    ViewModel, Navigation 3 host, Material screens/theme/native icons
```

The single ViewModel owns a single controller and speech player. The controller serializes transitions and cancels/joins previous playback before starting another operation. Room owns durable progress; UI-only modes and readout progress stay in memory. Manual constructor injection keeps these boundaries testable without a DI framework.

Room schemas live in `app/schemas`. Add a migration and migration tests before increasing the schema version; do not use destructive migration fallback for saved games.

## Tests and device checklist

Verification in this workspace: debug APK and Android test APK built successfully; all 14 JVM tests passed; Android lint reported no errors (dependency-update suggestions only). No device/emulator was connected, so instrumentation tests and audible playback have not been run here.

Unit tests cover all 90 unique draws, duplicate Play commands, interrupted speech, fixed readout pacing, readout isolation, live speed changes, session switching/recovery, foreground pausing, and failure handling.

Android tests cover Room persistence/expiry/completion cleanup and the completed caller’s controls. They require a device/emulator.

Before release, verify on a physical device:

- English voice setup and airplane-mode calling.
- Speaker and Bluetooth output, volume, phone-call interruption, and headphone disconnection.
- Home/lock/rotation pauses and interrupted-number recovery after process death.
- Large font sizes and narrow-screen scrolling.
- All 90 announcements finish while the screen remains open.

## Android references

- [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Speech completion callbacks](https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener)
- [Audio focus](https://developer.android.com/media/optimize/audio-focus)
- [Local backup policy](https://developer.android.com/identity/data/autobackup)
