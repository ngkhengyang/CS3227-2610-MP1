package degreeprogress.models.requirements;

import java.util.List;

/** The calculated progress of one requirement and its descendants. */
public record EvaluationResult(
        String requirementId,
        boolean fulfilled,
        int achieved,
        int target,
        List<EvaluationResult> children) {

    /** Creates a result with an immutable child list. */
    public EvaluationResult {
        children = List.copyOf(children);
    }

    /** Creates a result for a leaf requirement. */
    public static EvaluationResult leaf(
            String requirementId, boolean fulfilled, int achieved, int target) {
        return new EvaluationResult(requirementId, fulfilled, achieved, target, List.of());
    }

    /** Creates a result for a composite requirement. */
    public static EvaluationResult composite(
            String requirementId, boolean fulfilled, List<EvaluationResult> children) {
        return composite(requirementId, fulfilled, children.size(), children);
    }

    /** Creates a result for a composite requirement with an explicit progress target. */
    public static EvaluationResult composite(
            String requirementId, boolean fulfilled, int target, List<EvaluationResult> children) {
        int achieved = (int) children.stream().filter(EvaluationResult::fulfilled).count();
        return new EvaluationResult(requirementId, fulfilled, achieved, target, children);
    }
}
