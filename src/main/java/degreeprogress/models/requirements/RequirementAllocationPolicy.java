package degreeprogress.models.requirements;

/**
 * Defines the built-in allocation behaviour of a root requirement.
 *
 * <p>These policies are inferred from the stable identifiers of the bundled
 * aggregate requirements. They are evaluation details and are not persisted
 * as user-configurable requirement attributes.</p>
 */
public enum RequirementAllocationPolicy {
    /** A requirement that may claim completed modules for its own progress. */
    SPECIFIC,

    /** The unrestricted-elective requirement that receives the remaining modules. */
    UNRESTRICTED_ELECTIVES,

    /** The degree-total requirement that evaluates modules without claiming them. */
    DEGREE_TOTAL;

    private static final String UNRESTRICTED_ELECTIVES_ID = "unrestricted-electives";
    private static final String DEGREE_TOTAL_ID = "degree-total";

    /**
     * Classifies a requirement using the built-in aggregate requirement ids.
     *
     * @param requirement requirement to classify
     * @return the allocation policy for the requirement
     * @throws IllegalArgumentException if the requirement is null
     */
    public static RequirementAllocationPolicy classifyRequirement(Requirement requirement) {
        if (requirement == null) {
            throw new IllegalArgumentException("Requirement must not be null");
        }
        if (UNRESTRICTED_ELECTIVES_ID.equals(requirement.getId())) {
            return UNRESTRICTED_ELECTIVES;
        }
        if (DEGREE_TOTAL_ID.equals(requirement.getId())) {
            return DEGREE_TOTAL;
        }
        return SPECIFIC;
    }
}
