package degreeprogress.models.requirements;

import java.util.List;

/** A composite requirement fulfilled only when every child is fulfilled. */
public final class AllOfRequirement extends CompositeRequirement {
    /** Creates a requirement that requires every child to be fulfilled. */
    public AllOfRequirement(String id, String name, String description, List<Requirement> children) {
        super(id, name, description, children);
    }

    @Override
    protected boolean isFulfilled(List<EvaluationResult> childResults) {
        return childResults.stream().allMatch(EvaluationResult::fulfilled);
    }
}
