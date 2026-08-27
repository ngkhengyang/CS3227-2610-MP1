package degreeprogress.models.requirements;

import degreeprogress.models.modules.Module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** A requirement for a fixed set of modules, all of which must be completed. */
public final class ModuleRequirement extends Requirement {
    private Set<String> moduleCodes;

    public ModuleRequirement(
            String id, String name, String description, Set<String> moduleCodes) {
        super(id, name, description);
        setModuleCodes(moduleCodes);
    }

    @Override
    public EvaluationResult evaluate(Collection<Module> modules) {
        Set<String> completedCodes = modules.stream()
                .filter(Module::isCompleted)
                .map(Module::getCode)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        int achieved = (int) moduleCodes.stream().filter(completedCodes::contains).count();
        return EvaluationResult.leaf(getId(), achieved == moduleCodes.size(), achieved, moduleCodes.size());
    }

    public Set<String> getModuleCodes() {
        return Set.copyOf(moduleCodes);
    }

    public void setModuleCodes(Set<String> moduleCodes) {
        if (moduleCodes == null || moduleCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one module code is required");
        }
        Set<String> normalised = new HashSet<>();
        for (String code : moduleCodes) {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Module codes must not be blank");
            }
            normalised.add(code.trim().toUpperCase(Locale.ROOT));
        }
        this.moduleCodes = Set.copyOf(normalised);
    }
}
