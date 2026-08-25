package degreeprogress.modules;

/**
 * A module recorded by the student.
 *
 * <p>Requirement evaluation only considers completed modules. The module
 * level and prefix are derived from the module code and are not stored as
 * independent values.</p>
 */
public final class Module {
    private final ModuleCode code;
    private final int units;
    private boolean completed;

    public Module(String code, int units, boolean completed) {
        this(new ModuleCode(code), units, completed);
    }

    /** Creates a module from an already-normalised module code value. */
    public Module(ModuleCode code, int units, boolean completed) {
        if (code == null) {
            throw new IllegalArgumentException("Module code must be provided");
        }
        if (units <= 0) {
            throw new IllegalArgumentException("Module units must be positive");
        }
        this.code = code;
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
