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

## High-level architecture

Keep the application architecture simple and divided into domain models, two
application managers, one persistence component, and the JavaFX presentation
layer.

### Domain models

- `Module`, `ModuleCode`, and `ModuleDocument` represent the student's module
  records.
- `Requirement` and its subclasses represent editable degree requirements.
- `EvaluationResult` represents calculated requirement progress.
- Domain models must not depend on JavaFX or file-system details.

### `ModulesManager`

`ModulesManager` owns the in-memory module data and provides module-related
use cases:

- Add, edit, and delete modules.
- Search for modules and find modules by code.
- Mark modules as completed or incomplete.
- Validate operations using the domain model rules.
- Expose the current modules to `RequirementsManager` for evaluation.

### `RequirementsManager`

`RequirementsManager` owns the in-memory requirement hierarchy and provides
requirement-related use cases:

- Add, edit, and delete requirements.
- Add and remove child requirements.
- Edit requirement metadata, selectors, module lists, and count bounds.
- Load or replace the default requirement template.
- Evaluate requirements using the current modules.
- Return `EvaluationResult` objects for individual requirements or all root
  requirements.

The manager coordinates evaluation by calling
`Requirement.evaluate(Collection<Module>)`. It must not duplicate the
evaluation rules implemented by the requirement classes.

### `StorageManager`

Use one `StorageManager` for application persistence. It is responsible for:

- Loading modules and requirements from the local application data file.
- Saving modules and requirements to the local application data file.
- Resolving the data-file location beside the packaged executable on Windows
  and macOS.
- Loading the bundled default requirements on first launch.
- Handling missing, corrupted, invalid, or incompatible data files.
- Preserving or reporting corrupted user data instead of silently discarding
  it.

Format-specific conversion may be delegated to separate helper classes such
as `ModuleJsonMapper` and `RequirementJsonMapper`. These helpers only convert
between domain documents and JSON; they do not manage files, application
startup, or requirement evaluation.

### JavaFX presentation layer

The GUI may contain views and controllers that call `ModulesManager` and
`RequirementsManager`. Controllers translate user actions into manager calls
and display returned data. They must not contain requirement-evaluation rules,
JSON parsing, or file-system logic.

The application entry point may initialise `StorageManager`, load the data,
create both managers, and coordinate saving after successful changes. A
separate application-session or progress-service layer is not required unless
future requirements make it necessary.

### Component interaction

The intended dependency direction is:

```text
JavaFX views/controllers
        -> ModulesManager / RequirementsManager
        -> domain models

Application entry point
        -> StorageManager
        -> ModuleJsonMapper / RequirementJsonMapper
```

For progress updates, `RequirementsManager` receives the modules from
`ModulesManager`, evaluates the requirements, and returns `EvaluationResult`
objects. After a successful mutation, the application coordinates a save via
`StorageManager`.

## Build

Use Gradle 9.1 or later when running with Java 25:

```text
gradle clean test
gradle run
```

When the Gradle Wrapper is available, prefer `gradlew.bat` on Windows or
`./gradlew` on macOS/Linux.
