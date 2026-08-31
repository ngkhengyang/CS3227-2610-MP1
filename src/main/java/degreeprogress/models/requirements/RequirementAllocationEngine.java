package degreeprogress.models.requirements;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import degreeprogress.models.modules.Module;

/**
 * Allocates completed modules to specific requirements and unrestricted electives.
 *
 * <p>Specific roots are evaluated against selected module subsets. Modules listed
 * by a specific-module leaf are reserved from broad count requirements, while
 * non-explicit modules are claimed by at most one specific root. The
 * unrestricted-elective root receives the remaining completed modules, while the
 * degree-total root evaluates every completed module without consuming any
 * allocation.</p>
 */
public final class RequirementAllocationEngine {
    private static final int MAX_EXACT_CANDIDATES = 20;

    /**
     * Evaluates the supplied root requirements and calculates their module allocation.
     *
     * @param requirements root requirements to evaluate
     * @param modules module snapshot to evaluate
     * @return the evaluation results and credited module allocation
     * @throws IllegalArgumentException if the arguments are invalid or a reserved
     *         allocation requirement occurs more than once
     */
    public EvaluationAllocation evaluate(
            Collection<Requirement> requirements, Collection<Module> modules) {
        if (requirements == null || requirements.stream().anyMatch(requirement -> requirement == null)) {
            throw new IllegalArgumentException("Requirements must not contain null values");
        }

        EvaluationContext context = new EvaluationContext(modules);
        List<Module> completedModules = context.getCompletedModules();
        Map<String, EvaluationResult> resultsByRoot = new HashMap<>();
        Map<String, Set<String>> creditedModuleCodesByRoot = new HashMap<>();
        Map<String, Set<String>> creditedModuleCodesByRequirement = new HashMap<>();
        Set<String> explicitModuleCodes = collectExplicitModuleCodes(requirements);
        Set<Module> creditedNonExplicitModules = new HashSet<>();
        Requirement unrestrictedElectives = null;

        List<Requirement> specificRequirements = requirements.stream()
                .filter(requirement -> RequirementAllocationPolicy.classifyRequirement(requirement)
                        == RequirementAllocationPolicy.SPECIFIC)
                .sorted(Comparator.comparing(requirement ->
                        collectExplicitModuleCodes(requirement).isEmpty()))
                .toList();

        for (Requirement requirement : specificRequirements) {
            RequirementAllocationPolicy policy =
                    RequirementAllocationPolicy.classifyRequirement(requirement);
            if (policy != RequirementAllocationPolicy.SPECIFIC) {
                continue;
            }

            Set<String> requirementExplicitCodes = collectExplicitModuleCodes(requirement);
            Set<Module> availableModules = getAvailableModules(
                    completedModules,
                    creditedNonExplicitModules,
                    explicitModuleCodes,
                    requirementExplicitCodes);
            Set<Module> allocatedModules = allocateSpecificRequirement(
                    requirement, availableModules, explicitModuleCodes);
            resultsByRoot.put(
                    requirement.getId(), evaluate(requirement, allocatedModules, explicitModuleCodes));
            creditedModuleCodesByRoot.put(requirement.getId(), getModuleCodes(allocatedModules));
            recordRequirementAllocations(
                    requirement,
                    allocatedModules,
                    explicitModuleCodes,
                    creditedModuleCodesByRequirement);
            allocatedModules.stream()
                    .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                    .forEach(creditedNonExplicitModules::add);
        }

        for (Requirement requirement : requirements) {
            RequirementAllocationPolicy policy =
                    RequirementAllocationPolicy.classifyRequirement(requirement);
            if (policy == RequirementAllocationPolicy.UNRESTRICTED_ELECTIVES) {
                if (unrestrictedElectives != null) {
                    throw new IllegalArgumentException(
                            "Only one unrestricted-elective requirement is supported");
                }
                unrestrictedElectives = requirement;
            } else if (policy == RequirementAllocationPolicy.DEGREE_TOTAL) {
                resultsByRoot.put(requirement.getId(), requirement.evaluate(context));
                creditedModuleCodesByRoot.put(requirement.getId(), Set.of());
                creditedModuleCodesByRequirement.put(requirement.getId(), Set.of());
            }
        }

        if (unrestrictedElectives != null) {
            Set<Module> remainingModules = new HashSet<>(completedModules);
            remainingModules.removeAll(creditedNonExplicitModules);
            for (Requirement requirement : specificRequirements) {
                Set<String> explicitCodes = collectExplicitModuleCodes(requirement);
                remainingModules.removeIf(module -> explicitCodes.contains(module.getCode()));
            }
            resultsByRoot.put(
                    unrestrictedElectives.getId(), unrestrictedElectives.evaluate(remainingModules));
            Set<String> remainingModuleCodes = getModuleCodes(remainingModules);
            creditedModuleCodesByRoot.put(unrestrictedElectives.getId(), remainingModuleCodes);
            recordRequirementAllocations(
                    unrestrictedElectives,
                    remainingModules,
                    Set.of(),
                    creditedModuleCodesByRequirement);
            return new EvaluationAllocation(
                    orderResults(requirements, resultsByRoot),
                    creditedModuleCodesByRoot,
                    creditedModuleCodesByRequirement,
                    remainingModuleCodes);
        }

        return new EvaluationAllocation(
                orderResults(requirements, resultsByRoot),
                creditedModuleCodesByRoot,
                creditedModuleCodesByRequirement,
                Set.of());
    }

