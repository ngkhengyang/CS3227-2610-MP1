package degreeprogress.requirements;

import java.util.List;

/** A composite requirement fulfilled only when every child is fulfilled. */
public final class AllOfRequirement extends CompositeRequirement {
    public AllOfRequirement(String id, String name, String description, List<Requirement> children) {
        super(id, name, description, children);
    }

    @Override
    protected boolean isFulfilled(List<EvaluationResult> childResults) {
        return childResults.stream().allMatch(EvaluationResult::fulfilled);
    }
}
