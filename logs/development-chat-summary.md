# Development chat summary

This is a consolidated summary of the accessible development conversations for
`CS3227-2610-MP1`. It records the main prompts, decisions, implementation
outcomes, and verification reported during development.

## Project direction and setup

### Project idea and scope

The project began as a discussion of a Java desktop utility for tracking degree
progress for NUS Computing students. The scope was refined to an interactive
JavaFX GUI with editable modules, editable user-defined requirements, progress
calculation, search, and JSON persistence beside the packaged executable. A
bundled BComp Computer Science curriculum is only an editable first-launch
template. The features and scope of the project is determined from this conversation. As such, prerequisite checking, cohort-specific rules, what-if planning, chatbot interaction, and mandatory NUSMods integration were left out of the
first release.

### Initial project setup

The base JavaFX project was created with documentation placeholders, a test
layout, an empty application window, and a Gradle/Maven build setup. The Java
package was later simplified from `com.cs3227.degreeprogress` to
`degreeprogress`. Maven was then replaced by Gradle, with JavaFX, JUnit, the
Gradle Wrapper, and commands for testing and launching the GUI documented in
the README and developer guide.

### Environment and dependency troubleshooting

Several conversations diagnosed unresolved JUnit and JavaFX imports in VS Code.
The conclusion was that Gradle dependency resolution was working and that the
remaining issue was project import/language-server configuration. Follow-up
guidance covered Java 25, `JAVA_HOME`, Gradle refresh, and cleaning the Java
language-server workspace.

## Domain model and persistence

### Requirement data model

The initial proposal used recursive requirements with `ALL`/`ANY` groups and
leaf requirements. This was changed to a polymorphic hierarchy:

- `Requirement` and `CompositeRequirement`
- `AllOfRequirement` and `AnyOfRequirement`
- `ModuleRequirement`
- `ModuleCountRequirement`
- `UnitCountRequirement`
- `ModuleSelector` and `EvaluationResult`

The model supports module-code requirements, selected-module counts, selected-
unit counts, and nested composite requirements. The implementation and tests
were verified through Gradle during the relevant development stages.

### Requirement JSON format and defaults

Requirements were given a discriminator-based JSON format supporting
`module`, `moduleCount`, `unitCount`, `allOf`, and `anyOf`. A representative,
editable BComp Computer Science template was added with common curriculum,
general education, foundation, mathematics/science, breadth/depth, focus-area,
industry-experience, and unrestricted-elective requirements. The format and
limitations were documented in `docs/RequirementDataFormat.md`.

### Module data model

`ModuleCode` was introduced as an immutable value object. It normalises codes,
extracts alphabetic prefixes such as `CS` or `CP`, and infers the module level
from the first digit. A separate stored level and grade were not retained in
the first release. `Module` stores module identity, name, units, and completion
state; `ModuleDocument` provides versioned collection storage and duplicate-code
validation.

### JSON serializers and combined storage

The first storage implementation added `RequirementStorage` for parsing and
serialising requirement documents. It was later renamed to
`RequirementsSerializer`, and `ModuleSerializer` was added. `ApplicationData`
aggregates modules and requirements in memory so `StorageManager` can persist
both in one JSON file. `StorageManager` handles the local data-file path,
bundled defaults, schema validation, and atomic saves. Serializer and storage
tests covered JSON round trips, nested requirements, defaults, and invalid
data.

## Managers and evaluation

### `ModulesManager`

The manager was built incrementally to support:

- adding and deleting modules;
- editing module code, name, units, and preserving completion state;
- preset-module construction for isolated tests;
- marking modules complete or incomplete without changing list order;
- case-insensitive substring search over code and name;
- duplicate, missing-field, code-format, and unit-boundary validation.

Module units were constrained to 1 through 60 inclusive, with boundary tests
for values below, at, and above those limits.

### `RequirementsManager`

The manager was added with empty and preset-list constructors, then extended to
add requirements, add nested children, delete root or nested requirements,
and edit requirements. Deletion was refactored around unique requirement IDs;
later tests and implementation separated root-list removal from parent-child
removal where appropriate.

The editing policy became:

- leaf requirements may only be edited as the same leaf type;
- `AllOf` and `AnyOf` may be exchanged while preserving ID, metadata, children,
  and child order;
- leaf/composite conversion and other leaf-type conversions are rejected;
- failed edits do not mutate the existing requirement.

### Degree progress evaluation

Progress evaluation was kept inside `RequirementsManager`, while
`ModulesManager` remains unaware of requirements. The evaluator builds one
shared evaluation context and returns `EvaluationResult`/`DegreeProgress`
objects for individual requirements, roots, and the overall degree. Selector
results are indexed or memoised so repeated GUI refreshes do not evaluate each
row independently. Tests cover nested requirements, ordering, completed and
incomplete states, module changes, invalid input, and context behaviour.

