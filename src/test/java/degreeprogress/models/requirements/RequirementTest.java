package degreeprogress.models.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import degreeprogress.models.modules.Module;

class RequirementTest {
    private static final Module CS1231S = new Module("CS1231S", 4, true);
    private static final Module CS2040S = new Module("CS2040S", 4, true);
    private static final Module incompleteCS2040S = new Module("CS2040S", 4, false);
    private static final Module CS3227 = new Module("CS3227", 4, true);
    private static final Module CS4248 = new Module("CS4248", 4, 4000, false);

    @Test
    void moduleRequirementConstructor_rejectsEmptyCodes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleRequirement("id", "name", "", Set.of()));
    }

    @Test
    void moduleRequirementConstructor_rejectsInvalidCodes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleRequirement("id", "name", "", Set.of("CS-2040")));
    }

    @Test
    void unitCountConstructor_rejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new UnitCountRequirement("id", "name", "", null, 8, 4));
    }

    @Test
    void moduleSelectorConstructor_rejectsReversedLevels() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleSelector(Set.of(), Set.of(), 4000, 3000));
    }

    @Test
    void moduleSelectorConstructor_rejectsInvalidCodesAndPrefixes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleSelector(Set.of("CS2040-"), Set.of(), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleSelector(Set.of(), Set.of("CS1"), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleSelector(Set.of(), Set.of("C-S"), null, null));
    }

    @Test
    void evaluate_completedModules() {
        Requirement requirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"));

        EvaluationResult complete = requirement.evaluate(List.of(CS1231S, CS2040S));

        assertTrue(complete.fulfilled());
        assertEquals(2, complete.achieved());
    }

    @Test
    void evaluate_incompleteModules() {
        Requirement requirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"));

        EvaluationResult incompleteModule = requirement.evaluate(List.of(CS1231S, incompleteCS2040S));

        assertFalse(incompleteModule.fulfilled());
        assertEquals(1, incompleteModule.achieved());
    }

    @Test
    void evaluate_missingModules() {
        Requirement requirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"));

        EvaluationResult missingModule = requirement.evaluate(List.of(CS1231S));

        assertFalse(missingModule.fulfilled());
        assertEquals(1, missingModule.achieved());
    }

    @Test
    void evaluate_appliesModuleCountSelector() {
        ModuleSelector selector = new ModuleSelector(
                Set.of(), Set.of("CS"), 4000, null);
        Requirement requirement = new ModuleCountRequirement(
                "advanced", "Advanced modules", "", selector, 1);

        assertFalse(requirement.evaluate(List.of(CS3227, CS4248)).fulfilled());
        assertTrue(requirement.evaluate(List.of(
                CS3227, new Module("CS4248", 4, 4000, true))).fulfilled());
    }

    @Test
    void evaluate_moduleCountMaximumCapsCreditedModules() {
        Requirement requirement = new ModuleCountRequirement(
                "cd", "CD courses", "", ModuleSelector.forCodes("DAO2703", "MNO1706X"), 0, 1);

        EvaluationResult result = requirement.evaluate(List.of(
                new Module("DAO2703", 4, true),
                new Module("MNO1706X", 4, true)));

        assertTrue(result.fulfilled());
        assertEquals(1, result.achieved());
    }

    @Test
    void evaluate_appliesUnitCountSelector() {
        Requirement requirement = new UnitCountRequirement(
                "breadth", "Breadth", "",
                ModuleSelector.forCodes("CS1231S", "CS2040S"), 8);

        assertFalse(requirement.evaluate(List.of(CS1231S, incompleteCS2040S, CS3227)).fulfilled());
        assertTrue(requirement.evaluate(List.of(CS1231S, CS2040S, CS3227)).fulfilled());
    }

    @Test
    void evaluate_unitCountMaximumCapsCreditedUnits() {
        Requirement requirement = new UnitCountRequirement(
                "id-cd", "ID/CD units", "", ModuleSelector.forCodes("CS1231S", "CS2040S"), 4, 4);

        EvaluationResult result = requirement.evaluate(List.of(CS1231S, CS2040S));

        assertTrue(result.fulfilled());
        assertEquals(4, result.achieved());
    }

    @Test
    void evaluate_allOfRequiresEveryChild() {
        Requirement first = new ModuleRequirement("first", "First", "", Set.of("CS1231S"));
        Requirement second = new ModuleRequirement("second", "Second", "", Set.of("CS2040S"));
        Requirement all = new AllOfRequirement("all", "Both", "", List.of(first, second));

        assertFalse(all.evaluate(List.of(CS1231S)).fulfilled());
        assertFalse(all.evaluate(List.of(CS1231S, incompleteCS2040S)).fulfilled());
        assertTrue(all.evaluate(List.of(CS1231S, CS2040S)).fulfilled());
    }

    @Test
    void evaluate_anyOfRequiresOneChild() {
        Requirement first = new ModuleRequirement("first", "First", "", Set.of("CS1231S"));
        Requirement second = new ModuleRequirement("second", "Second", "", Set.of("CS2040S"));
        Requirement any = new AnyOfRequirement("any", "Either", "", List.of(first, second));

        assertTrue(any.evaluate(List.of(CS1231S, incompleteCS2040S)).fulfilled());
        assertFalse(any.evaluate(List.of(incompleteCS2040S)).fulfilled());
    }

    @Test
    void setName_updatesName() {
        ModuleRequirement requirement = new ModuleRequirement(
                "elective", "Elective", "", Set.of("CS1231S"));

        requirement.setName("Updated elective");

        assertEquals("Updated elective", requirement.getName());
    }

    @Test
    void setDescription_updatesDescription() {
        ModuleRequirement requirement = new ModuleRequirement(
                "elective", "Elective", "old", Set.of("CS1231S"));

        requirement.setDescription("new");

        assertEquals("new", requirement.getDescription());
    }

    @Test
    void setModuleCodes_updatesEvaluation() {
        ModuleRequirement requirement = new ModuleRequirement(
                "elective", "Elective", "", Set.of("CS1231S"));

        requirement.setModuleCodes(Set.of("CS2040S"));

        assertTrue(requirement.evaluate(List.of(CS2040S)).fulfilled());
    }

    @Test
    void addChild_addsChild() {
        AllOfRequirement group = new AllOfRequirement("group", "Group", "", List.of());
        Requirement child = new ModuleRequirement(
                "extra", "Extra", "", Set.of("CS1231S"));

        group.addChild(child);

        assertEquals(List.of(child), group.getChildren());
    }

    @Test
    void removeChild_removesChild() {
        Requirement child = new ModuleRequirement(
                "extra", "Extra", "", Set.of("CS1231S"));
        AllOfRequirement group = new AllOfRequirement("group", "Group", "", List.of(child));

        group.removeChild("extra");

        assertTrue(group.getChildren().isEmpty());
    }
}
