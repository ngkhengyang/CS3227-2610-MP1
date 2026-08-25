package degreeprogress.modules;

import java.util.Locale;
import java.util.Objects;

/**
 * A module recorded by the student.
 *
 * <p>Requirement evaluation only considers completed modules. The level is
 * stored explicitly so that selectors do not need to infer it from a module
 * code, while the convenience constructor derives the usual level from the
 * first digit in the code.</p>
 */
public final class Module {
    private final String code;
    private final int units;
    private final int level;
    private boolean completed;

    public Module(String code, int units, boolean completed) {
        this(code, units, inferLevel(code), completed);
    }

    public Module(String code, int units, int level, boolean completed) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Module code must not be blank");
        }
        if (units <= 0) {
            throw new IllegalArgumentException("Module units must be positive");
        }
        if (level < 0) {
            throw new IllegalArgumentException("Module level must not be negative");
        }
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.units = units;
        this.level = level;
        this.completed = completed;
    }

    public String getCode() {
        return code;
    }

    public int getUnits() {
        return units;
    }

    public int getLevel() {
        return level;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private static int inferLevel(String code) {
        Objects.requireNonNull(code, "code");
        for (int index = 0; index < code.length(); index++) {
            char character = code.charAt(index);
            if (Character.isDigit(character)) {
                return (character - '0') * 1000;
            }
        }
        throw new IllegalArgumentException("Module code must contain a level digit");
    }
}
