# Requirement allocation presentation and persistence

The requirements tree now evaluates one allocation snapshot per refresh so
completion indicators remain consistent with the details view. The details
view presents only a leaf requirement's matching modules, the union of child
allocations for a composite requirement, the remainder modules credited to
unrestricted electives, and the non-consuming behaviour of degree total.

No allocation fields were added to the requirement document. The user's
unrestricted-elective minimum continues to use the existing editable
`UnitCountRequirement` fields, and serializer coverage verifies that this
value survives a save-and-load round trip. The full Gradle test suite passed
after the presentation and persistence changes.

The final presentation pass now names the selected requirement in its module
allocation label, matching the leaf-specific and composite-union behaviour
already used for the displayed module list. Allocation snapshots also reject
blank module codes and defensively copy their maps and sets. Regression tests
cover these invariants and verify that unrestricted electives exclude modules
credited by every specific root.

The allocation hardening pass also prevents explicit modules from being reused
by broad count or unit leaves. This required scoped evaluation contexts so the
progress result and the displayed credited-module lists use the same exclusion
rule. Explicit reuse remains available when the same code is listed in each
specific-module requirement that claims it. The complete Gradle test suite
passed after this change.
