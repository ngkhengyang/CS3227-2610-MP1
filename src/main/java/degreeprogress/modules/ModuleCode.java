package degreeprogress.modules;

import java.util.Locale;
import java.util.Objects;

/**
 * The normalised identity of a module and the information derived from it.
 *
 * <p>A module code consists of a leading alphabetic prefix followed by at
 * least one digit. Any trailing suffix is retained as part of the code. For
 * example, {@code CS2040S} has prefix {@code CS} and level {@code 2000}.</p>
 */
public record ModuleCode(String value) {
    public ModuleCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Module code must not be blank");
        }

        String normalised = value.trim().toUpperCase(Locale.ROOT);
        int firstDigit = firstDigitIndex(normalised);
        if (firstDigit == 0) {
            throw new IllegalArgumentException("Module code must start with a prefix");
        }
        if (firstDigit == -1) {
            throw new IllegalArgumentException("Module code must contain a level digit");
        }
        for (int index = 0; index < firstDigit; index++) {
            if (!Character.isLetter(normalised.charAt(index))) {
                throw new IllegalArgumentException("Module code prefix must contain letters only");
            }
        }
        value = normalised;
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
        String normalisedPrefix = prefix.trim().toUpperCase(Locale.ROOT);
        if (normalisedPrefix.isEmpty()) {
            throw new IllegalArgumentException("Module prefix must not be blank");
        }
        return value.startsWith(normalisedPrefix);
    }

    private static int firstDigitIndex(String code) {
        for (int index = 0; index < code.length(); index++) {
            if (Character.isDigit(code.charAt(index))) {
                return index;
            }
        }
        return -1;
    }
}
