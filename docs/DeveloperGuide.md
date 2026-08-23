# Developer Guide

## Prerequisites

- Java 21 or later
- Gradle 9.1 or later when running with Java 25

## Build and run

```text
gradle clean test
gradle run
```

If the Gradle Wrapper has been generated, use `gradlew.bat` on Windows or
`./gradlew` on macOS/Linux instead of `gradle`.

## Architecture direction

Keep the project divided into domain models, application services, persistence, and JavaFX presentation code. JavaFX controllers and views should not contain requirement-evaluation rules or file-format details.

## Persistence direction

Application data should eventually be stored in JSON in the same directory as the packaged executable. The storage location should be resolved by an infrastructure component rather than hard-coded in the UI.
