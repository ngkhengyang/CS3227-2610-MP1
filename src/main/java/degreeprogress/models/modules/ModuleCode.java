package degreeprogress.models.modules;

import java.util.Locale;
import java.util.Objects;

/**
 * The normalized identity of a module and the information derived from it.
 *
 * <p>A module code consists of a leading alphabetic prefix, a numeric level,
 * and an optional alphabetic suffix. For example, {@code CS2040S} has prefix
 * {@code CS} and level {@code 2000}.</p>
 */
public record ModuleCode(String value) {
    /** Creates a normalized module code after validating its prefix and level. */
    public ModuleCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Module code must not be blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (!isAsciiLetter(character) && !isAsciiDigit(character)) {
                throw new IllegalArgumentException("Module code must contain only letters and numbers");
            }
        }
        int firstDigit = firstDigitIndex(normalized);
        if (firstDigit == 0) {
            throw new IllegalArgumentException("Module code must start with a prefix");
        }
        if (firstDigit == -1) {
            throw new IllegalArgumentException("Module code must contain a level digit");
        }
        for (int index = 0; index < firstDigit; index++) {
            if (!isAsciiLetter(normalized.charAt(index))) {
                throw new IllegalArgumentException("Module code prefix must contain letters only");
            }
        }
        int suffixStart = firstDigit;
        while (suffixStart < normalized.length() && isAsciiDigit(normalized.charAt(suffixStart))) {
            suffixStart++;
        }
        while (suffixStart < normalized.length() && isAsciiLetter(normalized.charAt(suffixStart))) {
            suffixStart++;
        }
        if (suffixStart != normalized.length()) {
            throw new IllegalArgumentException(
                    "Module code must have letters followed by a number and optional suffix letters");
        }
        value = normalized;
    }

    /** Returns the alphabetic prefix before the first digit. */
    public String getPrefix() {
        return value.substring(0, firstDigitIndex(value));
    }

    /**
     * Returns the level derived from the first digit in the module code.
     * For example, {@code CS2040} maps to {@code 2000}.
     */
    public int getLevel() {
        int firstDigit = firstDigitIndex(value);
        return Character.digit(value.charAt(firstDigit), 10) * 1000;
    }

    /** Returns whether this code has the supplied prefix, ignoring case. */
    public boolean startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        String normalizedPrefix = prefix.trim().toUpperCase(Locale.ROOT);
        if (normalizedPrefix.isEmpty()) {
            throw new IllegalArgumentException("Module prefix must not be blank");
        }
        return value.startsWith(normalizedPrefix);
    }

    private static int firstDigitIndex(String code) {
        for (int index = 0; index < code.length(); index++) {
            if (isAsciiDigit(code.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isAsciiLetter(char character) {
        return character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z';
    }

    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }
}
