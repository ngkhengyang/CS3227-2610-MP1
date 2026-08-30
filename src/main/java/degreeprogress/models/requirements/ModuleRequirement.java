package degreeprogress.models.requirements;

import java.util.HashSet;
import java.util.Set;

import degreeprogress.models.modules.ModuleCode;

/** A requirement for a fixed set of modules, all of which must be completed. */
public final class ModuleRequirement extends Requirement {
    private Set<String> moduleCodes;

    /** Creates a requirement for a fixed set of module codes. */
    public ModuleRequirement(
            String id, String name, String description, Set<String> moduleCodes) {
        super(id, name, description);
        setModuleCodes(moduleCodes);
    }

    @Override
    protected EvaluationResult evaluateWithContext(EvaluationContext context) {
        int achieved = context.countCompletedModules(moduleCodes);
        return EvaluationResult.leaf(getId(), achieved == moduleCodes.size(), achieved, moduleCodes.size());
    }

    public Set<String> getModuleCodes() {
        return Set.copyOf(moduleCodes);
    }

    public void setModuleCodes(Set<String> moduleCodes) {
        if (moduleCodes == null || moduleCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one module code is required");
        }
        Set<String> normalized = new HashSet<>();
        for (String code : moduleCodes) {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Module codes must not be blank");
            }
            normalized.add(new ModuleCode(code).value());
        }
        this.moduleCodes = Set.copyOf(normalized);
    }
}
