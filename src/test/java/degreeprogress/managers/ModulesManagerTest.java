package degreeprogress.managers;

import degreeprogress.models.modules.Module;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModulesManagerTest {
    @Test
    void presetConstructorRejectsNullAndDuplicateModules() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModulesManager(null));
        assertThrows(IllegalArgumentException.class,
                () -> new ModulesManager(List.of(
                        new Module("CS2040S", "Data Structures", 4),
                        new Module("cs2040s", "Duplicate", 4))));
    }
        
    @Test
    void addModuleStoresRequiredDetailsAndDefaultsToIncomplete() {
        ModulesManager manager = new ModulesManager();

        manager.addModule(" cs2040s ", "Data Structures and Algorithms", 4);

        assertEquals(1, manager.getModules().size());
        assertEquals("CS2040S", manager.getModules().get(0).getCode());
        assertEquals("Data Structures and Algorithms", manager.getModules().get(0).getName());
        assertEquals(4, manager.getModules().get(0).getUnits());
        assertFalse(manager.getModules().get(0).isCompleted());
    }

    @Test
    void deleteModuleRemovesModuleByCode() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures and Algorithms", 4)));

        manager.deleteModule("cs2040s");

        assertEquals(0, manager.getModules().size());
    }

    @Test
    void duplicateAndUnknownCodesAreRejected() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures and Algorithms", 4),
                new Module("CS2100", "Computer Organisation", 4)));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("cs2040s", "Duplicate", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.deleteModule("CS1231S"));
    }

    @Test
    void addModuleRejectsBlankRequiredFields() {
        ModulesManager manager = new ModulesManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("", "Introduction to Computing", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("CS1010", "", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("CS1010", "   ", 4));
    }

    @Test
    void addModuleRejectsUnitsOutsideOneToSixty() {
        ModulesManager manager = new ModulesManager();

        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("CS1010", "Introduction to Computing", 0));
        assertThrows(IllegalArgumentException.class,
                () -> manager.addModule("CS1010", "Introduction to Computing", 61));
    }

    @Test
    void addModuleAcceptsOneAndSixtyUnits() {
        ModulesManager manager = new ModulesManager();

        manager.addModule("CS1000", "One Unit Module", 1);
        manager.addModule("CS6000", "Sixty Unit Module", 60);

        assertEquals(1, manager.getModules().get(0).getUnits());
        assertEquals(60, manager.getModules().get(1).getUnits());
    }

    @Test
    void editModuleUpdatesDetailsAndPreservesCompletionState() {
        Module completedModule = new Module("CS2040S", "Data Structures", 4);
        completedModule.setCompleted(true);
        ModulesManager manager = new ModulesManager(List.of(completedModule));

        Module edited = manager.editModule(
                "cs2040s", "Computer Systems", 8);

        assertEquals("CS2040S", edited.getCode());
        assertEquals("Computer Systems", edited.getName());
        assertEquals(8, edited.getUnits());
        assertTrue(edited.isCompleted());
    }

    @Test
    void editModuleCanChangeCodeAndNormalisesIt() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures", 4)));

        manager.editModule("CS2040S", " cp2106 ", "Software Engineering", 1);

        assertEquals("CP2106", manager.getModules().get(0).getCode());
        assertEquals("Software Engineering", manager.getModules().get(0).getName());
        assertEquals(1, manager.getModules().get(0).getUnits());
    }

    @Test
    void editModuleRejectsUnknownOrDuplicateCodesAndInvalidDetails() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures", 4),
                new Module("CS2100", "Computer Organisation", 4)));

        assertThrows(IllegalArgumentException.class,
                () -> manager.editModule("CS1231S", "CS1231S", "Renamed", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.editModule("CS2040S", "CS2100", "Duplicate code", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.editModule("CS2040S", "", 4));
        assertThrows(IllegalArgumentException.class,
                () -> manager.editModule("CS2040S", "Renamed", 0));
        assertThrows(IllegalArgumentException.class,
                () -> manager.editModule("CS2040S", "Renamed", 61));
    }

    @Test
    void markModuleCompleted() {
        Module first = new Module("CS1010", "Introduction to Computing", 4);
        Module second = new Module("CS2040S", "Data Structures", 4);
        Module third = new Module("CS2100", "Computer Organisation", 4);
        ModulesManager manager = new ModulesManager(List.of(first, second, third));

        Module marked = manager.markModuleCompleted("cs2040s");
        List<Module> modules = manager.getModules();

        assertTrue(marked.isCompleted());
        assertSame(second, marked);
        // Should not change the order of modules in the list
        assertSame(first, modules.get(0));
        assertSame(second, modules.get(1));
        assertSame(third, modules.get(2));
    }

    @Test
    void markModuleCompletedRejectsUnknownCodes() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures", 4)));

        assertThrows(IllegalArgumentException.class,
                () -> manager.markModuleCompleted("CS1231S"));
    }

    @Test
    void markModuleUncompleted() {
        Module first = new Module("CS1010", "Introduction to Computing", 4);
        Module second = new Module("CS2040S", "Data Structures", 4);
        Module third = new Module("CS2100", "Computer Organisation", 4);
        first.setCompleted(true);
        second.setCompleted(true);
        third.setCompleted(true);
        ModulesManager manager = new ModulesManager(List.of(first, second, third));

        Module marked = manager.markModuleUncompleted("cs2040s");
        List<Module> modules = manager.getModules();

        assertFalse(marked.isCompleted());
        assertSame(second, marked);
        // Should not change the order of modules in the list
        assertSame(first, modules.get(0));
        assertSame(second, modules.get(1));
        assertSame(third, modules.get(2));
    }

    @Test
    void markModuleUncompletedRejectsUnknownCodes() {
        ModulesManager manager = new ModulesManager(List.of(
                new Module("CS2040S", "Data Structures", 4)));

        assertThrows(IllegalArgumentException.class,
                () -> manager.markModuleUncompleted("CS1231S"));
    }
}
