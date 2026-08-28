package degreeprogress.models.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import degreeprogress.models.modules.Module;

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
    public final EvaluationResult evaluate(Collection<Module> modules) {
        List<EvaluationResult> results = children.stream()
                .map(child -> child.evaluate(modules))
                .toList();
        return EvaluationResult.composite(getId(), isFulfilled(results), results);
    }

    protected abstract boolean isFulfilled(List<EvaluationResult> childResults);

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

    /** Removes child requirements matching the supplied id. */
    public void removeChild(String childId) {
        children.removeIf(child -> child.getId().equals(childId));
    }
}
