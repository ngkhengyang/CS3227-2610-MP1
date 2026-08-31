package degreeprogress.models.requirements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains requirement results and the modules credited during one evaluation.
 *
 * <p>Credited module codes are recorded for root requirements. Child
 * requirements also have their own matching allocation, while composite
 * requirements contain the union of their children's allocations.</p>
 */
public record EvaluationAllocation(
        List<EvaluationResult> requirementResults,
        Map<String, Set<String>> creditedModuleCodesByRoot,
        Map<String, Set<String>> creditedModuleCodesByRequirement,
        Set<String> unrestrictedElectiveModuleCodes) {

    /** Creates an immutable evaluation allocation. */
    public EvaluationAllocation {
        if (requirementResults == null
                || requirementResults.stream().anyMatch(result -> result == null)) {
            throw new IllegalArgumentException("Requirement results must not contain null values");
        }
        if (hasInvalidAllocation(creditedModuleCodesByRoot)) {
            throw new IllegalArgumentException("Credited module allocations must not contain null values");
        }
        if (hasInvalidAllocation(creditedModuleCodesByRequirement)) {
            throw new IllegalArgumentException(
                    "Requirement module allocations must not contain null values");
        }
        if (unrestrictedElectiveModuleCodes == null
                || unrestrictedElectiveModuleCodes.stream().anyMatch(code -> code == null || code.isBlank())) {
            throw new IllegalArgumentException(
                    "Unrestricted-elective module codes must not contain blank values");
        }

        creditedModuleCodesByRoot = copyAllocations(creditedModuleCodesByRoot);
        creditedModuleCodesByRequirement = copyAllocations(creditedModuleCodesByRequirement);
        requirementResults = List.copyOf(requirementResults);
        unrestrictedElectiveModuleCodes = Set.copyOf(unrestrictedElectiveModuleCodes);
    }

    private static boolean hasInvalidAllocation(Map<String, Set<String>> allocations) {
        return allocations == null
                || allocations.entrySet().stream()
                        .anyMatch(entry -> entry.getKey() == null
                                || entry.getKey().isBlank()
                                || entry.getValue() == null
                                || entry.getValue().stream()
                                        .anyMatch(code -> code == null || code.isBlank()));
    }

    private static Map<String, Set<String>> copyAllocations(
            Map<String, Set<String>> allocations) {
        Map<String, Set<String>> copiedAllocations = new HashMap<>();
        allocations.forEach(
                (requirementId, moduleCodes) -> copiedAllocations.put(requirementId, Set.copyOf(moduleCodes)));
        return Map.copyOf(copiedAllocations);
    }

    /** Returns the module codes credited to the supplied requirement. */
    public Set<String> creditedModuleCodesFor(String requirementId) {
        if (requirementId == null || requirementId.isBlank()) {
            throw new IllegalArgumentException("Requirement id must not be blank");
        }
        return creditedModuleCodesByRequirement.getOrDefault(requirementId, Set.of());
    }

    /** Finds a result by id, including results nested under composite requirements. */
    public EvaluationResult findResult(String requirementId) {
        if (requirementId == null || requirementId.isBlank()) {
            throw new IllegalArgumentException("Requirement id must not be blank");
        }
        for (EvaluationResult result : requirementResults) {
            EvaluationResult matchingResult = findResult(requirementId, result);
            if (matchingResult != null) {
                return matchingResult;
            }
        }
        throw new IllegalArgumentException(
                "No evaluation result exists with this id: " + requirementId);
    }

    private EvaluationResult findResult(String requirementId, EvaluationResult result) {
        if (result.requirementId().equals(requirementId)) {
            return result;
        }
        for (EvaluationResult child : result.children()) {
            EvaluationResult matchingResult = findResult(requirementId, child);
            if (matchingResult != null) {
                return matchingResult;
            }
        }
        return null;
    }
}