    private List<EvaluationResult> orderResults(
            Collection<Requirement> requirements, Map<String, EvaluationResult> resultsByRoot) {
        return requirements.stream()
                .map(requirement -> resultsByRoot.get(requirement.getId()))
                .toList();
    }

    private Set<Module> allocateSpecificRequirement(
            Requirement requirement,
            Collection<Module> completedModules,
            Set<String> explicitModuleCodes) {
        Set<Module> candidates = collectCandidates(requirement, completedModules, explicitModuleCodes);
        Set<Module> mandatoryModules = collectMandatoryModules(requirement, completedModules);
        List<Module> optionalModules = candidates.stream()
                .filter(module -> !mandatoryModules.contains(module))
                .sorted(Comparator.comparing(Module::getCode))
                .toList();

        if (optionalModules.size() <= MAX_EXACT_CANDIDATES) {
            return findBestExactAllocation(
                    requirement, mandatoryModules, optionalModules, explicitModuleCodes);
        }
        return findBestGreedyAllocation(
                requirement, mandatoryModules, optionalModules, explicitModuleCodes);
    }

    private Set<Module> findBestExactAllocation(
            Requirement requirement,
            Set<Module> mandatoryModules,
            List<Module> optionalModules,
            Set<String> explicitModuleCodes) {
        ExactAllocationSearch search = new ExactAllocationSearch(
                requirement, mandatoryModules, optionalModules, explicitModuleCodes);
        search.search(0, new LinkedHashSet<>(mandatoryModules));
        return search.getBestSelection();
    }

    private Set<Module> findBestGreedyAllocation(
            Requirement requirement,
            Set<Module> mandatoryModules,
            List<Module> optionalModules,
            Set<String> explicitModuleCodes) {
        Set<Module> selectedModules = new LinkedHashSet<>(mandatoryModules);
        if (violatesMandatoryMaximums(requirement, selectedModules, explicitModuleCodes)) {
            return Set.of();
        }
        EvaluationResult currentResult = evaluate(requirement, selectedModules, explicitModuleCodes);

        while (!currentResult.fulfilled()) {
            Module bestModule = null;
            EvaluationResult bestResult = currentResult;
            for (Module optionalModule : optionalModules) {
                if (selectedModules.contains(optionalModule)) {
                    continue;
                }

                Set<Module> proposedSelection = new LinkedHashSet<>(selectedModules);
                proposedSelection.add(optionalModule);
                if (violatesMandatoryMaximums(
                        requirement, proposedSelection, explicitModuleCodes)) {
                    continue;
                }

                EvaluationResult proposedResult = evaluate(
                        requirement, proposedSelection, explicitModuleCodes);
                if (isBetterSelection(
                        proposedResult, proposedSelection, bestResult, selectedModules)) {
                    bestModule = optionalModule;
                    bestResult = proposedResult;
                }
            }

            if (bestModule == null) {
                break;
            }
            selectedModules.add(bestModule);
            currentResult = bestResult;
        }

        return removeRedundantModules(
                requirement, mandatoryModules, selectedModules, explicitModuleCodes);
    }

