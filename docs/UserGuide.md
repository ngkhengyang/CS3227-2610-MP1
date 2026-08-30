# User Guide

## Current version

The current base version only opens the application window. User-facing features will be documented here as they are implemented.

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

Select any requirement and use **Edit** in the requirement details panel to
change its name, description, or type-specific values. Module-count and
unit-count requirements allow their matching module codes, prefixes, and level
bounds to be edited. Leaf requirement types cannot be changed. Composite
requirements may be changed between **All child requirements** and **Any child
requirement**; their existing children are preserved.
