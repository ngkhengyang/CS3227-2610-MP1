package degreeprogress.models.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import degreeprogress.models.modules.Module;

/**
 * Indexes and per-evaluation caches used while evaluating requirements.
 *
 * <p>A context is created once for a module snapshot and reused by every
 * requirement in that evaluation. This avoids repeatedly filtering the same
 * modules for each leaf requirement.</p>
 */
public final class EvaluationContext {
    private final List<Module> completedModules;
    private final Map<String, Module> completedModulesByCode;
    private final Map<ModuleSelector, SelectorSummary> selectorSummaries;

    /** Builds an evaluation context from the supplied module snapshot. */
    public EvaluationContext(Collection<Module> modules) {
        if (modules == null) {
            throw new IllegalArgumentException("Modules must not be null");
        }

        Map<String, Module> modulesByCode = new HashMap<>();
        Map<String, Module> completedByCode = new HashMap<>();
        List<Module> completed = new ArrayList<>();
        for (Module module : modules) {
            if (module == null) {
                throw new IllegalArgumentException("Modules must not contain null values");
            }

            String code = module.getCode().toUpperCase(Locale.ROOT);
            if (modulesByCode.putIfAbsent(code, module) != null) {
                throw new IllegalArgumentException("Module codes must be unique: " + code);
            }
            if (module.isCompleted()) {
                completed.add(module);
                completedByCode.put(code, module);
            }
        }

        this.completedModules = List.copyOf(completed);
        this.completedModulesByCode = Map.copyOf(completedByCode);
        this.selectorSummaries = new HashMap<>();
    }

    /** Counts completed modules whose codes occur in the supplied set. */
    public int countCompletedModules(Set<String> moduleCodes) {
        if (moduleCodes == null) {
            throw new IllegalArgumentException("Module codes must not be null");
        }

        int achieved = 0;
        for (String code : moduleCodes) {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Module codes must not be blank");
            }
            if (completedModulesByCode.containsKey(code.trim().toUpperCase(Locale.ROOT))) {
                achieved++;
            }
        }
        return achieved;
    }

    /** Returns the cached count and unit sum for a selector. */
    public SelectorSummary summarize(ModuleSelector selector) {
        if (selector == null) {
            throw new IllegalArgumentException("Module selector must not be null");
        }
        return selectorSummaries.computeIfAbsent(selector, this::calculateSummary);
    }

    private SelectorSummary calculateSummary(ModuleSelector selector) {
        int matchedModules = 0;
        int matchedUnits = 0;
        for (Module module : completedModules) {
            if (selector.matches(module)) {
                matchedModules++;
                matchedUnits += module.getUnits();
            }
        }
        return new SelectorSummary(matchedModules, matchedUnits);
    }
}
