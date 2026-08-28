package degreeprogress.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import degreeprogress.models.requirements.AllOfRequirement;
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
    void getRequirements_returnsUnmodifiableSnapshot() {
        RequirementsManager manager = new RequirementsManager();
        manager.addRequirement(new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S")));

        assertThrows(UnsupportedOperationException.class,
                () -> manager.getRequirements().clear());
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
    void addChildRequirement_rejectsInvalidParentOrChild() {
        Requirement leaf = new ModuleRequirement(
                "foundation", "Foundation", "", Set.of("CS1231S"));
        RequirementsManager leafManager = new RequirementsManager(List.of(leaf));

        assertThrows(IllegalArgumentException.class,
                () -> leafManager.addChildRequirement("foundation", new ModuleRequirement(
                        "child", "Child", "", Set.of("CS2040S"))));
        assertThrows(IllegalArgumentException.class,
                () -> leafManager.addChildRequirement("missing", new ModuleRequirement(
                        "child", "Child", "", Set.of("CS2040S"))));
        assertThrows(IllegalArgumentException.class,
                () -> leafManager.addChildRequirement("foundation", null));
        assertThrows(IllegalArgumentException.class,
                () -> leafManager.addChildRequirement("", new ModuleRequirement(
                        "child", "Child", "", Set.of("CS2040S"))));
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
    void deleteRequirement_rejectsInvalidTarget() {
        Requirement leaf = new ModuleRequirement(
                "leaf", "Leaf", "", Set.of("CS1231S"));
        AllOfRequirement parent = new AllOfRequirement(
                "parent", "Parent", "", List.of(leaf));
        RequirementsManager manager = new RequirementsManager(List.of(parent));

        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement(null));
        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement(""));
        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteRequirement("missing"));
        assertEquals(List.of(leaf), parent.getChildren());
    }
}
