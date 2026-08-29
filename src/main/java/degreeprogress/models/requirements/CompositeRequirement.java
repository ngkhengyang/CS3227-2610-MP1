package degreeprogress.models.requirements;

import java.util.ArrayList;
import java.util.List;

/** Shared implementation for requirements that contain child requirements. */
public abstract class CompositeRequirement extends Requirement {
    private final List<Requirement> children;

    protected CompositeRequirement(
            String id, String name, String description, List<Requirement> children) {
        super(id, name, description);
        this.children = new ArrayList<>(children == null ? List.of() : children);
        if (this.children.stream().anyMatch(child -> child == null)) {
            throw new IllegalArgumentException("Children must not contain null requirements");
        }
    }

    @Override
    protected final EvaluationResult evaluateWithContext(EvaluationContext context) {
        List<EvaluationResult> results = children.stream()
                .map(child -> child.evaluate(context))
                .toList();
        return EvaluationResult.composite(
                getId(), isFulfilled(results), getProgressTarget(results.size()), results);
    }

    protected abstract boolean isFulfilled(List<EvaluationResult> childResults);

    /** Returns the number of fulfilled children needed for progress display. */
    protected int getProgressTarget(int childCount) {
        return childCount;
    }

    @Override
    public List<Requirement> getChildren() {
        return List.copyOf(children);
    }

    /** Adds a child requirement. */
    public void addChild(Requirement child) {
        if (child == null) {
            throw new IllegalArgumentException("Child requirement must not be null");
        }
        children.add(child);
    }

    /** Replaces a child requirement while preserving its position. */
    public void replaceChild(String childId, Requirement replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("Replacement requirement must not be null");
        }
        for (int index = 0; index < children.size(); index++) {
            if (children.get(index).getId().equals(childId)) {
                children.set(index, replacement);
                return;
            }
        }
        throw new IllegalArgumentException("No child requirement exists with this id: " + childId);
    }

    /** Removes child requirements matching the supplied id. */
    public void removeChild(String childId) {
        children.removeIf(child -> child.getId().equals(childId));
    }
}
