package degreeprogress.models.requirements;

import java.util.Collection;
import java.util.List;

import degreeprogress.models.modules.Module;

/** Base class for every user-defined requirement. */
public abstract class Requirement {
    private final String id;
    private String name;
    private String description;

    protected Requirement(String id, String name, String description) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Requirement id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Requirement name must not be blank");
        }
        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
    }

    /** Evaluates this requirement against the supplied modules. */
    public final EvaluationResult evaluate(Collection<Module> modules) {
        return evaluate(new EvaluationContext(modules));
    }

    /** Evaluates this requirement using a context shared by the requirement tree. */
    public final EvaluationResult evaluate(EvaluationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Evaluation context must not be null");
        }
        return evaluateWithContext(context);
    }

    /** Evaluates this requirement using precomputed module indexes. */
    protected abstract EvaluationResult evaluateWithContext(EvaluationContext context);

    public List<Requirement> getChildren() {
        return List.of();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Requirement name must not be blank");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }
}