### Allocation and double-counting rules

The later allocation work changed evaluation from simple selector matching to
allocation-aware evaluation without adding new user-configurable requirement
attributes. Existing IDs define the special cases:

- `unrestricted-electives` receives completed modules left over after specific
  requirements claim modules;
- `degree-total` evaluates all completed modules but consumes none;
- other roots use specific allocation.

Explicit module requirements reserve their listed codes before broad count/unit
requirements are evaluated. Non-explicit modules are credited to at most one
specific requirement, while explicit overlap is allowed when a module is
listed explicitly in each relevant requirement. Maximums cap credited modules
or units instead of failing merely because additional modules match. The UI
uses the same allocation snapshot as evaluation, so leaf selections show their
own credited modules, composites show the union of descendant allocations,
and unrestricted electives show the remainder.

## JavaFX GUI development

### Main window and requirements panel

The GUI was refactored into a launcher outside the GUI package, a `MainWindow`,
an application toolbar, a requirements panel, and a requirement-details panel.
The requirements panel displays root requirements in a tree; selecting a node
shows read-only details. The root-level BComp wrapper was removed so the user
can configure the requirements directly.

The details view was progressively refined to show configured modules, child
requirements, selector prefixes/codes/levels, and minimum/maximum conditions.
Equal bounds are displayed as “Exactly x … taken”, and multi-item values are
shown as lists.

### Requirement actions

The requirements GUI gained modal forms for adding root requirements and child
requirements, editing existing requirements, and deleting with confirmation.
Requirement IDs are generated internally and hidden from users. Blank minimum
module/unit counts default to zero, and module-code/prefix validation is applied
in both the dialog and domain model. Editing preserves selection and persists
successful changes.

Action buttons were moved and replaced with local JAR-safe SVG assets for plus,
pencil, trash, and completion indicators. Icon sizes and button sizes are
centralised in `IconFactory`; the button size scales with the requested icon
size. Tooltips and accessible text were retained.

### Modules panel

The modules panel was placed beside the requirements panel and displays code,
name, units, completion checkbox, edit control, and delete control. It gained
an add-module dialog, pencil-based editing, confirmation-based deletion, and
checkbox completion toggling, all delegated to `ModulesManager` and persisted
after successful changes.

The window was sized to 1440×810 with the same minimum size. Long names were
initially ellipsised, then changed to wrapping. The module list uses a
width-fitting vertical scroll pane with horizontal scrolling disabled, keeping
the action controls visible. A module search bar performs case-insensitive
substring matching over code and name.

### Progress display and GUI testing

The GUI gained green requirement progress bars, `Progress: x / y` labels,
overall degree progress, and completion checkmarks that refresh after module
mutations. A later bug investigation found that the checkmark was clipped by a
tree-cell width binding; removing that binding fixed the right-aligned icon.

Manual GUI testing confirmed application launch, default data loading,
requirement selection, composite details, searching, completion toggling,
live progress updates, persistence after reopening, edit-dialog prefilling,
delete confirmation, validation, and File-menu Save/Exit. Automated GUI typing
inside modal text fields could not be completed because of a Computer Use
input limitation; this was recorded as a testing limitation rather than an
observed application defect.

## Packaging, robustness, and documentation

### Cross-platform packaging

The build was aligned with Java SE 25 and JavaFX 25. JavaFX native libraries
are platform-specific, so the initial packaging work produced platform-specific
JARs. After clarifying that one GitHub release artifact was preferred, the
build was changed to create a universal JAR containing isolated Windows, Linux,
and macOS JavaFX runtimes. `UniversalLauncher` detects the platform and
architecture, extracts the matching runtime to a temporary directory, and
starts the application through an isolated classloader. Historical packaging
verification passed on Windows; Linux and macOS payloads were inspected but
require native machines or CI for full GUI launch verification.

### Corrupted-data fallback

A malformed adjacent JSON file previously aborted JavaFX startup. The fix added
`StorageLoadResult` and changed startup loading to fall back to bundled module
and requirement defaults while reporting corruption. `MainWindow` shows a
warning with the affected path, and the malformed user file is not silently
overwritten. Tests and JAR smoke tests covered malformed JSON, renamed root
fields, and unsupported schema versions.

### Documentation and coding standards

The user guide was rewritten to describe the application, setup, icon-based
actions, requirement types, modules, progress evaluation, storage, and current
limitations. The developer guide was expanded with the high-level architecture,
PlantUML source/SVG, responsibilities, persistence, testing, packaging,
trade-offs, and acknowledgements. The architecture diagram was simplified to
high-level components and clarified to show `MainWindow` coordinating the
managers and `StorageManager`.

Test names were renamed and tests reorganised by production method, from basic
cases to complex and boundary cases. A project-specific SE-EDU Java coding
standard skill was added and mandated in `AGENTS.md`; production and test code
were updated accordingly.
