# User Guide

## Setup and launch

Install Java SE 25 and use the committed Gradle Wrapper. On Windows, run:

```text
gradlew.bat clean test
gradlew.bat run
```

On macOS/Linux, use `./gradlew` instead of `gradlew.bat`. JavaFX uses native
runtime binaries for the selected operating system. Linux systems must provide
GTK 3.20 or later.

To create the universal self-contained submission JAR:

```text
gradlew.bat clean test universalJar
```

The generated JAR is placed in `release/` and runs with Java SE 25 on supported
Windows, Linux, and macOS systems. The application data file is created beside
the JAR on first launch. Linux systems must provide GTK 3.20 or later.

## Current version

The application opens a JavaFX window with editable requirements and module
records. User-facing features will be documented here as they are implemented.

## Requirements

Use **Add** at the top of the Requirements panel to create a root requirement.
The form asks for a name, an optional description, a requirement type, and the
type-specific module or count details. Minimum module and unit counts may
be left blank and default to zero. Module codes must use letters followed by a
number, with optional trailing letters; prefixes must contain letters only.

When a composite requirement (**All child requirements** or **Any child
requirement**) is selected, use **Add child** in the requirement details panel
to add a nested requirement using the same form. Changes are saved to the
application data file after a requirement is added or edited.

During evaluation, completed modules are matched to specific requirements first.
Modules listed in **Specific modules** requirements are reserved from broad
module-count and unit-count requirements. A module is reused only when it is
explicitly listed in each **Specific modules** requirement that receives it.
Other completed modules are allocated to one specific requirement at a time;
modules not selected for any specific requirement are automatically counted as
unrestricted electives. The minimum unrestricted-elective units can be changed
by editing the **Unrestricted Electives** unit-count requirement.
The requirement details view lists the modules credited to the selected
requirement. A leaf lists only its own matching modules, while a composite
lists the union of modules credited to its child requirements. For **Degree
Total**, the view explains that all completed modules are observed without
being reserved.

Select any requirement and use the trash icon in the requirement details panel
to delete it. Deleting a composite requirement also deletes all of its child
requirements. Deletion must be confirmed before it is applied.

Select any requirement and use **Edit** in the requirement details panel to
change its name, description, or type-specific values. Module-count and
unit-count requirements allow their matching module codes, prefixes, and level
bounds to be edited. Leaf requirement types cannot be changed. Composite
requirements may be changed between **All child requirements** and **Any child
requirement**; their existing children are preserved.

## Modules

Use **Add module** at the top right of the Modules panel to record a module.
Enter its module code, name, and number of units. The number of units must be
between 1 and 60, and newly added modules start as incomplete. Changes are
saved to the application data file after a module is added.

Use the checkbox beside a module to mark it as complete or incomplete. The
change is saved to the application data file immediately.

Use the pencil icon beside a module to edit its code, name, or number of units.
The module's completion status is preserved when it is edited. Changes are
saved to the application data file after a module is edited.

Use the trash icon beside a module to delete it. Deletion must be confirmed
before it is applied and cannot be undone.
