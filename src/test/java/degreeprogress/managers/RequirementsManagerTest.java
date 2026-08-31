package degreeprogress.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import degreeprogress.models.modules.Module;
import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.DegreeProgress;
import degreeprogress.models.requirements.EvaluationAllocation;
import degreeprogress.models.requirements.EvaluationResult;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ModuleSelector;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.UnitCountRequirement;

class RequirementsManagerTest {
    @Test
    void addRequirement_storesSupportedTypesInOrder() {
        RequirementsManager manager = new RequirementsManager();
        Requirement moduleRequirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S"));
        Requirement moduleCountRequirement = new ModuleCountRequirement(
                "advanced", "Advanced", "", ModuleSelector.allModules(), 2);
        Requirement unitCountRequirement = new UnitCountRequirement(
                "breadth", "Breadth", "", ModuleSelector.allModules(), 8);

        assertSame(moduleRequirement, manager.addRequirement(moduleRequirement));
        manager.addRequirement(moduleCountRequirement);
        manager.addRequirement(unitCountRequirement);

        assertEquals(List.of(moduleRequirement, moduleCountRequirement, unitCountRequirement),
                manager.getRequirements());
    }

    @Test
    void addRequirement_duplicateNestedId_rejectsAddition() {
        Requirement child = new ModuleRequirement(
                "child", "Child", "", Set.of("CS1231S"));
        AllOfRequirement parent = new AllOfRequirement(
                "parent", "Parent", "", List.of(child));
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addRequirement(new ModuleRequirement(
                        "child", "Different child", "", Set.of("CS2040S"))));
    }

    @Test
    void addRequirement_rejectsNull() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class, () -> manager.addRequirement(null));
        assertTrue(manager.getRequirements().isEmpty());
    }

    @Test
    void addRequirement_rejectsDuplicateId() {
        RequirementsManager manager = new RequirementsManager();
        manager.addRequirement(new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S")));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addRequirement(new UnitCountRequirement(
                        "foundation", "Different requirement", "",
                        ModuleSelector.allModules(), 8)));
        assertEquals(1, manager.getRequirements().size());
    }

    @Test
    void addChildRequirement_addsToDirectParent() {
        AllOfRequirement parent = new AllOfRequirement(
                "foundation", "Foundation", "", List.of());
        RequirementsManager manager = new RequirementsManager(List.of(parent));
        Requirement child = new ModuleRequirement(
                "programming", "Programming", "", Set.of("CS2040S"));

        assertSame(child, manager.addChildRequirement("foundation", child));
        assertEquals(List.of(child), parent.getChildren());
    }

    @Test
    void addChildRequirement_findsNestedParent() {
        AllOfRequirement nestedParent = new AllOfRequirement(
                "focus", "Focus", "", List.of());
        AllOfRequirement root = new AllOfRequirement(
                "degree", "Degree", "", List.of(nestedParent));
        RequirementsManager manager = new RequirementsManager(List.of(root));
        Requirement child = new UnitCountRequirement(
                "focus-units", "Focus units", "", ModuleSelector.allModules(), 12);

        manager.addChildRequirement("focus", child);

        assertEquals(List.of(child), nestedParent.getChildren());
    }

    @Test
    void addChildRequirement_rejectsLeafParent() {
        Requirement leaf = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(leaf));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addChildRequirement("foundation", new ModuleRequirement(
                        "child", "Child", "", Set.of("CS2040S"))));
    }

    @Test
    void addChildRequirement_rejectsMissingParent() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.addChildRequirement("missing", new ModuleRequirement(
                        "child", "Child", "", Set.of("CS2040S"))));
    }

    @Test
    void addChildRequirement_rejectsNullChild() {
        AllOfRequirement parent = new AllOfRequirement(
                "foundation", "Foundation", "", List.of());
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addChildRequirement("foundation", null));
    }

    @Test
    void addChildRequirement_rejectsBlankParentId() {
        AllOfRequirement parent = new AllOfRequirement(
                "foundation", "Foundation", "", List.of());
        RequirementsManager manager = new RequirementsManager(List.of(parent));
        Requirement child = new ModuleRequirement(
                "child", "Child", "", Set.of("CS2040S"));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addChildRequirement("", child));
    }

    @Test
    void addChildRequirement_rejectsDuplicateIdWithoutMutation() {
        AllOfRequirement parent = new AllOfRequirement(
                "foundation", "Foundation", "", List.of());
        RequirementsManager manager = new RequirementsManager(List.of(parent));
        manager.addChildRequirement("foundation", new ModuleRequirement(
                "programming", "Programming", "", Set.of("CS2040S")));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addChildRequirement("foundation", new UnitCountRequirement(
                        "programming", "Different", "", ModuleSelector.allModules(), 4)));
        assertEquals(1, parent.getChildren().size());
    }

    @Test
    void deleteRequirement_removesRoot() {
        Requirement first = new ModuleRequirement(
                "first", "First", "", Set.of("CS1231S"));
        Requirement second = new UnitCountRequirement(
                "second", "Second", "", ModuleSelector.allModules(), 4);
        RequirementsManager manager = new RequirementsManager(List.of(first, second));

        assertSame(first, manager.deleteRequirement("first"));
        assertEquals(List.of(second), manager.getRequirements());
    }

    @Test
    void deleteRequirement_removesChild() {
        Requirement child = new ModuleRequirement(
                "child", "Child", "", Set.of("CS2040S"));
        AllOfRequirement parent = new AllOfRequirement(
                "parent", "Parent", "", List.of(child));
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertSame(child, manager.deleteRequirement("child"));
        assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    void deleteRequirement_removesNestedChild() {
        Requirement nestedChild = new ModuleRequirement(
                "nested-child", "Nested child", "", Set.of("CS1231S"));
        AllOfRequirement nestedParent = new AllOfRequirement(
                "nested-parent", "Nested parent", "", List.of(nestedChild));
        AllOfRequirement root = new AllOfRequirement(
                "root", "Root", "", List.of(nestedParent));
        RequirementsManager manager = new RequirementsManager(List.of(root));

        assertSame(nestedChild, manager.deleteRequirement("nested-child"));
        assertTrue(nestedParent.getChildren().isEmpty());
    }

    @Test
    void deleteRequirement_removesCompositeChildWithDescendants() {
        Requirement nestedChild = new ModuleRequirement(
                "nested-child", "Nested child", "", Set.of("CS1231S"));
        AllOfRequirement child = new AllOfRequirement(
                "child", "Child", "", List.of(nestedChild));
        AllOfRequirement parent = new AllOfRequirement(
                "parent", "Parent", "", List.of(child));
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertSame(child, manager.deleteRequirement("child"));
        assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    void deleteRequirement_rejectsNullOrBlankId() {
        Requirement leaf = new ModuleRequirement(
                "leaf", "Leaf", "", Set.of("CS1231S"));
        AllOfRequirement parent = new AllOfRequirement(
                "parent", "Parent", "", List.of(leaf));
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement(null));
        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement(""));
    }

    @Test
    void deleteRequirement_rejectsMissingId() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement("missing"));
    }

    @Test
    void getRequirements_returnsUnmodifiableSnapshot() {
        RequirementsManager manager = new RequirementsManager();
        manager.addRequirement(new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S")));

        assertThrows(UnsupportedOperationException.class,
                () -> manager.getRequirements().clear());
    }

    @Test
    void evaluateRequirement_completedModule_returnsFulfilledResult() {
        List<Module> modules = List.of(
                new Module("CS1231S", 4, true), 
                new Module("CS2040S", 4, true)
        );
        RequirementsManager requirementsManager = new RequirementsManager(List.of(
                new ModuleRequirement("foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"))));
        ModulesManager modulesManager = new ModulesManager(modules);

        EvaluationResult result = requirementsManager.evaluateRequirement(
                "foundation", modulesManager);

        assertTrue(result.fulfilled());
        assertEquals(2, result.achieved());
        assertEquals(2, result.target());
    }

    @Test
    void evaluateRequirement_completedModule_returnsUnfulfilledResult() {
        List<Module> modules = List.of(
                new Module("CS1231S", 4, true), 
                new Module("CS2040S", 4, false)
        );
        RequirementsManager requirementsManager = new RequirementsManager(List.of(
                new ModuleRequirement("foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"))));
        ModulesManager modulesManager = new ModulesManager(modules);

        EvaluationResult result = requirementsManager.evaluateRequirement(
                "foundation", modulesManager);

        assertFalse(result.fulfilled());
        assertEquals(1, result.achieved());
        assertEquals(2, result.target());
    }

    @Test
    void evaluateRequirement_nestedRequirement_returnsNestedResult() {
        Requirement cs1231sRequirement = new ModuleRequirement(
                "1231requirement", "1231requirement", "", Set.of("CS1231S"));
        Requirement cs2040sRequirement = new ModuleRequirement(
                "2040srequirement", "2040srequirement", "", Set.of("CS2040S"));
        Requirement root = new AllOfRequirement(
                "root", "Root", "", List.of(cs1231sRequirement, cs2040sRequirement));
        RequirementsManager requirementsManager = new RequirementsManager(List.of(root));

        List<Module> modules = List.of(
                new Module("CS1231S", 4, true), 
                new Module("CS2040S", 4, false)
        );
        EvaluationResult rootResult = requirementsManager.evaluateRequirement(
                "root", modules);

        assertEquals("root", rootResult.requirementId());
        assertFalse(rootResult.fulfilled());
        assertTrue(rootResult.children().get(0).fulfilled());
        assertFalse(rootResult.children().get(1).fulfilled());
    }

    @Test
    void evaluateRequirement_unknownId_rejectsEvaluation() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.evaluateRequirement(
                        "missing", List.of(new Module("CS1231S", 4, true))));
    }

    @Test
    void evaluateRequirements_returnsRootResultsInOrder() {
        Requirement first = new ModuleRequirement(
                "first", "First", "", Set.of("CS1231S"));
        Requirement second = new UnitCountRequirement(
                "second", "Second", "", ModuleSelector.allModules(), 4);
        RequirementsManager manager = new RequirementsManager(List.of(first, second));

        List<EvaluationResult> results = manager.evaluateRequirements(
                List.of(
                        new Module("CS1231S", 4, true),
                        new Module("CS2040S", 4, true)));

        assertEquals(List.of("first", "second"),
                results.stream().map(EvaluationResult::requirementId).toList());
        assertTrue(results.get(0).fulfilled());
        assertTrue(results.get(1).fulfilled());
    }

    @Test
    void evaluateAllocation_specificModulesAreNotReusedByBroadSiblingRequirement() {
        Requirement foundation = new ModuleRequirement(
                "foundation", "Computer Science Foundation", "", Set.of("CS1231S"));
        Requirement breadth = new UnitCountRequirement(
                "breadth", "Computing Breadth and Depth", "",
                new ModuleSelector(Set.of(), Set.of("CS"), null, null), 4);
        Requirement programme = new AllOfRequirement(
                "programme", "Programme Requirements", "", List.of(foundation, breadth));
        RequirementsManager manager = new RequirementsManager(List.of(programme));

        EvaluationAllocation allocation = manager.evaluateAllocation(List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true)));

        assertTrue(allocation.findResult("programme").fulfilled());
        assertTrue(allocation.findResult("foundation").fulfilled());
        assertTrue(allocation.findResult("breadth").fulfilled());
        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesFor("foundation"));
        assertEquals(Set.of("CS2040S"), allocation.creditedModuleCodesFor("breadth"));
        assertEquals(
                Set.of("CS1231S", "CS2040S"),
                allocation.creditedModuleCodesFor("programme"));
    }

    @Test
    void evaluateAllocation_duplicateSpecificModuleClaims_allowExplicitOverlap() {
        Requirement first = new ModuleRequirement(
                "first", "First", "", Set.of("CS1231S"));
        Requirement second = new ModuleRequirement(
                "second", "Second", "", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(first, second));

        EvaluationAllocation allocation = manager.evaluateAllocation(List.of(
                new Module("CS1231S", 4, true)));

        assertTrue(allocation.findResult("first").fulfilled());
        assertTrue(allocation.findResult("second").fulfilled());
        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesFor("first"));
        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesFor("second"));
    }

    @Test
    void evaluateAllocation_broadRequirementsDoNotReuseNonExplicitModules() {
        Requirement first = new UnitCountRequirement(
                "first", "First", "", ModuleSelector.allModules(), 4);
        Requirement second = new UnitCountRequirement(
                "second", "Second", "", ModuleSelector.allModules(), 4);
        RequirementsManager manager = new RequirementsManager(List.of(first, second));

        EvaluationAllocation allocation = manager.evaluateAllocation(List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true)));

        assertTrue(allocation.findResult("first").fulfilled());
        assertTrue(allocation.findResult("second").fulfilled());
        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesFor("first"));
        assertEquals(Set.of("CS2040S"), allocation.creditedModuleCodesFor("second"));
    }

    @Test
    void evaluateAllocation_unrestrictedElectivesUsesUncreditedModules() {
        Requirement idCdRequirement = new AllOfRequirement(
                "id-cd-education",
                "Interdisciplinary and Cross-Disciplinary Education",
                "",
                List.of(
                        new UnitCountRequirement(
                                "id-cd-total-units",
                                "ID/CD total",
                                "",
                                ModuleSelector.forCodes("DTK1234", "IS1128", "DAO2703", "MNO1706X"),
                                12,
                                12),
                        new ModuleCountRequirement(
                                "id-course-count",
                                "At least two ID courses",
                                "",
                                ModuleSelector.forCodes("DTK1234", "IS1128"),
                                2),
                        new ModuleCountRequirement(
                                "cd-course-count",
                                "At most one CD course",
                                "",
                                ModuleSelector.forCodes("DAO2703", "MNO1706X"),
                                0,
                                1)));
        Requirement unrestrictedElectives = new UnitCountRequirement(
                "unrestricted-electives",
                "Unrestricted Electives",
                "",
                ModuleSelector.allModules(),
                4);
        Requirement degreeTotal = new UnitCountRequirement(
                "degree-total", "Degree Total", "", ModuleSelector.allModules(), 16);
        RequirementsManager manager = new RequirementsManager(
                List.of(idCdRequirement, unrestrictedElectives, degreeTotal));
        List<Module> modules = List.of(
                new Module("DTK1234", 4, true),
                new Module("IS1128", 4, true),
                new Module("DAO2703", 4, true),
                new Module("MNO1706X", 4, true));

        EvaluationAllocation allocation = manager.evaluateAllocation(modules);

        assertEquals(
                List.of("id-cd-education", "unrestricted-electives", "degree-total"),
                allocation.requirementResults().stream()
                        .map(EvaluationResult::requirementId)
                        .toList());
        assertTrue(allocation.findResult("id-cd-education").fulfilled());
        assertTrue(allocation.findResult("id-course-count").fulfilled());
        assertEquals(1, allocation.findResult("cd-course-count").achieved());
        assertEquals(
                Set.of("DTK1234", "IS1128", "DAO2703"),
                allocation.creditedModuleCodesByRoot().get("id-cd-education"));
        assertEquals(
                Set.of("DTK1234", "IS1128", "DAO2703"),
                allocation.creditedModuleCodesFor("id-cd-education"));
        assertEquals(
                Set.of("DTK1234", "IS1128"),
                allocation.creditedModuleCodesFor("id-course-count"));
        assertEquals(
                Set.of("DAO2703"),
                allocation.creditedModuleCodesFor("cd-course-count"));
        assertEquals(Set.of("MNO1706X"), allocation.unrestrictedElectiveModuleCodes());
        assertTrue(allocation.findResult("unrestricted-electives").fulfilled());
        assertTrue(allocation.findResult("degree-total").fulfilled());
    }

    @Test
    void evaluateAllocation_atLeastRequirementLeavesExtraMatchingModulesForElectives() {
        Requirement specificRequirement = new ModuleCountRequirement(
                "specific", "Specific modules", "", ModuleSelector.allModules(), 2);
        Requirement unrestrictedElectives = new UnitCountRequirement(
                "unrestricted-electives",
                "Unrestricted Electives",
                "",
                ModuleSelector.allModules(),
                4);
        RequirementsManager manager = new RequirementsManager(
                List.of(specificRequirement, unrestrictedElectives));
        List<Module> modules = List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true),
                new Module("CS2100", 4, true));

        EvaluationAllocation allocation = manager.evaluateAllocation(modules);

        assertTrue(allocation.findResult("specific").fulfilled());
        assertEquals(2, allocation.findResult("specific").achieved());
        assertEquals(2, allocation.creditedModuleCodesByRoot().get("specific").size());
        assertEquals(Set.of("CS2100"), allocation.unrestrictedElectiveModuleCodes());
        assertTrue(allocation.findResult("unrestricted-electives").fulfilled());
    }

    @Test
    void evaluateAllocation_multipleSpecificRoots_excludesAllCreditedModulesFromElectives() {
        Requirement firstRequirement = new ModuleRequirement(
                "first", "First requirement", "", Set.of("CS1231S"));
        Requirement secondRequirement = new ModuleRequirement(
                "second", "Second requirement", "", Set.of("CS2040S"));
        Requirement unrestrictedElectives = new UnitCountRequirement(
                "unrestricted-electives",
                "Unrestricted Electives",
                "",
                ModuleSelector.allModules(),
                4);
        RequirementsManager manager = new RequirementsManager(
                List.of(firstRequirement, secondRequirement, unrestrictedElectives));

        EvaluationAllocation allocation = manager.evaluateAllocation(List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true),
                new Module("CS2100", 4, true)));

        assertEquals(Set.of("CS1231S"), allocation.creditedModuleCodesByRoot().get("first"));
        assertEquals(Set.of("CS2040S"), allocation.creditedModuleCodesByRoot().get("second"));
        assertEquals(Set.of("CS2100"), allocation.unrestrictedElectiveModuleCodes());
    }

    @Test
    void evaluateAllocation_editedElectiveMinimumChangesProgress() {
        Requirement specificRequirement = new ModuleCountRequirement(
                "specific", "Specific modules", "", ModuleSelector.allModules(), 2);
        Requirement unrestrictedElectives = new UnitCountRequirement(
                "unrestricted-electives",
                "Unrestricted Electives",
                "",
                ModuleSelector.allModules(),
                8);
        RequirementsManager manager = new RequirementsManager(
                List.of(specificRequirement, unrestrictedElectives));
        List<Module> modules = List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true),
                new Module("CS2100", 4, true));

        EvaluationResult result = manager.evaluateRequirement(
                "unrestricted-electives", modules);

        assertFalse(result.fulfilled());
        assertEquals(4, result.achieved());
        assertEquals(8, result.target());
    }

    @Test
    void getRootRequirement_nestedRequirement_returnsContainingRoot() {
        Requirement child = new ModuleRequirement(
                "child", "Child", "", Set.of("CS1231S"));
        Requirement root = new AllOfRequirement("root", "Root", "", List.of(child));
        RequirementsManager manager = new RequirementsManager(List.of(root));

        assertSame(root, manager.getRootRequirement("child"));
    }

    @Test
    void evaluateDegree_incompleteRoot_returnsIncompleteProgress() {
        Requirement first = new ModuleRequirement(
                "first", "First", "", Set.of("CS1231S"));
        Requirement second = new ModuleRequirement(
                "second", "Second", "", Set.of("CS2040S"));
        RequirementsManager requirementsManager = new RequirementsManager(List.of(first, second));

        DegreeProgress progress = requirementsManager.evaluateDegree(
                new ModulesManager(List.of(new Module("CS1231S", 4, true))));

        assertFalse(progress.fulfilled());
        assertEquals(1, progress.achievedRequirements());
        assertEquals(2, progress.totalRequirements());
        assertEquals(List.of("first", "second"), progress.requirementResults().stream()
                .map(EvaluationResult::requirementId)
                .toList());
    }

    @Test
    void evaluateDegree_allRootsFulfilled_returnsCompleteProgress() {
        Requirement first = new ModuleRequirement(
                "first", "First", "", Set.of("CS1231S"));
        Requirement second = new UnitCountRequirement(
                "second", "Second", "", ModuleSelector.allModules(), 4);
        RequirementsManager requirementsManager = new RequirementsManager(List.of(first, second));
        ModulesManager modulesManager = new ModulesManager(List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, true)));

        DegreeProgress progress = requirementsManager.evaluateDegree(modulesManager);

        assertTrue(progress.fulfilled());
        assertEquals(2, progress.achievedRequirements());
        assertEquals(2, progress.totalRequirements());
    }

    @Test
    void evaluateDegree_afterModuleStateChange_reflectsLatestState() {
        RequirementsManager requirementsManager = new RequirementsManager(List.of(
                new ModuleRequirement("foundation", "Foundation", "", Set.of("CS1231S"))));
        ModulesManager modulesManager = new ModulesManager(List.of(
                new Module("CS1231S", 4, false)));

        assertFalse(requirementsManager.evaluateDegree(modulesManager).fulfilled());

        modulesManager.markModuleCompleted("CS1231S");

        assertTrue(requirementsManager.evaluateDegree(modulesManager).fulfilled());
    }

    @Test
    void evaluateDegree_nullModulesManager_rejectsEvaluation() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.evaluateDegree((ModulesManager) null));
    }

    @Test
    void editRequirement_sameModuleType_updatesMetadataAndModuleCodes() {
        ModuleRequirement existing = new ModuleRequirement(
                "foundation", "Foundation", "old", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        ModuleRequirement edited = new ModuleRequirement(
                "foundation", "Updated foundation", "new", Set.of("CS2040S"));

        assertSame(existing, manager.editRequirement("foundation", edited));
        assertEquals("Updated foundation", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(Set.of("CS2040S"), existing.getModuleCodes());
    }

    @Test
    void editRequirement_sameModuleCountType_updatesMetadataSelectorAndBounds() {
        ModuleCountRequirement existing = new ModuleCountRequirement(
                "advanced", "Advanced", "old", ModuleSelector.allModules(), 1);
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        ModuleSelector selector = ModuleSelector.forCodes("CS4248");
        ModuleCountRequirement edited = new ModuleCountRequirement(
                "advanced", "Updated advanced", "new", selector, 2, 4);

        manager.editRequirement("advanced", edited);

        assertEquals("Updated advanced", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(selector, existing.getSelector());
        assertEquals(2, existing.getMinimumModules());
        assertEquals(4, existing.getMaximumModules());
    }

    @Test
    void editRequirement_sameUnitCountType_updatesMetadataSelectorAndBounds() {
        UnitCountRequirement existing = new UnitCountRequirement(
                "breadth", "Breadth", "old", ModuleSelector.allModules(), 8);
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        ModuleSelector selector = ModuleSelector.forCodes("CS1231S", "CS2040S");
        UnitCountRequirement edited = new UnitCountRequirement(
                "breadth", "Updated breadth", "new", selector, 12, 20);

        manager.editRequirement("breadth", edited);

        assertEquals("Updated breadth", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(selector, existing.getSelector());
        assertEquals(12, existing.getMinimumUnits());
        assertEquals(20, existing.getMaximumUnits());
    }

    @Test
    void editRequirement_unrestrictedElectives_updatesMinimumUnits() {
        UnitCountRequirement existing = new UnitCountRequirement(
                "unrestricted-electives",
                "Unrestricted Electives",
                "",
                ModuleSelector.allModules(),
                40);
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        UnitCountRequirement edited = new UnitCountRequirement(
                "unrestricted-electives", "Unrestricted Electives", "",
                ModuleSelector.allModules(), 48);

        manager.editRequirement("unrestricted-electives", edited);

        assertEquals(48, existing.getMinimumUnits());
    }

    @Test
    void editRequirement_sameCompositeType_updatesMetadataAndPreservesChildren() {
        Requirement child = new ModuleRequirement(
                "child", "Child", "", Set.of("CS1231S"));
        AllOfRequirement existing = new AllOfRequirement(
                "parent", "Parent", "old", List.of(child));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        AllOfRequirement edited = new AllOfRequirement(
                "parent", "Updated parent", "new", List.of());

        assertSame(existing, manager.editRequirement("parent", edited));
        assertEquals("Updated parent", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(List.of(child), existing.getChildren());
    }

    @Test
    void editRequirement_allOfToAnyOf_preservesIdChildrenAndOrder() {
        Requirement firstChild = new ModuleRequirement(
                "first-child", "First child", "", Set.of("CS1231S"));
        Requirement secondChild = new ModuleRequirement(
                "second-child", "Second child", "", Set.of("CS2040S"));
        AllOfRequirement existing = new AllOfRequirement(
                "parent", "Parent", "old", List.of(firstChild, secondChild));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        AnyOfRequirement edited = new AnyOfRequirement(
                "parent", "Updated parent", "new", List.of());

        Requirement converted = manager.editRequirement("parent", edited);

        assertEquals(AnyOfRequirement.class, converted.getClass());
        assertEquals("parent", converted.getId());
        assertEquals("Updated parent", converted.getName());
        assertEquals("new", converted.getDescription());
        assertEquals(List.of(firstChild, secondChild), converted.getChildren());
    }

    @Test
    void editRequirement_anyOfToAllOf_preservesIdChildrenAndOrder() {
        Requirement firstChild = new ModuleRequirement(
                "first-child", "First child", "", Set.of("CS1231S"));
        Requirement secondChild = new ModuleRequirement(
                "second-child", "Second child", "", Set.of("CS2040S"));
        AnyOfRequirement existing = new AnyOfRequirement(
                "parent", "Parent", "old", List.of(firstChild, secondChild));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        AllOfRequirement edited = new AllOfRequirement(
                "parent", "Updated parent", "new", List.of());

        Requirement converted = manager.editRequirement("parent", edited);

        assertEquals(AllOfRequirement.class, converted.getClass());
        assertEquals("parent", converted.getId());
        assertEquals(List.of(firstChild, secondChild), converted.getChildren());
    }

    @Test
    void editRequirement_nestedComposite_replacesChildInParent() {
        AllOfRequirement nested = new AllOfRequirement(
                "nested", "Nested", "", List.of());
        AllOfRequirement root = new AllOfRequirement(
                "root", "Root", "", List.of(nested));
        RequirementsManager manager = new RequirementsManager(List.of(root));
        AnyOfRequirement edited = new AnyOfRequirement(
                "nested", "Updated nested", "", List.of());

        Requirement converted = manager.editRequirement("nested", edited);

        assertSame(converted, root.getChildren().get(0));
        assertEquals(AnyOfRequirement.class, converted.getClass());
    }

    @Test
    void editRequirement_leafToCompositeConversion_rejectsEdit() {
        Requirement existing = new ModuleRequirement(
                "requirement", "Requirement", "old", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        Requirement edited = new AllOfRequirement(
                "requirement", "Updated", "new", List.of());

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", edited));
    }

    @Test
    void editRequirement_compositeToLeafConversion_rejectsEdit() {
        Requirement existing = new AllOfRequirement(
                "requirement", "Requirement", "old", List.of());
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        Requirement edited = new ModuleRequirement(
                "requirement", "Updated", "new", Set.of("CS1231S"));

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", edited));
    }

    @Test
    void editRequirement_differentLeafTypeConversion_rejectsEdit() {
        Requirement existing = new ModuleRequirement(
                "requirement", "Requirement", "old", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        Requirement edited = new UnitCountRequirement(
                "requirement", "Updated", "new", ModuleSelector.allModules(), 4);

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", edited));
    }

    @Test
    void editRequirement_differentId_rejectsEdit() {
        Requirement existing = new ModuleRequirement(
                "requirement", "Requirement", "old", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        Requirement edited = new ModuleRequirement(
                "different", "Updated", "new", Set.of("CS2040S"));

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", edited));
    }

    @Test
    void editRequirement_nullEditedRequirement_rejectsEdit() {
        RequirementsManager manager = new RequirementsManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", null));
    }

    @Test
    void editRequirement_unknownOrBlankId_rejectsEdit() {
        RequirementsManager manager = new RequirementsManager();
        Requirement edited = new ModuleRequirement(
                "requirement", "Requirement", "", Set.of("CS1231S"));

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("unknown", edited));
        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("", edited));
    }

    @Test
    void editRequirement_rejectedEdit_doesNotMutateExistingRequirement() {
        ModuleRequirement existing = new ModuleRequirement(
                "requirement", "Requirement", "old", Set.of("CS1231S"));
        RequirementsManager manager = new RequirementsManager(List.of(existing));
        Requirement edited = new UnitCountRequirement(
                "requirement", "Updated", "new", ModuleSelector.allModules(), 4);

        assertThrows(IllegalArgumentException.class,
                () -> manager.editRequirement("requirement", edited));
        assertEquals("Requirement", existing.getName());
        assertEquals("old", existing.getDescription());
        assertEquals(Set.of("CS1231S"), existing.getModuleCodes());
    }
}
