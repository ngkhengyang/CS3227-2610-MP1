package degreeprogress.models.requirements;

import java.util.List;

/** The overall progress of the degree's root requirements. */
public record DegreeProgress(
        boolean fulfilled,
        int achievedRequirements,
        int totalRequirements,
        List<EvaluationResult> requirementResults) {

    /** Creates an immutable degree-progress result. */
    public DegreeProgress {
        if (achievedRequirements < 0
                || totalRequirements < 0
                || achievedRequirements > totalRequirements) {
            throw new IllegalArgumentException("Invalid degree progress counts");
        }
        if (requirementResults == null
                || requirementResults.size() != totalRequirements
                || requirementResults.stream().anyMatch(result -> result == null)) {
            throw new IllegalArgumentException("Requirement results must match the total count");
        }
        requirementResults = List.copyOf(requirementResults);
    }
}
