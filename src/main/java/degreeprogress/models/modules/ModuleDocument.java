package degreeprogress.models.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** The complete module data set represented by a future JSON document. */
public record ModuleDocument(int schemaVersion, List<Module> modules) {
    /** Validates the document and defensively copies its module list. */
    public ModuleDocument {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("Schema version must be positive");
        }

        List<Module> suppliedModules = modules == null ? List.of() : modules;
        if (suppliedModules.stream().anyMatch(module -> module == null)) {
            throw new IllegalArgumentException("Modules must not contain null values");
        }

        Set<ModuleCode> codes = new HashSet<>();
        for (Module module : suppliedModules) {
            if (!codes.add(module.getModuleCode())) {
                throw new IllegalArgumentException(
                        "Module codes must be unique: " + module.getCode());
            }
        }
        modules = List.copyOf(suppliedModules);
    }

    /** Finds a module by its normalized code. */
    public Optional<Module> findByCode(String code) {
        ModuleCode requestedCode = new ModuleCode(code);
        return modules.stream()
                .filter(module -> module.getModuleCode().equals(requestedCode))
                .findFirst();
    }

    /** Returns whether a module with the supplied code exists. */
    public boolean containsCode(String code) {
        return findByCode(code).isPresent();
    }
}
