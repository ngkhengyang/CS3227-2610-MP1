package degreeprogress.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModulesManagerTest {
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
        ModulesManager manager = new ModulesManager();
        manager.addModule("CS2040S", "Data Structures and Algorithms", 4);

        manager.deleteModule("cs2040s");

        assertEquals(0, manager.getModules().size());
    }

    @Test
    void duplicateAndUnknownCodesAreRejected() {
        ModulesManager manager = new ModulesManager();
        manager.addModule("CS2040S", "Data Structures and Algorithms", 4);

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
}
