package degreeprogress.models.requirements;

import degreeprogress.models.modules.Module;

import java.util.Collection;
import java.util.List;

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

    public abstract EvaluationResult evaluate(Collection<Module> modules);

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
