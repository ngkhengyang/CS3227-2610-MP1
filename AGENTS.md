# CS3227-2610-MP1

## Project

Degree-progress tracker for NUS Computing students. This is an individual Java desktop application built with JavaFX.

## Current scope

- Manage and search modules with units, completion status, and optional grades.
- Manage editable, user-defined degree requirements.
- Load a default degree-requirements template on first launch; loaded requirements remain editable.
- Support requirements based on selected modules, at least N selected modules, and at least N units.
- Show progress for individual requirements and the overall degree.
- Persist application data as JSON beside the packaged executable on Windows and macOS.

## Out of scope for the first release

Prerequisite checking, cohort-specific rules, what-if planning, chatbot interaction, and mandatory NUSMods integration.

## Development conventions

- Keep domain logic independent of JavaFX controllers and views.
- Prefer small, focused changes with tests for requirement evaluation and persistence.
- Do not add official curriculum assumptions without documenting their source and limitations.
- Required project documents: `docs/UserGuide.md`, `docs/DeveloperGuide.md`, and `docs/Reflections.md`.
- AI-assisted development notes belong in `logs/`.

## Build

Use Gradle 9.1 or later when running with Java 25:

```text
gradle clean test
gradle run
```

When the Gradle Wrapper is available, prefer `gradlew.bat` on Windows or
`./gradlew` on macOS/Linux.
