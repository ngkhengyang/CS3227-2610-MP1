# Developer Guide

## Prerequisites

- Java SE 25
- Gradle 9.1 or later when running with Java 25

## Build and run

```text
gradlew.bat clean test
gradlew.bat run
```

On macOS/Linux, use `./gradlew` instead of `gradlew.bat`.

## Release packaging

The `universalJar` task creates one self-contained JAR in `release/`. It
contains the application classes and resources, Jackson, and separate nested
JavaFX runtimes for Windows, Linux, and macOS. Its
`degreeprogress.UniversalLauncher` detects the host platform, extracts the
matching JavaFX JARs to a temporary directory, and loads
`degreeprogress.Launcher` in an isolated class loader.

Build it with:

```text
gradlew.bat clean test universalJar
```

The universal JAR supports Windows x64, Linux x86_64 and ARM64, and macOS
x86_64 and Apple Silicon. Linux systems must provide GTK 3.20 or later for
JavaFX. The optional `releaseJar` task still creates a smaller JAR for one
explicit target selected with `-PjavafxPlatform=win`, `linux`, or `mac`.

## Architecture direction

Keep the project divided into domain models, application services, persistence, and JavaFX presentation code. JavaFX controllers and views should not contain requirement-evaluation rules or file-format details.

The requirements UI uses `RequirementDialog` for root and child creation as
well as editing. Edit forms are pre-populated from the selected requirement,
preserve its ID, and restrict type choices to those accepted by
`RequirementsManager.editRequirement`. `RequirementsPanel` owns root insertion
and tree refresh, while `RequirementDetailsPanel` owns editing and child
insertion and deletion for the selected requirement. Deletion is delegated to
`RequirementsManager.deleteRequirement`, which removes either a root
requirement or a descendant from its composite parent. `MainWindow` coordinates
persistence after any mutation.

Requirement evaluation is coordinated by `RequirementAllocationEngine` in the
domain layer. It evaluates specific roots against selected completed modules.
Module codes found in `ModuleRequirement` leaves are treated as explicit claims:
count and unit leaves cannot reuse those codes, and non-explicit modules are
claimed by at most one specific root. Explicit overlap is possible only when a
module is listed in each `ModuleRequirement` that receives it. The reserved
`unrestricted-electives` root is evaluated against the remaining completed
modules, and the reserved `degree-total` root evaluates all completed modules
without consuming modules. `RequirementsManager` exposes the resulting progress
and allocation snapshot to the presentation layer.

`RequirementsPanel` computes one allocation snapshot when it refreshes, so all
completion indicators use the same matching decision. `RequirementDetailsPanel`
uses the manager's allocation snapshot to show the modules credited to the
selected requirement. A leaf shows only its own matching modules, while a
composite shows the union of its descendants' allocations. Selecting
`unrestricted-electives` shows the remainder modules. Allocation is runtime
evaluation data and is not added to the persisted requirement JSON.

## Persistence direction

Application data should eventually be stored in JSON in the same directory as the packaged executable. The storage location should be resolved by an infrastructure component rather than hard-coded in the UI.
