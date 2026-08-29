package degreeprogress.models.requirements;

/** A requirement for at least, and optionally at most, N matching modules. */
public final class ModuleCountRequirement extends Requirement {
    private ModuleSelector selector;
    private int minimumModules;
    private Integer maximumModules;

    /** Creates a module-count requirement without a maximum. */
    public ModuleCountRequirement(
            String id, String name, String description,
            ModuleSelector selector, int minimumModules) {
        this(id, name, description, selector, minimumModules, null);
    }

    /** Creates a module-count requirement with optional lower and upper bounds. */
    public ModuleCountRequirement(
            String id, String name, String description,
            ModuleSelector selector, int minimumModules, Integer maximumModules) {
        super(id, name, description);
        this.selector = selector == null ? ModuleSelector.allModules() : selector;
        setBounds(minimumModules, maximumModules);
    }

    @Override
    protected EvaluationResult evaluateWithContext(EvaluationContext context) {
        int achieved = context.summarize(selector).matchedModules();
        boolean meetsMinimum = achieved >= minimumModules;
        boolean meetsMaximum = maximumModules == null || achieved <= maximumModules;
        return EvaluationResult.leaf(getId(), meetsMinimum && meetsMaximum, achieved, minimumModules);
    }

    public ModuleSelector getSelector() {
        return selector;
    }

    public void setSelector(ModuleSelector selector) {
        this.selector = selector == null ? ModuleSelector.allModules() : selector;
    }

    public int getMinimumModules() {
        return minimumModules;
    }

    public Integer getMaximumModules() {
        return maximumModules;
    }

    /** Sets the minimum and optional maximum number of matching modules. */
    public void setBounds(int minimumModules, Integer maximumModules) {
        if (minimumModules < 0) {
            throw new IllegalArgumentException("Minimum modules must not be negative");
        }
        if (maximumModules != null && maximumModules < minimumModules) {
            throw new IllegalArgumentException("Maximum modules must not be below minimum modules");
        }
        this.minimumModules = minimumModules;
        this.maximumModules = maximumModules;
    }
}
