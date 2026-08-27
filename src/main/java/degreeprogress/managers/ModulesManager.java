package degreeprogress.managers;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;

import java.util.ArrayList;
import java.util.List;

/** Owns the modules recorded by the student. */
public final class ModulesManager {
    private final List<Module> modules;

    /** Creates an empty module manager. */
    public ModulesManager() {
        this.modules = new ArrayList<>();
    }

    /** Adds a new incomplete module. */
    public Module addModule(String code, String name, int units) {
        ModuleCode moduleCode = new ModuleCode(code);
        if (findModule(moduleCode).isPresent()) {
            throw new IllegalArgumentException("A module with this code already exists: " + moduleCode.value());
        }

        Module module = new Module(moduleCode, name, units, false);
        modules.add(module);
        return module;
    }

    /** Deletes the module identified by its code. */
    public Module deleteModule(String code) {
        ModuleCode moduleCode = new ModuleCode(code);
        for (int index = 0; index < modules.size(); index++) {
            Module module = modules.get(index);
            if (module.getModuleCode().equals(moduleCode)) {
                modules.remove(index);
                return module;
            }
        }
        throw new IllegalArgumentException("No module exists with this code: " + moduleCode.value());
    }

    /** Returns an immutable snapshot of the current modules. */
    public List<Module> getModules() {
        return List.copyOf(modules);
    }

    private java.util.Optional<Module> findModule(ModuleCode code) {
        return modules.stream()
                .filter(module -> module.getModuleCode().equals(code))
                .findFirst();
    }
}
