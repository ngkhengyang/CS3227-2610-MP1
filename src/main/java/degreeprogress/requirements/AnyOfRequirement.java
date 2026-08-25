package degreeprogress.requirements;

import java.util.List;

/** A composite requirement fulfilled when at least one child is fulfilled. */
public final class AnyOfRequirement extends CompositeRequirement {
    public AnyOfRequirement(String id, String name, String description, List<Requirement> children) {
        super(id, name, description, children);
    }

    @Override
    protected boolean isFulfilled(List<EvaluationResult> childResults) {
        return childResults.stream().anyMatch(EvaluationResult::fulfilled);
    }
}
