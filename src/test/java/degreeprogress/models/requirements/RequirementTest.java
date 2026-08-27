package degreeprogress.models.requirements;

import degreeprogress.models.modules.Module;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementTest {
    private static final Module CS1231S = new Module("CS1231S", 4, true);
    private static final Module CS2040S = new Module("CS2040S", 4, true);
    private static final Module CS4248 = new Module("CS4248", 4, 4000, false);

    @Test
    void moduleRequirementRequiresEveryListedModule() {
        Requirement requirement = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S", "CS2040S"));

        EvaluationResult incomplete = requirement.evaluate(List.of(CS1231S));
        EvaluationResult complete = requirement.evaluate(List.of(CS1231S, CS2040S));

        assertFalse(incomplete.fulfilled());
        assertEquals(1, incomplete.achieved());
        assertTrue(complete.fulfilled());
        assertEquals(2, complete.achieved());
    }

    @Test
    void countAndUnitRequirementsApplySelectors() {
        ModuleSelector cs4000 = new ModuleSelector(
                Set.of(), Set.of("CS"), 4000, null);
        Requirement moduleCount = new ModuleCountRequirement(
                "advanced", "Advanced modules", "", cs4000, 1);
        Requirement unitCount = new UnitCountRequirement(
                "breadth", "Breadth", "", ModuleSelector.forCodes("CS1231S", "CS2040S"), 8);

        assertFalse(moduleCount.evaluate(List.of(CS4248)).fulfilled());
        assertTrue(moduleCount.evaluate(List.of(new Module("CS4248", 4, 4000, true))).fulfilled());
        assertTrue(unitCount.evaluate(List.of(CS1231S, CS2040S)).fulfilled());
    }

    @Test
    void allAndAnyRequirementsComposeChildResults() {
        Requirement first = new ModuleRequirement("first", "First", "", Set.of("CS1231S"));
        Requirement second = new ModuleRequirement("second", "Second", "", Set.of("CS2040S"));
        Requirement all = new AllOfRequirement("all", "Both", "", List.of(first, second));
        Requirement any = new AnyOfRequirement("any", "Either", "", List.of(first, second));

        assertFalse(all.evaluate(List.of(CS1231S)).fulfilled());
        assertTrue(any.evaluate(List.of(CS1231S)).fulfilled());
        assertEquals(2, all.evaluate(List.of(CS1231S, CS2040S)).children().size());
    }

    @Test
    void requirementsCanBeEditedAndChildrenCanBeChanged() {
        ModuleRequirement requirement = new ModuleRequirement(
                "elective", "Elective", "old", Set.of("CS1231S"));
        requirement.setName("Updated elective");
        requirement.setDescription("new");
        requirement.setModuleCodes(Set.of("CS2040S"));

        AllOfRequirement group = new AllOfRequirement("group", "Group", "", List.of(requirement));
        group.addChild(new ModuleRequirement("extra", "Extra", "", Set.of("CS1231S")));
        group.removeChild("extra");

        assertEquals("Updated elective", requirement.getName());
        assertTrue(requirement.evaluate(List.of(CS2040S)).fulfilled());
        assertEquals(1, group.getChildren().size());
    }

    @Test
    void invalidRequirementValuesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleRequirement("id", "name", "", Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new UnitCountRequirement("id", "name", "", null, 8, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleSelector(Set.of(), Set.of(), 4000, 3000));
    }
}
