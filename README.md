# CS3227-2610-MP1

An editable degree-progress tracker for NUS Computing students.

The application will help students record their modules and evaluate their progress against editable degree requirements. A bundled requirement template is loaded on first launch, but the resulting requirements use the same editing and evaluation features as user-created requirements.

## Status

The base JavaFX application is set up. It currently opens an empty application window; application features and UI will be added incrementally.

## Requirements

- Java 21 or later
- Gradle 9.1 or later when running with Java 25

## Build and test

From the project root:

```text
gradle clean test
```

Gradle downloads the JavaFX and JUnit dependencies needed by the project.

## Launch the GUI

```text
gradle run
```

The current base version opens an empty JavaFX window.

## Gradle Wrapper

For a reproducible build, generate and commit the Gradle Wrapper once Gradle is installed:

```text
gradle wrapper --gradle-version 9.1.0
```

After that, use these commands instead:

```text
gradlew.bat clean test
gradlew.bat run
```

On macOS or Linux, use `./gradlew` instead of `gradlew.bat`.

## Project structure

```text
src/
  main/
    java/degreeprogress/
      gui/                 # JavaFX application and entry point
      models/
        modules/            # Module data models
        requirements/       # Requirement data models
      storage/             # Persistence and JSON conversion
    resources/
      application.properties
      default-requirements.json
  test/
    java/degreeprogress/
      models/              # Domain model tests
      storage/             # Persistence tests
docs/                     # User and developer documentation
logs/                     # AI-assisted development and reflection logs
build.gradle              # Gradle build configuration
settings.gradle           # Gradle project name
```

## Scope boundaries

The initial release does not include prerequisite checking, cohort-specific rules, what-if planning, chatbot interaction, or mandatory NUSMods integration.

## Documentation

- [Agent guidance](AGENTS.md)
- [User Guide](docs/UserGuide.md)
- [Developer Guide](docs/DeveloperGuide.md)
- [Reflections](docs/Reflections.md)
