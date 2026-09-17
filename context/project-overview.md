# Tambola Number Caller — Project Overview

## Overview

Tambola is a native Android application that runs a Housie/Tambola number-calling session without creating or managing tickets. For each session, it shuffles the numbers 1 through 90 once, automatically selects each number without repetition, displays the current and previously called numbers, and announces each live number through the device's offline English text-to-speech engine. The host can pause or resume calling, change the live-call interval, hear an ordered readout of called or remaining numbers, inspect the chronological call history, quit and resume unfinished sessions, and keep multiple unfinished sessions locally for up to 15 days.

## Goals

1. Call every number from 1 through 90 automatically, randomly, and exactly once per session.
2. Make the caller usable without a network connection by using an installed offline English text-to-speech voice.
3. Give the host direct control over playback through Play, Pause, a configurable live-call interval, readout controls, and Quit.
4. Make the current number, previous number, called count, remaining count, and full 1–90 board readable at a glance.
5. Let the host review earlier numbers in the exact order in which they were called.
6. Preserve every unfinished session locally so it can be resumed after quitting, closing the app, or a process interruption.
7. Allow a new session to start even when one or more unfinished sessions already exist.
8. Remove completed and expired sessions so the app stores only relevant unfinished games.
9. Prevent overlapping timers or speech queues when the host changes playback modes.
10. Present the experience with a clean Material 3 interface that works in light and dark themes.

## Core User Flow

1. The user opens the app and sees a **Start new session** action plus any unexpired unfinished sessions saved on the device.
2. The user either starts a new session or selects an unfinished session to resume.
3. For a new session, the app creates one shuffled sequence containing every integer from 1 through 90 exactly once and saves it locally before calling begins.
4. The app prepares an installed offline English text-to-speech voice. If no suitable voice is available, it pauses and directs the user to the device's speech settings.
5. After the voice is ready, a new session waits one second before drawing and announcing the first number.
6. The caller screen displays the current number, the immediately previous number, Play/Pause, the live interval control, the repeat/readout menu, the called and remaining counts, and the 1–90 number board.
7. While calling is active, the app announces the current number, waits for the announcement to finish, waits for the selected interval, and then draws the next number.
8. The user may pause or resume live calling at any time. The user may also change the live interval from 1 to 6 seconds in 0.5-second increments.
9. The user may tap the previous-number circle to open a dark drawer from the left side. The drawer lists all earlier numbers in original call order from top to bottom and can be scrolled without changing or pausing the session.
10. The user may choose **Repeat called numbers** or **Read remaining numbers** from the loop-icon menu. Starting either readout pauses live calling, reads a fixed snapshot in ascending numeric order, and leaves the session paused when it finishes or is stopped.
11. The user may leave before all 90 numbers are called. The unfinished session remains available on the home screen until it is resumed, completed, or reaches its 15-day expiry.
12. After the 90th live announcement finishes successfully, the screen remains open and displays that all 90 numbers have been called. Live Play is disabled, but the host may inspect the board, open the previous-number drawer, or repeat the called numbers.
13. When the user leaves a completed session, the app returns to the home screen. Completed sessions do not appear in saved sessions and are not retained in the local database.

## Rules

### Number Selection

1. A session uses only the integers 1 through 90.
2. The app shuffles all 90 numbers once when a new session is created.
3. Each number can be drawn only once in a session.
4. Numbers are drawn automatically; the interface does not provide a manual draw button.
5. The number board is informational. Tapping a board cell does not draw, announce, or change a number.
6. A readout never marks a number as called and never changes the saved shuffled order.

### Live Announcements

1. Single-digit numbers are announced as **“single number one”** through **“single number nine.”**
2. Two-digit numbers are announced digit by digit and then as a whole number. For example, 13 is **“one three, thirteen,”** 10 is **“one zero, ten,”** and 90 is **“nine zero, ninety.”**
3. A new session waits one second after the speech engine is ready before drawing and announcing its first number.
4. The default interval is 2 seconds.
5. The live interval can be set from 1 to 6 seconds in 0.5-second increments.
6. The live interval begins after the full speech announcement completes. It is not the total time between number selections.
7. A changed interval applies to the next interval that starts; it does not shorten or extend an interval already in progress.
8. An offline English voice is required. The app prefers an installed Indian English voice when one is available.

### Play, Pause, and Interruptions

