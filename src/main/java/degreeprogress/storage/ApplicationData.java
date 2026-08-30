package degreeprogress.storage;

import degreeprogress.models.modules.ModuleDocument;
import degreeprogress.models.requirements.RequirementDocument;

/** Contains the complete application state stored in one JSON file. */
public record ApplicationData(
        int schemaVersion,
        ModuleDocument modules,
        RequirementDocument requirements) {

    /** Validates the application data aggregate. */
    public ApplicationData {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("Schema version must be positive");
        }
        if (modules == null) {
            throw new IllegalArgumentException("Module document is required");
        }
        if (requirements == null) {
            throw new IllegalArgumentException("Requirement document is required");
        }
    }
}
