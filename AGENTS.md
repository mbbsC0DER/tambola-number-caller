# Repository Instructions

## Application Building Context

Read the following files in order before implementing, debugging, reviewing, or making a product, architecture, storage, UI, or dependency decision:

1. `context/project-overview.md` — product definition, goals, user flow, rules, features, scope, and success criteria.
2. `context/architecture.md` — technology stack, system boundaries, storage model, lifecycle, recovery strategy, and invariants.
3. `context/ui-context.md` — visual language, semantic colors, typography, spacing, shapes, layouts, components, icons, motion, and accessibility.
4. `context/code-standards.md` — Kotlin, Compose, coroutine, persistence, speech, testing, formatting, and review conventions.
5. `context/progress-tracker.md` — current phase, completed work, active work, open questions, risks, verification status, and next steps.

Do not begin implementation based only on the user prompt when these files provide relevant constraints. The latest explicit user instruction takes priority, but any resulting conflict with repository context must be reconciled in the documentation.

## Working Rules

1. Start from the Current Goal, In Progress, and Next Up sections of `context/progress-tracker.md` unless the user explicitly chooses different work.
2. Keep work within the In Scope and Out of Scope boundaries in `context/project-overview.md`.
3. Treat every invariant in `context/architecture.md` as mandatory. Do not silently weaken an invariant to simplify implementation.
4. Use only the visual tokens and component patterns in `context/ui-context.md` for UI work.
5. Follow `context/code-standards.md` for every new or materially edited source file.
6. Prefer the smallest change that fully satisfies the requirement. Do not combine feature work with unrelated dependency upgrades, formatting sweeps, or architectural rewrites.
7. Preserve unfinished sessions, pending-announcement recovery, automatic-only drawing, offline operation, and the no-background-calling rule.
8. Do not add ticket generation, authentication, cloud storage, networking, analytics, advertisements, or permanent completed-session history without an explicit product decision.
9. Do not expose secrets, signing material, user data, or machine-specific configuration in the repository.
10. Preserve user changes and unrelated work already present in the working tree.

## Documentation Maintenance

Update `context/progress-tracker.md` after every meaningful implementation, product, architecture, test, or release change. The update must include:

- What changed.
- What was verified and the exact result.
- What remains incomplete or unverified.
- Any new risk, blocker, question, or architectural decision.
- The next concrete action for a future session.

If a requested implementation changes documented behavior, architecture, storage, visual language, scope, or standards:

1. Identify the affected context file before coding.
2. Update that file as part of the same change.
3. Update `context/progress-tracker.md` after implementation and verification.
4. Do not leave documentation describing behavior the code no longer follows.

When context files disagree, do not guess. Resolve the conflict using this priority:

1. The user's latest explicit requirement.
2. `context/project-overview.md` for product behavior and scope.
3. `context/architecture.md` for system design and invariants.
4. `context/ui-context.md` for visual decisions.
5. `context/code-standards.md` for implementation conventions.
6. `context/progress-tracker.md` for status and sequencing.

Update every affected file when a higher-priority decision changes a lower-priority one.

## Implementation Workflow

1. Read all five context files.
2. Inspect the relevant implementation and tests before editing.
3. Confirm the intended change respects product rules and architecture invariants.
4. Implement within the owning package: `domain`, `data`, `platform`, or `presentation`.
5. Add or update tests in proportion to the behavioral risk.
6. Run the narrowest relevant checks while iterating.
7. Run the required handoff checks before declaring the work complete.
8. Update documentation and the progress tracker.
9. Report the outcome, verification, and remaining device-only checks clearly.

For diagnosis or review requests, inspect and report first. Do not modify application code unless the user also asks for a fix or implementation.

## Verification Baseline

Use JDK 17. The standard local quality gates are:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

Run this when an emulator or Android device is available and the change affects Room, Compose interaction, lifecycle behavior, or Android platform integration:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Run a release build before release handoff:

```sh
./gradlew :app:assembleRelease
```

Do not claim device speech, audio routing, interruption handling, process-death recovery, TalkBack behavior, or connected tests passed unless they were actually exercised on an appropriate target.

## Completion Standard

Work is complete only when:

- The requested behavior is implemented without violating project scope or architecture invariants.
- Relevant automated checks pass.
- Device-only validation is either completed or explicitly listed as outstanding.
- Context documentation matches the resulting implementation.
- `context/progress-tracker.md` contains an accurate handoff note.

## Workflow Document Status

`context/ai-workflow-rules.md` does not currently exist. The workflow and delivery rules that would belong there are defined in this `AGENTS.md` file so every referenced path remains valid. If a separate workflow document is added later, add it between `context/code-standards.md` and `context/progress-tracker.md` in the reading order and keep this entry point concise.
