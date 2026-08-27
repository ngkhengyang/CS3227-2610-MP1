package degreeprogress.managers;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Owns the modules recorded by the student. */
public final class ModulesManager {
    private final List<Module> modules;

    /** Creates an empty module manager. */
    public ModulesManager() {
        this.modules = new ArrayList<>();
    }

    /** Creates a manager containing the supplied preset modules. */
    public ModulesManager(List<Module> presetModules) {
        if (presetModules == null || presetModules.stream().anyMatch(module -> module == null)) {
            throw new IllegalArgumentException("Preset modules must not be null");
        }

        Set<ModuleCode> moduleCodes = new HashSet<>();
        for (Module module : presetModules) {
            if (!moduleCodes.add(module.getModuleCode())) {
                throw new IllegalArgumentException(
                        "Preset module codes must be unique: " + module.getCode());
            }
        }
        this.modules = new ArrayList<>(presetModules);
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

    /** Edits a module's name and units while keeping its existing code. */
    public Module editModule(String code, String name, int units) {
        return editModule(code, code, name, units);
    }

    /**
     * Edits a module's code, name, and units. The completion state is
     * preserved when the module is replaced.
     */
    public Module editModule(String currentCode, String newCode, String name, int units) {
        ModuleCode currentModuleCode = new ModuleCode(currentCode);
        ModuleCode newModuleCode = new ModuleCode(newCode);
        int moduleIndex = findModuleIndex(currentModuleCode);
        if (moduleIndex < 0) {
            throw new IllegalArgumentException(
                    "No module exists with this code: " + currentModuleCode.value());
        }

        Module existingModule = modules.get(moduleIndex);
        if (!currentModuleCode.equals(newModuleCode) && findModule(newModuleCode).isPresent()) {
            throw new IllegalArgumentException(
                    "A module with this code already exists: " + newModuleCode.value());
        }

        Module editedModule = new Module(
                newModuleCode, name, units, existingModule.isCompleted());
        modules.set(moduleIndex, editedModule);
        return editedModule;
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

    private int findModuleIndex(ModuleCode code) {
        for (int index = 0; index < modules.size(); index++) {
            if (modules.get(index).getModuleCode().equals(code)) {
                return index;
            }
        }
        return -1;
    }
}
