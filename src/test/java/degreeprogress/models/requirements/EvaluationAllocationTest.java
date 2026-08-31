package degreeprogress.models.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class EvaluationAllocationTest {
    @Test
    void constructor_invalidModuleCode_rejectsAllocation() {
        Set<String> moduleCodes = new HashSet<>();
        moduleCodes.add(null);

        assertThrows(IllegalArgumentException.class, () -> createAllocation(
                Map.of("root", moduleCodes), Map.of(), Set.of()));
    }

    @Test
    void constructor_blankModuleCode_rejectsAllocation() {
        assertThrows(IllegalArgumentException.class, () -> createAllocation(
                Map.of("root", Set.of("")), Map.of(), Set.of()));
    }

    @Test
    void constructor_mutableInputs_copiesAllocationData() {
        Set<String> rootCodes = new HashSet<>(Set.of("CS1231S"));
        Map<String, Set<String>> rootAllocations = new HashMap<>();
        rootAllocations.put("root", rootCodes);
        EvaluationAllocation allocation = createAllocation(rootAllocations, Map.of(), Set.of());

        rootCodes.add("CS2040S");
        rootAllocations.put("other", Set.of("CS2100"));

        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesByRoot().get("root"));
        assertEquals(Set.of(), allocation.creditedModuleCodesByRoot().getOrDefault("other", Set.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> allocation.creditedModuleCodesByRoot().put("other", Set.of("CS2100")));
    }

    private EvaluationAllocation createAllocation(
            Map<String, Set<String>> rootAllocations,
            Map<String, Set<String>> requirementAllocations,
            Set<String> unrestrictedElectives) {
        EvaluationResult result = EvaluationResult.leaf("root", true, 1, 1);
        return new EvaluationAllocation(
                List.of(result), rootAllocations, requirementAllocations, unrestrictedElectives);
    }
}
