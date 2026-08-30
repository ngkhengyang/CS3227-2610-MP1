package degreeprogress.models.requirements;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;

/**
 * Describes which completed modules contribute to a requirement.
 * Empty criteria mean that every module matches.
 */
public final class ModuleSelector {
    private final Set<String> moduleCodes;
    private final Set<String> codePrefixes;
    private final Integer minimumLevel;
    private final Integer maximumLevel;

    /** Creates a selector from optional code and level criteria. */
    public ModuleSelector(
            Set<String> moduleCodes,
            Set<String> codePrefixes,
            Integer minimumLevel,
            Integer maximumLevel) {
        this.moduleCodes = normalizeModuleCodes(moduleCodes);
        this.codePrefixes = normalizePrefixes(codePrefixes);
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

    /** Returns a selector that matches every module. */
    public static ModuleSelector allModules() {
        return new ModuleSelector(Set.of(), Set.of(), null, null);
    }

    /** Returns a selector that matches the supplied module codes. */
    public static ModuleSelector forCodes(String... moduleCodes) {
        return new ModuleSelector(Set.of(moduleCodes), Set.of(), null, null);
    }

    /** Returns whether the supplied module satisfies this selector. */
    public boolean matches(Module module) {
        String code = module.getCode();
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModuleSelector selector)) {
            return false;
        }
        return moduleCodes.equals(selector.moduleCodes)
                && codePrefixes.equals(selector.codePrefixes)
                && Objects.equals(minimumLevel, selector.minimumLevel)
                && Objects.equals(maximumLevel, selector.maximumLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleCodes, codePrefixes, minimumLevel, maximumLevel);
    }

    private static Set<String> normalizeModuleCodes(Set<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values == null ? Set.<String>of() : values) {
            result.add(new ModuleCode(value).value());
        }
        return Set.copyOf(result);
    }

    private static Set<String> normalizePrefixes(Set<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values == null ? Set.<String>of() : values) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Module prefixes must not be blank");
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            if (normalized.chars().anyMatch(character -> !isAsciiLetter(character))) {
                throw new IllegalArgumentException("Module prefixes must contain letters only");
            }
            result.add(normalized);
        }
        return Set.copyOf(result);
    }

    private static boolean isAsciiLetter(int character) {
        return character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z';
    }
}
