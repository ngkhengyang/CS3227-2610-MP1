# Requirement Data Format

The bundled sample is [default-requirements.json](../src/main/resources/default-requirements.json).
It uses a `type` discriminator to preserve the Java subclass hierarchy when a
requirement is stored as JSON. The discriminator is a serialization detail;
the domain model still uses `Requirement` subclasses rather than a
`RequirementKind` enum.

## JSON types

| JSON `type` | Java class | Main fields |
| --- | --- | --- |
| `module` | `ModuleRequirement` | `moduleCodes` |
| `moduleCount` | `ModuleCountRequirement` | `selector`, `minimumModules`, `maximumModules` |
| `unitCount` | `UnitCountRequirement` | `selector`, `minimumUnits`, `maximumUnits` |
| `allOf` | `AllOfRequirement` | `children` |
| `anyOf` | `AnyOfRequirement` | `children` |

All requirement types share `id`, `name`, and `description`. Composite
requirements contain more requirements in `children`.

## Selectors

Selectors support the fields currently implemented by `ModuleSelector`:

```json
{
  "moduleCodes": ["CS4248", "CS4262"],
  "codePrefixes": ["CS", "IFS", "CP"],
  "minimumLevel": 4000,
  "maximumLevel": 5000
}
```

Module codes remain strings. Empty selectors match every module. A selector
matches a module only when all populated selector fields match.

Module codes are validated as a leading alphabetic prefix followed by one or
more digits and an optional alphabetic suffix. Code prefixes must contain
letters only. Special symbols and malformed letter/number ordering are
rejected.

For module-count and unit-count requirements, an optional maximum limits the
number of modules or units credited to that requirement. Extra completed
matches do not make the requirement fail; non-explicit extra matches remain
available for other specific requirements or unrestricted electives. Explicit
module claims follow the allocation rule below.

## Scope of the sample

The sample uses one valid course as a representative choice for each GE
pillar. Users can edit those `moduleCodes` to another approved course. It
models the Artificial Intelligence and Software Engineering focus areas using
their published Area Primary lists.

The evaluator applies the following allocation rule:

1. Specific root requirements are evaluated against selected completed modules.
   Module codes listed by `module` requirements are explicit claims and are not
   available to module-count or unit-count selectors. A module can be credited
   to multiple requirements only when it is explicitly listed by each of those
   `module` requirements. Other completed modules are allocated to at most one
   specific root. Unselected completed modules are allocated to the root
   requirement with the reserved id `unrestricted-electives`. The `degree-total`
   root evaluates all completed modules without consuming them. Children of one
   root share that root's selected module pool, except that broad count leaves
   use the scoped pool with explicit module claims removed.
2. The CP4101 dissertation replacement depends on GPA and completing at least
   112 units. The current model has no GPA or conditional predicate, so that
   alternative is intentionally not treated as an unconditional requirement.

Module allocation is calculated at runtime and is not stored in the JSON
document. Existing requirement fields, including the unrestricted-elective
`minimumUnits`, remain the only user-configured inputs.

The source pages used for the sample are the [BComp CS curriculum](https://www.comp.nus.edu.sg/programmes/ug/cs/curr/),
[Common Curriculum](https://www.comp.nus.edu.sg/cug/soc-22-23/),
[CS focus areas](https://www.comp.nus.edu.sg/programmes/ug/focus/), and the
[approved GE pillar courses](https://www.nus.edu.sg/registrar/academic-information-policies/undergraduate-students/general-education/list-of-courses-approved-under-the-ge-pillars).
