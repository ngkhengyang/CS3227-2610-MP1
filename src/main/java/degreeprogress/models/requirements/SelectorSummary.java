package degreeprogress.models.requirements;

/** The number of completed modules and units matched by a module selector. */
public record SelectorSummary(int matchedModules, int matchedUnits) {
    /** Creates a selector summary after validating its non-negative values. */
    public SelectorSummary {
        if (matchedModules < 0 || matchedUnits < 0) {
            throw new IllegalArgumentException("Selector summary values must not be negative");
        }
    }
}
