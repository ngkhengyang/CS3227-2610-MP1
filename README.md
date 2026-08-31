# CS3227-2610-MP1

An editable degree-progress tracker for NUS Computing students.

The application will help students record their modules and evaluate their progress against editable degree requirements. Bundled sample requirements and a small, non-exhaustive set of modules are loaded on first launch for demonstration. The sample module codes are drawn from codes referenced by the default requirements, while names are supplied for display; this is not an authoritative or complete curriculum list.

## Requirements

- Java SE 25
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

The application uses the JavaFX binaries for the current operating system.

## Package a release JAR

The release task creates one self-contained universal JAR containing the
application, Jackson, and JavaFX runtimes for Windows, Linux, and macOS. The
launcher detects the host platform and loads only its matching JavaFX runtime:

```text
gradlew.bat clean test universalJar
```

The universal JAR is written to `release/` and can be run on supported systems
with Java SE 25:

```text
java -jar release/degree-progress-tracker.jar
```

The universal JAR supports Windows x64, Linux x86_64 and ARM64, and macOS
x86_64 and Apple Silicon. Linux systems must provide GTK 3.20 or later.

The optional platform-specific task is useful for debugging or producing a
smaller artifact:

```text
gradlew.bat clean test releaseJar -PjavafxPlatform=win
```

## Gradle Wrapper

The Gradle Wrapper for Gradle 9.1.0 is committed to the repository. Use
`gradlew.bat` on Windows and `./gradlew` on macOS/Linux.

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
      default-modules.json
  test/
    java/degreeprogress/
      models/              # Domain model tests
      storage/             # Persistence tests
release/                   # universal GitHub-release JAR
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
