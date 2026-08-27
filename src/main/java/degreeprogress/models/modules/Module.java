package degreeprogress.models.modules;

/**
 * A module recorded by the student.
 *
 * <p>Requirement evaluation only considers completed modules. The module
 * level and prefix are derived from the module code and are not stored as
 * independent values.</p>
 */
public final class Module {
    private static final int MIN_UNITS = 1;
    private static final int MAX_UNITS = 60;

    private final ModuleCode code;
    private final String name;
    private final int units;
    private boolean completed;

    /** Creates an incomplete module with the supplied basic details. */
    public Module(String code, String name, int units) {
        this(new ModuleCode(code), name, units, false);
    }

    public Module(String code, int units, boolean completed) {
        this(new ModuleCode(code), code, units, completed);
    }

    /** Creates a module from an already-normalised module code value. */
    public Module(ModuleCode code, int units, boolean completed) {
        this(code, code == null ? null : code.value(), units, completed);
    }

    /** Creates a module from an already-normalised module code value. */
    public Module(ModuleCode code, String name, int units, boolean completed) {
        if (code == null) {
            throw new IllegalArgumentException("Module code must be provided");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Module name must be provided");
        }
        if (units < MIN_UNITS || units > MAX_UNITS) {
            throw new IllegalArgumentException("Module units must be between "
                    + MIN_UNITS + " and " + MAX_UNITS);
        }
        this.code = code;
        this.name = name.trim();
        this.units = units;
        this.completed = completed;
    }

    /**
     * Compatibility constructor for callers that previously supplied a
     * level. The supplied value must agree with the value derived from code;
     * it is not stored.
     */
    public Module(String code, int units, int level, boolean completed) {
        this(code, units, completed);
        if (level != getLevel()) {
            throw new IllegalArgumentException("Module level must match the module code");
        }
    }

    public String getCode() {
        return code.value();
    }

    public String getName() {
        return name;
    }

    public ModuleCode getModuleCode() {
        return code;
    }

    public int getUnits() {
        return units;
    }

    public int getLevel() {
        return code.getLevel();
    }

    public String getPrefix() {
        return code.getPrefix();
    }

    public boolean hasPrefix(String prefix) {
        return code.startsWith(prefix);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Module module)) {
            return false;
        }
        return code.equals(module.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
