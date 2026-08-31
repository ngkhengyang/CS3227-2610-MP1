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
    private final Map<ScopedSelector, SelectorSummary> selectorSummaries;
    private final Map<String, List<Module>> completedModulesByRequirement;

    /** Builds an evaluation context from the supplied module snapshot. */
    public EvaluationContext(Collection<Module> modules) {
        this(modules, Map.of());
    }

    /**
     * Builds an evaluation context with optional per-requirement module scopes.
     *
     * <p>When a requirement id has a scope, leaf requirements with that id use
     * the scoped modules for evaluation. Requirements without a scope continue
     * to use all completed modules in the snapshot.</p>
     *
     * @param modules module snapshot to index
     * @param moduleScopes completed modules visible to specific requirement ids
     */
    public EvaluationContext(
            Collection<Module> modules, Map<String, Collection<Module>> moduleScopes) {
        if (modules == null) {
            throw new IllegalArgumentException("Modules must not be null");
        }
        if (moduleScopes == null || moduleScopes.keySet().stream()
                .anyMatch(requirementId -> requirementId == null || requirementId.isBlank())) {
            throw new IllegalArgumentException("Module scope requirement ids must not be blank");
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
        this.completedModulesByRequirement = createModuleScopes(moduleScopes, completedByCode);
    }

    /** Counts completed modules whose codes occur in the supplied set. */
    public int countCompletedModules(Set<String> moduleCodes) {
        return countCompletedModules(null, moduleCodes);
    }

    /** Counts completed modules in a requirement scope whose codes occur in a supplied set. */
    public int countCompletedModules(String requirementId, Set<String> moduleCodes) {
        if (moduleCodes == null) {
            throw new IllegalArgumentException("Module codes must not be null");
        }

        int achieved = 0;
        for (String code : moduleCodes) {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Module codes must not be blank");
            }
            String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
            if (getCompletedModules(requirementId).stream()
                    .anyMatch(module -> module.getCode().equals(normalizedCode))) {
                achieved++;
            }
        }
        return achieved;
    }

    /** Returns the completed modules indexed by this context. */
    public List<Module> getCompletedModules() {
        return completedModules;
    }

    /** Returns the cached count and unit sum for a selector. */
    public SelectorSummary summarize(ModuleSelector selector) {
        return summarize(null, selector);
    }

    /** Returns a cached count and unit sum for a selector in a requirement scope. */
    public SelectorSummary summarize(String requirementId, ModuleSelector selector) {
        if (selector == null) {
            throw new IllegalArgumentException("Module selector must not be null");
        }
        return selectorSummaries.computeIfAbsent(
                new ScopedSelector(requirementId, selector), this::calculateSummary);
    }

    private SelectorSummary calculateSummary(ScopedSelector scopedSelector) {
        int matchedModules = 0;
        int matchedUnits = 0;
        for (Module module : getCompletedModules(scopedSelector.requirementId())) {
            if (scopedSelector.selector().matches(module)) {
                matchedModules++;
                matchedUnits += module.getUnits();
            }
        }
        return new SelectorSummary(matchedModules, matchedUnits);
    }

    private List<Module> getCompletedModules(String requirementId) {
        if (requirementId == null) {
            return completedModules;
        }
        return completedModulesByRequirement.getOrDefault(requirementId, completedModules);
    }

    private Map<String, List<Module>> createModuleScopes(
            Map<String, Collection<Module>> moduleScopes,
            Map<String, Module> completedByCode) {
        Map<String, List<Module>> normalizedScopes = new HashMap<>();
        for (Map.Entry<String, Collection<Module>> entry : moduleScopes.entrySet()) {
            Collection<Module> scopedModules = entry.getValue();
            if (scopedModules == null || scopedModules.stream().anyMatch(module -> module == null)) {
                throw new IllegalArgumentException("Module scopes must not contain null values");
            }

            Map<String, Module> uniqueModules = new HashMap<>();
            for (Module module : scopedModules) {
                String code = module.getCode().toUpperCase(Locale.ROOT);
                if (!completedByCode.containsKey(code)) {
                    throw new IllegalArgumentException(
                            "Module scopes must contain only completed modules: " + code);
                }
                if (uniqueModules.putIfAbsent(code, module) != null) {
                    throw new IllegalArgumentException("Module scopes must contain unique modules");
                }
            }
            normalizedScopes.put(entry.getKey(), List.copyOf(uniqueModules.values()));
        }
        return Map.copyOf(normalizedScopes);
    }

    private record ScopedSelector(String requirementId, ModuleSelector selector) {
    }
}
