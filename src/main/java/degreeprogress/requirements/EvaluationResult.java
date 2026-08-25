package degreeprogress.requirements;

import java.util.List;

/** The calculated progress of one requirement and its descendants. */
public record EvaluationResult(
        String requirementId,
        boolean fulfilled,
        int achieved,
        int target,
        List<EvaluationResult> children) {

    public EvaluationResult {
        children = List.copyOf(children);
    }

    public static EvaluationResult leaf(
            String requirementId, boolean fulfilled, int achieved, int target) {
        return new EvaluationResult(requirementId, fulfilled, achieved, target, List.of());
    }

    public static EvaluationResult composite(
            String requirementId, boolean fulfilled, List<EvaluationResult> children) {
        int achieved = (int) children.stream().filter(EvaluationResult::fulfilled).count();
        return new EvaluationResult(requirementId, fulfilled, achieved, children.size(), children);
    }
}
