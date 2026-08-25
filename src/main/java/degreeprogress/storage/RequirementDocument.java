package degreeprogress.storage;

import degreeprogress.requirements.Requirement;

import java.util.List;

/** The complete JSON-serialisable document containing requirement roots. */
public record RequirementDocument(
        int schemaVersion,
        ProgrammeInfo programme,
        List<String> sources,
        List<Requirement> requirements) {

    public RequirementDocument {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("Schema version must be positive");
        }
        if (programme == null) {
            throw new IllegalArgumentException("Programme information is required");
        }
        sources = List.copyOf(sources == null ? List.of() : sources);
        requirements = List.copyOf(requirements == null ? List.of() : requirements);
        if (requirements.stream().anyMatch(requirement -> requirement == null)) {
            throw new IllegalArgumentException("Requirements must not contain null values");
        }
    }
}
