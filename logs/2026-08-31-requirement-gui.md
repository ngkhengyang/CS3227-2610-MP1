# AI-assisted development note

Implemented the requirements GUI creation flow. Added a shared JavaFX dialog
for specific-module, module-count, unit-count, all-of, and any-of requirements;
requirement IDs are generated as UUIDs. Added root creation from the
Requirements panel, composite-child creation from the details panel, tree
refresh, and persistence callbacks. Verified with `gradlew.bat test`.

Follow-up validation changes made minimum module/unit counts optional in the
GUI, defaulting blank values to zero, and validated module codes and prefixes
in both the dialog and domain models. Existing NUS suffix forms such as
`CS2040S` remain supported.

Implemented requirement editing using the existing requirement dialog and form.
Edit forms preserve requirement IDs, pre-populate module codes, selector codes,
prefixes, level bounds, and count bounds, and restrict type choices according to
`RequirementsManager.editRequirement`. Composite children remain preserved when
switching between all-of and any-of requirements. The requirements tree now
preserves the selected requirement across refreshes.

Implemented requirement deletion from the details panel. The trash button now
confirms the action, delegates deletion by ID to `RequirementsManager`, clears
the details view, refreshes the tree, and persists the updated requirements.
Composite and nested-child deletion behavior is covered by manager tests.
