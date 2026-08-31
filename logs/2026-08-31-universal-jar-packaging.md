# Universal JAR packaging summary

## Prompt and objective

The release JAR must run on Windows, Linux, and macOS, and the JAR will be
published as a single GitHub release artifact.

## Engineering decision

JavaFX includes platform-specific native libraries, so merging all JavaFX
platform JARs into one flat classpath is unsafe because the platform JARs share
classes. The build now places each JavaFX runtime in a separate nested
`platform/<platform>/` directory inside one universal JAR.

`UniversalLauncher` detects the operating system and architecture, extracts
the matching nested JavaFX JARs to a temporary directory, and loads the normal
application launcher through an isolated `URLClassLoader`. Supported targets
are Windows x64, Linux x86_64/ARM64, and macOS x86_64/Apple Silicon.

## Verification

- Gradle 9.7.1 completed `clean test universalJar` on Java 25.
- The universal JAR contains 15 JavaFX runtime JARs, covering three JavaFX
  modules across five supported platform targets.
- The universal JAR stayed running for five seconds during a Windows launch
  smoke test.
- Linux and macOS runtime payloads were inspected in the artifact but require
  native machines or CI runners for full GUI launch verification.
