package degreeprogress.models.requirements;

/** A requirement for at least, and optionally at most, N units from matching modules. */
public final class UnitCountRequirement extends Requirement {
    private ModuleSelector selector;
    private int minimumUnits;
    private Integer maximumUnits;

    /** Creates a unit-count requirement without a maximum. */
    public UnitCountRequirement(
            String id, String name, String description,
            ModuleSelector selector, int minimumUnits) {
        this(id, name, description, selector, minimumUnits, null);
    }

    /** Creates a unit-count requirement with optional lower and upper bounds. */
    public UnitCountRequirement(
            String id, String name, String description,
            ModuleSelector selector, int minimumUnits, Integer maximumUnits) {
        super(id, name, description);
        this.selector = selector == null ? ModuleSelector.allModules() : selector;
        setBounds(minimumUnits, maximumUnits);
    }

    @Override
    protected EvaluationResult evaluateWithContext(EvaluationContext context) {
        int achieved = context.summarize(selector).matchedUnits();
        boolean meetsMinimum = achieved >= minimumUnits;
        boolean meetsMaximum = maximumUnits == null || achieved <= maximumUnits;
        return EvaluationResult.leaf(getId(), meetsMinimum && meetsMaximum, achieved, minimumUnits);
    }

    public ModuleSelector getSelector() {
        return selector;
    }

    public void setSelector(ModuleSelector selector) {
        this.selector = selector == null ? ModuleSelector.allModules() : selector;
    }

    public int getMinimumUnits() {
        return minimumUnits;
    }

    public Integer getMaximumUnits() {
        return maximumUnits;
    }

    /** Sets the minimum and optional maximum number of matching units. */
    public void setBounds(int minimumUnits, Integer maximumUnits) {
        if (minimumUnits < 0) {
            throw new IllegalArgumentException("Minimum units must not be negative");
        }
        if (maximumUnits != null && maximumUnits < minimumUnits) {
            throw new IllegalArgumentException("Maximum units must not be below minimum units");
        }
        this.minimumUnits = minimumUnits;
        this.maximumUnits = maximumUnits;
    }
}
