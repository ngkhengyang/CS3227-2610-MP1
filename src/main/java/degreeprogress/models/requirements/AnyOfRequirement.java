package degreeprogress.models.requirements;

import java.util.List;

/** A composite requirement fulfilled when at least one child is fulfilled. */
public final class AnyOfRequirement extends CompositeRequirement {
    /** Creates a requirement that requires at least one child to be fulfilled. */
    public AnyOfRequirement(String id, String name, String description, List<Requirement> children) {
        super(id, name, description, children);
    }

    @Override
    protected boolean isFulfilled(List<EvaluationResult> childResults) {
        return childResults.stream().anyMatch(EvaluationResult::fulfilled);
    }

    @Override
    protected int getProgressTarget(int childCount) {
        return 1;
    }
}
