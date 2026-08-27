package degreeprogress.models.requirements;

import degreeprogress.models.modules.Module;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Describes which completed modules contribute to a requirement.
 * Empty criteria mean that every module matches.
 */
public final class ModuleSelector {
    private final Set<String> moduleCodes;
    private final Set<String> codePrefixes;
    private final Integer minimumLevel;
    private final Integer maximumLevel;

    public ModuleSelector(
            Set<String> moduleCodes,
            Set<String> codePrefixes,
            Integer minimumLevel,
            Integer maximumLevel) {
        this.moduleCodes = normalise(moduleCodes);
        this.codePrefixes = normalise(codePrefixes);
        if (minimumLevel != null && minimumLevel < 0) {
            throw new IllegalArgumentException("Minimum level must not be negative");
        }
        if (maximumLevel != null && maximumLevel < 0) {
            throw new IllegalArgumentException("Maximum level must not be negative");
        }
        if (minimumLevel != null && maximumLevel != null && minimumLevel > maximumLevel) {
            throw new IllegalArgumentException("Minimum level must not exceed maximum level");
        }
        this.minimumLevel = minimumLevel;
        this.maximumLevel = maximumLevel;
    }

    public static ModuleSelector allModules() {
        return new ModuleSelector(Set.of(), Set.of(), null, null);
    }

    public static ModuleSelector forCodes(String... moduleCodes) {
        return new ModuleSelector(Set.of(moduleCodes), Set.of(), null, null);
    }

    public boolean matches(Module module) {
        String code = module.getCode().toUpperCase(Locale.ROOT);
        boolean matchesCode = moduleCodes.isEmpty() || moduleCodes.contains(code);
        boolean matchesPrefix = codePrefixes.isEmpty()
                || codePrefixes.stream().anyMatch(code::startsWith);
        boolean matchesMinimumLevel = minimumLevel == null || module.getLevel() >= minimumLevel;
        boolean matchesMaximumLevel = maximumLevel == null || module.getLevel() <= maximumLevel;
        return matchesCode && matchesPrefix && matchesMinimumLevel && matchesMaximumLevel;
    }

    public Set<String> getModuleCodes() {
        return moduleCodes;
    }

    public Set<String> getCodePrefixes() {
        return codePrefixes;
    }

    public Integer getMinimumLevel() {
        return minimumLevel;
    }

    public Integer getMaximumLevel() {
        return maximumLevel;
    }

    private static Set<String> normalise(Set<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values == null ? Set.<String>of() : values) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Selector values must not be blank");
            }
            result.add(value.trim().toUpperCase(Locale.ROOT));
        }
        return Set.copyOf(result);
    }
}
