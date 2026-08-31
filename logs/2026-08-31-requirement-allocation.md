# Requirement allocation

The requirement evaluator now treats module allocation as an internal concern
instead of adding user-configurable properties to the requirement model. The
existing `unrestricted-electives` and `degree-total` identifiers define the
special root behaviours. Specific roots select credited completed modules;
unselected completed modules are evaluated as unrestricted electives, while
degree total evaluates all completed modules without consuming them.

The allocation engine evaluates a specific root against a selected module
subset. Module codes from `ModuleRequirement` leaves are explicit claims and
are removed from broad count/unit scopes. Non-explicit modules are claimed by
at most one specific root; the same module can overlap only across explicit
`ModuleRequirement` claims. The engine prefers a fulfilled allocation and then
minimizes credited units, which allows extra matching modules to remain
available for unrestricted electives. Small candidate pools use deterministic
exact search; larger pools use deterministic greedy selection followed by
redundant-module removal. The resulting runtime snapshot also derives each
leaf's scoped matching modules and each composite's union of descendant
allocations for the presentation layer.
