package degreeprogress.models.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class RequirementAllocationPolicyTest {
    @Test
    void classifyRequirement_unrestrictedElectivesId_returnsRemainderPolicy() {
        Requirement requirement = new ModuleRequirement(
                "unrestricted-electives", "Unrestricted Electives", "", Set.of("CS1231S"));

        assertEquals(
                RequirementAllocationPolicy.UNRESTRICTED_ELECTIVES,
                RequirementAllocationPolicy.classifyRequirement(requirement));
    }

    @Test
    void classifyRequirement_degreeTotalId_returnsNonConsumingPolicy() {
        Requirement requirement = new ModuleRequirement(
                "degree-total", "Degree Total", "", Set.of("CS1231S"));

        assertEquals(
                RequirementAllocationPolicy.DEGREE_TOTAL,
                RequirementAllocationPolicy.classifyRequirement(requirement));
    }

    @Test
    void classifyRequirement_otherId_returnsSpecificPolicy() {
        Requirement requirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S"));

        assertEquals(
                RequirementAllocationPolicy.SPECIFIC,
                RequirementAllocationPolicy.classifyRequirement(requirement));
    }

    @Test
    void classifyRequirement_nullRequirement_rejectsClassification() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RequirementAllocationPolicy.classifyRequirement(null));
    }
}
