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

## Scope of the sample

The sample uses one valid course as a representative choice for each GE
pillar. Users can edit those `moduleCodes` to another approved course. It
models the Artificial Intelligence and Software Engineering focus areas using
their published Area Primary lists.

Two curriculum rules are documented but not fully enforceable by the current
model:

1. A module can currently contribute to multiple requirements. Exclusive,
   non-overlapping allocation between foundation, breadth/depth, and
   unrestricted-elective pools is not implemented yet.
2. The CP4101 dissertation replacement depends on GPA and completing at least
   112 units. The current model has no GPA or conditional predicate, so that
   alternative is intentionally not treated as an unconditional requirement.

The source pages used for the sample are the [BComp CS curriculum](https://www.comp.nus.edu.sg/programmes/ug/cs/curr/),
[Common Curriculum](https://www.comp.nus.edu.sg/cug/soc-22-23/),
[CS focus areas](https://www.comp.nus.edu.sg/programmes/ug/focus/), and the
[approved GE pillar courses](https://www.nus.edu.sg/registrar/academic-information-policies/undergraduate-students/general-education/list-of-courses-approved-under-the-ge-pillars).