    private Set<Module> removeRedundantModules(
            Requirement requirement,
            Set<Module> mandatoryModules,
            Set<Module> selectedModules,
            Set<String> explicitModuleCodes) {
        if (violatesMandatoryMaximums(requirement, selectedModules, explicitModuleCodes)
                || !evaluate(requirement, selectedModules, explicitModuleCodes).fulfilled()) {
            return selectedModules;
        }

        List<Module> removableModules = selectedModules.stream()
                .filter(module -> !mandatoryModules.contains(module))
                .sorted(Comparator.comparingInt(Module::getUnits).reversed()
                        .thenComparing(Module::getCode))
                .toList();
        for (Module module : removableModules) {
            Set<Module> proposedSelection = new LinkedHashSet<>(selectedModules);
            proposedSelection.remove(module);
            if (evaluate(requirement, proposedSelection, explicitModuleCodes).fulfilled()) {
                selectedModules.remove(module);
            }
        }
        return selectedModules;
    }

    private EvaluationResult evaluate(
            Requirement requirement,
            Collection<Module> selectedModules,
            Set<String> explicitModuleCodes) {
        Map<String, Collection<Module>> moduleScopes = new HashMap<>();
        recordModuleScopes(requirement, selectedModules, explicitModuleCodes, moduleScopes);
        return requirement.evaluate(new EvaluationContext(selectedModules, moduleScopes));
    }

    private void recordModuleScopes(
            Requirement requirement,
            Collection<Module> selectedModules,
            Set<String> explicitModuleCodes,
            Map<String, Collection<Module>> moduleScopes) {
        if (requirement instanceof ModuleRequirement) {
            moduleScopes.put(requirement.getId(), selectedModules);
            return;
        }
        if (requirement instanceof ModuleCountRequirement
                || requirement instanceof UnitCountRequirement) {
            moduleScopes.put(
                    requirement.getId(),
                    selectedModules.stream()
                            .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                            .toList());
            return;
        }
        requirement.getChildren().forEach(child -> recordModuleScopes(
                child, selectedModules, explicitModuleCodes, moduleScopes));
    }

    private Set<Module> getAvailableModules(
            Collection<Module> completedModules,
            Set<Module> creditedNonExplicitModules,
            Set<String> explicitModuleCodes,
            Set<String> requirementExplicitCodes) {
        return completedModules.stream()
                .filter(module -> !creditedNonExplicitModules.contains(module))
                .filter(module -> !isExplicitModule(module, explicitModuleCodes)
                        || requirementExplicitCodes.contains(module.getCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> collectExplicitModuleCodes(Collection<Requirement> requirements) {
        Set<String> explicitModuleCodes = new HashSet<>();
        requirements.forEach(requirement -> collectExplicitModuleCodes(requirement, explicitModuleCodes));
        return explicitModuleCodes;
    }

    private Set<String> collectExplicitModuleCodes(Requirement requirement) {
        Set<String> explicitModuleCodes = new HashSet<>();
        collectExplicitModuleCodes(requirement, explicitModuleCodes);
        return explicitModuleCodes;
    }

    private void collectExplicitModuleCodes(
            Requirement requirement, Set<String> explicitModuleCodes) {
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            explicitModuleCodes.addAll(moduleRequirement.getModuleCodes());
            return;
        }
        requirement.getChildren().forEach(child -> collectExplicitModuleCodes(child, explicitModuleCodes));
    }

    private boolean isExplicitModule(Module module, Set<String> explicitModuleCodes) {
        return explicitModuleCodes.contains(module.getCode());
    }

    private Set<Module> collectCandidates(
            Requirement requirement,
            Collection<Module> completedModules,
            Set<String> explicitModuleCodes) {
        Set<Module> candidates = new HashSet<>();
        collectCandidates(requirement, completedModules, explicitModuleCodes, candidates);
        return candidates;
    }

    private Set<Module> recordRequirementAllocations(
            Requirement requirement,
            Collection<Module> allocatedModules,
            Set<String> explicitModuleCodes,
            Map<String, Set<String>> allocations) {
        Set<Module> requirementModules = new HashSet<>();
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            Set<String> moduleCodes = moduleRequirement.getModuleCodes();
            allocatedModules.stream()
                    .filter(module -> moduleCodes.contains(module.getCode()))
                    .forEach(requirementModules::add);
        } else if (requirement instanceof ModuleCountRequirement moduleCountRequirement) {
            allocatedModules.stream()
                    .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                    .filter(module -> moduleCountRequirement.getSelector().matches(module))
                    .forEach(requirementModules::add);
        } else if (requirement instanceof UnitCountRequirement unitCountRequirement) {
            allocatedModules.stream()
                    .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                    .filter(module -> unitCountRequirement.getSelector().matches(module))
                    .forEach(requirementModules::add);
        } else {
            requirement.getChildren().forEach(child -> requirementModules.addAll(
                    recordRequirementAllocations(
                            child, allocatedModules, explicitModuleCodes, allocations)));
        }
        allocations.put(requirement.getId(), getModuleCodes(requirementModules));
        return requirementModules;
    }

    private void collectCandidates(
            Requirement requirement,
            Collection<Module> completedModules,
            Set<String> explicitModuleCodes,
            Set<Module> candidates) {
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            Set<String> moduleCodes = moduleRequirement.getModuleCodes();
            completedModules.stream()
                    .filter(module -> moduleCodes.contains(module.getCode()))
                    .forEach(candidates::add);
        } else if (requirement instanceof ModuleCountRequirement moduleCountRequirement) {
            completedModules.stream()
                    .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                    .filter(module -> moduleCountRequirement.getSelector().matches(module))
                    .forEach(candidates::add);
        } else if (requirement instanceof UnitCountRequirement unitCountRequirement) {
            completedModules.stream()
                    .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                    .filter(module -> unitCountRequirement.getSelector().matches(module))
                    .forEach(candidates::add);
        } else {
            requirement.getChildren().forEach(
                    child -> collectCandidates(
                            child, completedModules, explicitModuleCodes, candidates));
        }
    }