1. Only one live-calling or readout operation may run at a time.
2. Repeated taps on Play must not create additional timers or overlapping announcements.
3. Pause stops the active delay and any speech currently in progress.
4. If a live announcement is interrupted, that same number remains pending and is announced again when the user resumes. It is not drawn or counted a second time.
5. Starting a called-number or remaining-number readout pauses live calling first.
6. Completing or stopping a readout leaves live calling paused. The user must press Play to resume the game.
7. Moving the app out of the foreground, losing audio focus, receiving an audio interruption, or disconnecting the active audio output pauses playback.
8. Returning to the app does not restart playback automatically.
9. Calling does not continue in the background and does not use a foreground service.

### Called and Remaining Readouts

1. **Repeat called numbers** uses a snapshot of the numbers already called when the readout begins.
2. **Read remaining numbers** uses a snapshot of the numbers not yet called when the readout begins.
3. Both readouts sort their snapshot in ascending numeric order.
4. Readouts announce whole numbers only. For example, 7 is **“seven”** and 13 is **“thirteen.”**
5. Readout speech does not use the **“single number”** prefix or digit-by-digit phrasing.
6. The pause after each readout announcement is fixed at 0.6 seconds and cannot be customized.
7. The live interval control has no effect on readout timing.
8. Read remaining numbers is unavailable when no numbers remain. Repeat called numbers is unavailable when no numbers have been called.

### Previous-Number Drawer

1. Tapping the previous-number circle opens a masked dark drawer from the left side of the caller screen.
2. The drawer contains all called numbers except the current live number.
3. Numbers appear in chronological call order, with the earliest call at the top and the most recent previous call at the bottom.
4. Every row includes the call position and its number.
5. The list is vertically scrollable.
6. Opening, scrolling, or closing the drawer does not pause playback or modify session progress.
7. The drawer can be closed with the system Back action, its close arrow, a swipe, or a tap outside the drawer.
8. The previous-number circle is disabled until there is at least one earlier number to show.

### Session Storage and Expiry

1. A session is unfinished while fewer than 90 live announcements have completed successfully.
2. An unfinished session is stored locally whether the user quits intentionally, closes the app, or the app process stops unexpectedly.
3. Multiple unfinished sessions may coexist.
4. The presence of unfinished sessions never prevents the user from starting a new session.
5. Each unfinished session expires exactly 15 days after its creation time.
6. Resuming or viewing a session does not extend its expiry.
7. Expired sessions are hidden and removed from local storage during normal app cleanup.
8. A session becomes complete only after the 90th live announcement reports successful completion.
9. A completed session is removed from persistent storage and is not shown in saved sessions.
10. Reaching 90 does not close or navigate away from the caller screen. The user leaves manually.
11. The completed session remains available in memory only while its caller screen is open.
12. All session data is stored in the app's private local database. The app has no cloud database, account, or synchronization feature.
13. Session data and preferences are excluded from Android cloud backup and device-to-device transfer.
14. Clearing app data or uninstalling the app permanently removes all saved sessions and preferences.

### Persistence and Recovery

1. The app saves the complete shuffled order when it creates a session.
2. Each drawn number is committed to the database before it is displayed and announced.
3. Speech completion is saved separately after the text-to-speech engine reports that the announcement finished.
4. If the app stops after saving a draw but before saving speech completion, that number is treated as pending and is announced again before another number is drawn.
5. Recovery may repeat an interrupted announcement, but it must never draw or count the number twice.
6. The 90th number remains recoverable until its announcement completion is committed.
7. A database or speech failure pauses playback and shows a retryable error instead of skipping a number.
8. Text-to-speech completion confirms that the speech engine finished playback; it cannot confirm that a person heard the announcement or that device volume was audible.

## Features

### Automatic Caller

- One randomized 1–90 sequence per session.
- Automatic, non-repeating draws.
- One-second initial delay for new sessions.
- Play and Pause controls.
- Live interval control from 1 to 6 seconds in 0.5-second increments.
- Current-number and previous-number displays.
- Called and remaining counters.
- Completion state that keeps the caller screen open after all 90 numbers.

### Voice and Audio

- Offline Android text-to-speech announcements in English.
- Indian English voice preference when installed.
- “Single number” phrasing for live single-digit calls.
- Digit-by-digit plus whole-number phrasing for live two-digit calls.
- Whole-number-only phrasing for readouts.
- Audio-focus handling and automatic pause on interruptions.
- Speech-engine status, voice test, and link to system speech settings.

