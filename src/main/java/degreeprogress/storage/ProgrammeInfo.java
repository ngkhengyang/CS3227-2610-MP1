package degreeprogress.storage;

import java.util.List;

/** Metadata stored alongside a requirement document. */
public record ProgrammeInfo(
        String id,
        String name,
        String cohort,
        int totalUnits,
        List<String> focusAreas) {

    public ProgrammeInfo {
        focusAreas = List.copyOf(focusAreas == null ? List.of() : focusAreas);
    }
}