    private Set<Module> collectMandatoryModules(
            Requirement requirement, Collection<Module> completedModules) {
        Set<Module> mandatoryModules = new HashSet<>();
        collectMandatoryModules(requirement, completedModules, mandatoryModules);
        return mandatoryModules;
    }

    private void collectMandatoryModules(
            Requirement requirement, Collection<Module> completedModules, Set<Module> mandatoryModules) {
        if (requirement instanceof AnyOfRequirement) {
            return;
        }
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            Set<String> moduleCodes = moduleRequirement.getModuleCodes();
            completedModules.stream()
                    .filter(module -> moduleCodes.contains(module.getCode()))
                    .forEach(mandatoryModules::add);
            return;
        }
        requirement.getChildren().forEach(
                child -> collectMandatoryModules(child, completedModules, mandatoryModules));
    }

    private boolean violatesMandatoryMaximums(
            Requirement requirement,
            Set<Module> selectedModules,
            Set<String> explicitModuleCodes) {
        if (requirement instanceof AnyOfRequirement) {
            return false;
        }
        if (requirement instanceof ModuleCountRequirement moduleCountRequirement
                && moduleCountRequirement.getMaximumModules() != null
                && countMatchingModules(
                        moduleCountRequirement.getSelector(), selectedModules, explicitModuleCodes)
                        > moduleCountRequirement.getMaximumModules()) {
            return true;
        }
        if (requirement instanceof UnitCountRequirement unitCountRequirement
                && unitCountRequirement.getMaximumUnits() != null
                && countMatchingUnits(
                        unitCountRequirement.getSelector(), selectedModules, explicitModuleCodes)
                        > unitCountRequirement.getMaximumUnits()) {
            return true;
        }
        return requirement.getChildren().stream()
                .anyMatch(child -> violatesMandatoryMaximums(child, selectedModules, explicitModuleCodes));
    }

    private int countMatchingModules(
            ModuleSelector selector,
            Collection<Module> modules,
            Set<String> explicitModuleCodes) {
        return (int) modules.stream()
                .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                .filter(selector::matches)
                .count();
    }

    private int countMatchingUnits(
            ModuleSelector selector,
            Collection<Module> modules,
            Set<String> explicitModuleCodes) {
        return modules.stream()
                .filter(module -> !isExplicitModule(module, explicitModuleCodes))
                .filter(selector::matches)
                .mapToInt(Module::getUnits)
                .sum();
    }

    private boolean isBetterSelection(
            EvaluationResult candidateResult,
            Collection<Module> candidateSelection,
            EvaluationResult currentResult,
            Collection<Module> currentSelection) {
        if (candidateResult.fulfilled() != currentResult.fulfilled()) {
            return candidateResult.fulfilled();
        }

        if (candidateResult.fulfilled()) {
            return compareAllocationCost(candidateSelection, currentSelection) < 0;
        }

        int candidateFulfilledCount = countFulfilledResults(candidateResult);
        int currentFulfilledCount = countFulfilledResults(currentResult);
        if (candidateFulfilledCount != currentFulfilledCount) {
            return candidateFulfilledCount > currentFulfilledCount;
        }

        int candidateProgress = sumAchievedProgress(candidateResult);
        int currentProgress = sumAchievedProgress(currentResult);
        if (candidateProgress != currentProgress) {
            return candidateProgress > currentProgress;
        }

        return compareAllocationCost(candidateSelection, currentSelection) < 0;
    }

    private int compareAllocationCost(
            Collection<Module> firstSelection, Collection<Module> secondSelection) {
        int unitComparison = Integer.compare(sumUnits(firstSelection), sumUnits(secondSelection));
        if (unitComparison != 0) {
            return unitComparison;
        }
        int moduleComparison = Integer.compare(firstSelection.size(), secondSelection.size());
        if (moduleComparison != 0) {
            return moduleComparison;
        }
        return getModuleCodeKey(firstSelection).compareTo(getModuleCodeKey(secondSelection));
    }

    private int countFulfilledResults(EvaluationResult result) {
        return (result.fulfilled() ? 1 : 0)
                + result.children().stream().mapToInt(this::countFulfilledResults).sum();
    }

    private int sumAchievedProgress(EvaluationResult result) {
        return result.achieved()
                + result.children().stream().mapToInt(this::sumAchievedProgress).sum();
    }

    private int sumUnits(Collection<Module> modules) {
        return modules.stream().mapToInt(Module::getUnits).sum();
    }

    private String getModuleCodeKey(Collection<Module> modules) {
        return modules.stream()
                .map(Module::getCode)
                .sorted()
                .reduce("", (first, second) -> first + second + "|");
    }

    private Set<String> getModuleCodes(Collection<Module> modules) {
        Set<String> moduleCodes = new HashSet<>();
        modules.forEach(module -> moduleCodes.add(module.getCode()));
        return Set.copyOf(moduleCodes);
    }

    private final class ExactAllocationSearch {
        private final Requirement requirement;
        private final List<Module> optionalModules;
        private final Set<String> explicitModuleCodes;
        private Set<Module> bestSelection;
        private EvaluationResult bestResult;

        private ExactAllocationSearch(
                Requirement requirement,
                Set<Module> mandatoryModules,
                List<Module> optionalModules,
                Set<String> explicitModuleCodes) {
            this.requirement = requirement;
            this.optionalModules = optionalModules;
            this.explicitModuleCodes = explicitModuleCodes;
            this.bestSelection = new LinkedHashSet<>(mandatoryModules);
            if (violatesMandatoryMaximums(requirement, bestSelection, explicitModuleCodes)) {
                this.bestSelection = new LinkedHashSet<>();
            }
            this.bestResult = evaluate(requirement, bestSelection, explicitModuleCodes);
        }

        private void search(int index, Set<Module> selectedModules) {
            if (violatesMandatoryMaximums(requirement, selectedModules, explicitModuleCodes)) {
                return;
            }

            EvaluationResult currentResult = evaluate(
                    requirement, selectedModules, explicitModuleCodes);
            if (isBetterSelection(
                    currentResult, selectedModules, bestResult, bestSelection)) {
                bestSelection = new LinkedHashSet<>(selectedModules);
                bestResult = currentResult;
            }
            if (currentResult.fulfilled() || index == optionalModules.size()) {
                return;
            }
            if (bestResult.fulfilled()
                    && sumUnits(selectedModules) >= sumUnits(bestSelection)) {
                return;
            }

            Module module = optionalModules.get(index);
            selectedModules.add(module);
            search(index + 1, selectedModules);
            selectedModules.remove(module);
            search(index + 1, selectedModules);
        }

        private Set<Module> getBestSelection() {
            return Set.copyOf(bestSelection);
        }
    }
}