### Number Review

- Dark left-side drawer containing previous numbers in original call order.
- Scrollable chronological history with call positions.
- Repeat called numbers in ascending order.
- Read remaining numbers in ascending order.
- Fixed 0.6-second readout interval.
- Readout progress and Stop control.

### Number Board

- A 10-column by 9-row board containing numbers 1 through 90.
- Distinct visual states for remaining, called, and current numbers.
- Read-only cells so session state cannot be changed accidentally.

### Saved Sessions

- Multiple unfinished sessions saved on the device.
- Resume an unfinished session with the same shuffled order, progress, pending announcement, and live interval.
- 15-day expiry measured from session creation.
- Automatic deletion of completed and expired sessions.
- Incremental persistence for process-death recovery.

### Interface and Settings

- Jetpack Compose interface using Material 3 components.
- Light, dark, and system theme options.
- Simplified caller screen without decorative captions or redundant subtitles.
- Screen kept awake while live calling or a readout is active.
- Accessible labels for interactive controls and number states.

## In Scope

- A native Android application for phones running Android 8.0 or later.
- Fully automatic random calling for numbers 1 through 90.
- Android offline English text-to-speech integration.
- Live playback control and configurable timing.
- Fixed-speed called and remaining audio readouts.
- The current-number, previous-number, number-board, and chronological drawer interfaces.
- Local persistence and recovery of multiple unfinished sessions.
- A 15-day time-to-live policy for unfinished sessions.
- Automatic removal of completed and expired sessions.
- Material 3 light and dark themes.
- Local unit tests, Room persistence tests, Compose interface tests, and Android lint checks for the defined behavior.

## Out of Scope

- Tambola or Housie ticket generation.
- Digital tickets, ticket marking, ticket scanning, or ticket storage.
- Winner, line, house, or claim verification.
- Manual number selection or manual next-number calling.
- Reordering, editing, undoing, or deleting individual called numbers.
- Multiplayer rooms, remote callers, device-to-device synchronization, or audience participation.
- User accounts, authentication, profiles, or social features.
- Cloud databases, cloud history, cloud backups, or cross-device restore.
- Background calling, foreground-service playback, lock-screen media controls, or notifications for calling.
- Permanent history for sessions in which all 90 announcements completed.
- Custom readout speed or readout ordering.
- Downloading or bundling a text-to-speech voice inside the app.
- iOS, web, desktop, tablet-specific, Wear OS, or television versions.
- Ticket-related analytics, advertising, purchases, or subscriptions.

## Success Criteria

The project is considered complete when all of the following are true:

1. A new session saves one valid shuffled permutation of 1 through 90 before playback starts.
2. Live calling can announce all 90 numbers automatically without a duplicate or out-of-range value.
3. The first call in every new session starts no earlier than one second after the voice is ready.
4. Live single-digit and two-digit announcements follow the exact required wording.
5. The live interval defaults to 2 seconds and accepts every 0.5-second value from 1 through 6 seconds.
6. Play, Pause, rapid repeated taps, and audio interruptions never create overlapping timers or speech.
7. Called and remaining readouts use whole-number speech, ascending order, and a fixed 0.6-second pause without changing session progress.
8. The previous-number drawer opens from the left, uses a dark background, preserves chronological call order, scrolls correctly, and does not affect playback.
9. The current number, previous number, counts, and all 90 board cells show the correct state throughout a session.
10. The user can start a new session while any number of unfinished sessions exist.
11. Quitting or interrupting a session before completion preserves its exact order, progress, pending announcement, interval, creation time, and expiry.
12. Every unfinished session remains resumable until 15 days after creation and is unavailable after expiry.
13. Completing the 90th announcement keeps the caller screen open, disables further live draws, and removes that session from persistent storage.
14. No session data is sent to a server, included in Android backup, or restored through device transfer.
15. Backgrounding or losing audio focus pauses playback, and returning to the app requires the user to press Play.
16. Database failures, speech failures, and interrupted announcements pause safely without losing or double-counting a number.
17. The debug app and Android test APKs compile, unit tests pass, instrumentation tests pass on a supported Android device or emulator, and Android lint reports no errors.
18. A physical-device check confirms offline speech, audible phrasing, speaker and Bluetooth behavior, interruption recovery, large-text usability, drawer scrolling, and the complete 90-number flow.
