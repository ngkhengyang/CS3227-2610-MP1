package degreeprogress.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import degreeprogress.models.modules.Module;
import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.CompositeRequirement;
import degreeprogress.models.requirements.DegreeProgress;
import degreeprogress.models.requirements.EvaluationContext;
import degreeprogress.models.requirements.EvaluationResult;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.UnitCountRequirement;

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
        if (containsRequirementId(requirement.getId())) {
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

    /** Edits a requirement while applying the supported requirement type rules. */
    public Requirement editRequirement(String requirementId, Requirement editedRequirement) {
        validateRequirementId(requirementId);
        if (editedRequirement == null) {
            throw new IllegalArgumentException("Edited requirement must not be null");
        }
        if (!requirementId.equals(editedRequirement.getId())) {
            throw new IllegalArgumentException("Requirement id cannot be changed");
        }

        Requirement existingRequirement = findRequirement(requirementId, requirements);
        if (existingRequirement == null) {
            throw new IllegalArgumentException(
                    "No requirement exists with this id: " + requirementId);
        }
        validateEditableTypes(existingRequirement, editedRequirement);

        if (existingRequirement instanceof CompositeRequirement existingComposite) {
            if (existingRequirement.getClass().equals(editedRequirement.getClass())) {
                copyMetadata(existingRequirement, editedRequirement);
                return existingRequirement;
            }
            Requirement replacement = createCompositeReplacement(
                    editedRequirement, existingComposite.getChildren());
            replaceRequirement(requirementId, replacement);
            return replacement;
        }

        copyLeafRequirement(existingRequirement, editedRequirement);
        return existingRequirement;
    }

    /** Returns an immutable snapshot of the current requirements. */
    public List<Requirement> getRequirements() {
        return List.copyOf(requirements);
    }

    /** Evaluates one requirement against the current modules. */
    public EvaluationResult evaluateRequirement(
            String requirementId, ModulesManager modulesManager) {
        if (modulesManager == null) {
            throw new IllegalArgumentException("Modules manager must not be null");
        }
        return evaluateRequirement(requirementId, modulesManager.getModules());
    }

    /** Evaluates one requirement against the supplied module snapshot. */
    public EvaluationResult evaluateRequirement(
            String requirementId, Collection<Module> modules) {
        validateRequirementId(requirementId);
        Requirement requirement = findRequirement(requirementId, requirements);
        if (requirement == null) {
            throw new IllegalArgumentException(
                    "No requirement exists with this id: " + requirementId);
        }
        return requirement.evaluate(new EvaluationContext(modules));
    }

    /** Evaluates all root requirements against the current modules. */
    public List<EvaluationResult> evaluateRequirements(ModulesManager modulesManager) {
        if (modulesManager == null) {
            throw new IllegalArgumentException("Modules manager must not be null");
        }
        return evaluateRequirements(modulesManager.getModules());
    }

    /** Evaluates all root requirements against the supplied module snapshot. */
    public List<EvaluationResult> evaluateRequirements(Collection<Module> modules) {
        EvaluationContext context = new EvaluationContext(modules);
        return evaluateRequirements(context);
    }

    /** Evaluates the complete degree against the current modules. */
    public DegreeProgress evaluateDegree(ModulesManager modulesManager) {
        if (modulesManager == null) {
            throw new IllegalArgumentException("Modules manager must not be null");
        }
        return evaluateDegree(modulesManager.getModules());
    }

    /** Evaluates the complete degree against the supplied module snapshot. */
    public DegreeProgress evaluateDegree(Collection<Module> modules) {
        List<EvaluationResult> results = evaluateRequirements(modules);
        int achieved = (int) results.stream()
                .filter(EvaluationResult::fulfilled)
                .count();
        boolean fulfilled = !results.isEmpty() && achieved == results.size();
        return new DegreeProgress(fulfilled, achieved, results.size(), results);
    }

    private List<EvaluationResult> evaluateRequirements(EvaluationContext context) {
        return requirements.stream()
                .map(requirement -> requirement.evaluate(context))
                .toList();
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

    private void validateEditableTypes(Requirement existing, Requirement edited) {
        boolean existingIsComposite = existing instanceof CompositeRequirement;
        boolean editedIsComposite = edited instanceof CompositeRequirement;
        if (existingIsComposite != editedIsComposite) {
            throw new IllegalArgumentException(
                    "Leaf and composite requirement types cannot be converted");
        }
        if (!existingIsComposite && !existing.getClass().equals(edited.getClass())) {
            throw new IllegalArgumentException(
                    "Leaf requirement types cannot be converted");
        }
        if (existingIsComposite
                && !(isAllOfAndAnyOf(existing, edited))) {
            throw new IllegalArgumentException(
                    "Composite requirement types cannot be converted except between AllOf and AnyOf");
        }
    }

    private boolean isAllOfAndAnyOf(Requirement first, Requirement second) {
        return (first instanceof AllOfRequirement && second instanceof AnyOfRequirement)
                || (first instanceof AnyOfRequirement && second instanceof AllOfRequirement)
                || first.getClass().equals(second.getClass());
    }

    private void copyMetadata(Requirement existing, Requirement edited) {
        existing.setName(edited.getName());
        existing.setDescription(edited.getDescription());
    }

    private void copyLeafRequirement(Requirement existing, Requirement edited) {
        copyMetadata(existing, edited);
        if (existing instanceof ModuleRequirement existingModule
                && edited instanceof ModuleRequirement editedModule) {
            existingModule.setModuleCodes(editedModule.getModuleCodes());
        } else if (existing instanceof ModuleCountRequirement existingModuleCount
                && edited instanceof ModuleCountRequirement editedModuleCount) {
            existingModuleCount.setSelector(editedModuleCount.getSelector());
            existingModuleCount.setBounds(
                    editedModuleCount.getMinimumModules(), editedModuleCount.getMaximumModules());
        } else if (existing instanceof UnitCountRequirement existingUnitCount
                && edited instanceof UnitCountRequirement editedUnitCount) {
            existingUnitCount.setSelector(editedUnitCount.getSelector());
            existingUnitCount.setBounds(
                    editedUnitCount.getMinimumUnits(), editedUnitCount.getMaximumUnits());
        }
    }

    private Requirement createCompositeReplacement(
            Requirement edited, List<Requirement> children) {
        if (edited instanceof AllOfRequirement) {
            return new AllOfRequirement(
                    edited.getId(), edited.getName(), edited.getDescription(), children);
        }
        return new AnyOfRequirement(
                edited.getId(), edited.getName(), edited.getDescription(), children);
    }

    private void replaceRequirement(String id, Requirement replacement) {
        for (int index = 0; index < requirements.size(); index++) {
            Requirement requirement = requirements.get(index);
            if (requirement.getId().equals(id)) {
                requirements.set(index, replacement);
                return;
            }
            if (requirement instanceof CompositeRequirement composite
                    && replaceRequirementFromParent(id, composite, replacement)) {
                return;
            }
        }
        throw new IllegalArgumentException("No requirement exists with this id: " + id);
    }

    private boolean replaceRequirementFromParent(
            String id, CompositeRequirement parent, Requirement replacement) {
        for (Requirement child : parent.getChildren()) {
            if (child.getId().equals(id)) {
                parent.replaceChild(id, replacement);
                return true;
            }
            if (child instanceof CompositeRequirement composite
                    && replaceRequirementFromParent(id, composite, replacement)) {
                return true;
            }
        }
        return false;
    }
}
