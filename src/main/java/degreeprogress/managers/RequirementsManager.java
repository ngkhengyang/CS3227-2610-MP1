package degreeprogress.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import degreeprogress.models.requirements.CompositeRequirement;
import degreeprogress.models.requirements.Requirement;

/** Owns the requirements defined for the student's degree. */
public final class RequirementsManager {
    private final List<Requirement> requirements;

    /** Creates an empty requirements manager. */
    public RequirementsManager() {
        this.requirements = new ArrayList<>();
    }

    /** Creates a manager containing the supplied preset requirements. */
    public RequirementsManager(List<Requirement> presetRequirements) {
        if (presetRequirements == null
                || presetRequirements.stream().anyMatch(requirement -> requirement == null)) {
            throw new IllegalArgumentException("Preset requirements must not be null");
        }

        Set<String> requirementIds = new HashSet<>();
        for (Requirement requirement : presetRequirements) {
            if (!requirementIds.add(requirement.getId())) {
                throw new IllegalArgumentException(
                        "Preset requirement ids must be unique: " + requirement.getId());
            }
        }
        this.requirements = new ArrayList<>(presetRequirements);
    }

    /** Adds a requirement to the manager. */
    public Requirement addRequirement(Requirement requirement) {
        if (requirement == null) {
            throw new IllegalArgumentException("Requirement must not be null");
        }
        if (requirements.stream()
                .anyMatch(existing -> existing.getId().equals(requirement.getId()))) {
            throw new IllegalArgumentException(
                    "A requirement with this id already exists: " + requirement.getId());
        }
        requirements.add(requirement);
        return requirement;
    }

    /** Adds a child requirement under the composite requirement with the supplied id. */
    public Requirement addChildRequirement(String parentId, Requirement child) {
        if (parentId == null || parentId.isBlank()) {
            throw new IllegalArgumentException("Parent requirement id must not be blank");
        }
        if (child == null) {
            throw new IllegalArgumentException("Child requirement must not be null");
        }
        if (containsRequirementId(child.getId())) {
            throw new IllegalArgumentException(
                    "A requirement with this id already exists: " + child.getId());
        }

        Requirement parent = findRequirement(parentId, requirements);
        if (parent == null) {
            throw new IllegalArgumentException(
                    "No requirement exists with this id: " + parentId);
        }
        if (!(parent instanceof CompositeRequirement composite)) {
            throw new IllegalArgumentException(
                    "Requirement is not composite: " + parentId);
        }

        composite.addChild(child);
        return child;
    }

    /** Deletes a requirement identified by id from the root list or its hierarchy. */
    public Requirement deleteRequirement(String requirementId) {
        validateRequirementId(requirementId);

        Requirement deleted = removeRequirementFromList(requirementId, requirements);
        if (deleted == null) {
            throw new IllegalArgumentException(
                    "No requirement exists with this id: " + requirementId);
        }
        return deleted;
    }

    /** Returns an immutable snapshot of the current requirements. */
    public List<Requirement> getRequirements() {
        return List.copyOf(requirements);
    }

    private boolean containsRequirementId(String id) {
        return findRequirement(id, requirements) != null;
    }

    private void validateRequirementId(String requirementId) {
        if (requirementId == null || requirementId.isBlank()) {
            throw new IllegalArgumentException("Requirement id must not be blank");
        }
    }

    private Requirement removeRequirementFromList(String id, List<Requirement> candidates) {
        for (int index = 0; index < candidates.size(); index++) {
            Requirement candidate = candidates.get(index);
            if (candidate.getId().equals(id)) {
                candidates.remove(index);
                return candidate;
            }
            if (candidate instanceof CompositeRequirement composite) {
                Requirement removed = removeRequirementFromParent(id, composite);
                if (removed != null) {
                    return removed;
                }
            }
        }
        return null;
    }

    private Requirement removeRequirementFromParent(String id, CompositeRequirement parent) {
        for (Requirement child : parent.getChildren()) {
            if (child.getId().equals(id)) {
                parent.removeChild(id);
                return child;
            }
            if (child instanceof CompositeRequirement composite) {
                Requirement removed = removeRequirementFromParent(id, composite);
                if (removed != null) {
                    return removed;
                }
            }
        }
        return null;
    }

    private Requirement findRequirement(String id, List<Requirement> candidates) {
        for (Requirement candidate : candidates) {
            if (candidate.getId().equals(id)) {
                return candidate;
            }
            Requirement descendant = findRequirement(id, candidate.getChildren());
            if (descendant != null) {
                return descendant;
            }
        }
        return null;
    }
}
