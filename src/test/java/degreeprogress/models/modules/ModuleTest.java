package degreeprogress.models.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleTest {
    @Test
    void moduleCodeNormalisesAndDerivesPrefixAndLevel() {
        Module module = new Module(" cp3880 ", 4, false);

        assertEquals("CP3880", module.getCode());
        assertEquals("CP", module.getPrefix());
        assertEquals(3000, module.getLevel());
        assertTrue(module.hasPrefix("cp"));
        assertFalse(module.hasPrefix("CS"));
    }

    @Test
    void moduleCodePreservesSuffixes() {
        ModuleCode code = new ModuleCode("cs2040s");

        assertEquals("CS2040S", code.value());
        assertEquals("CS", code.getPrefix());
        assertEquals(2000, code.getLevel());
    }

    @Test
    void explicitLevelMustAgreeWithDerivedLevel() {
        Module module = new Module("CS2040", 4, 2000, true);

        assertEquals(2000, module.getLevel());
        assertThrows(IllegalArgumentException.class,
                () -> new Module("CS2040", 4, 3000, true));
    }

    @Test
    void invalidModuleCodesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode("2040"));
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode("CS"));
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode("C-2040"));
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode("   "));
        assertThrows(IllegalArgumentException.class, () -> new ModuleCode("CS2040").startsWith(" "));
    }

    @Test
    void moduleRequiresANonBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Module("CS2040", "", 4));
        assertThrows(IllegalArgumentException.class, () -> new Module("CS2040", "   ", 4));
    }

    @Test
    void moduleUnitsIncludeBothValidBoundaries() {
        assertEquals(1, new Module("CS1010", "Introduction to Computing", 1).getUnits());
        assertEquals(60, new Module("CS6000", "Advanced Computing", 60).getUnits());
    }

    @Test
    void moduleRejectsUnitsOutsideTheValidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new Module("CS1010", "Introduction to Computing", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Module("CS1010", "Introduction to Computing", -1));
        assertThrows(IllegalArgumentException.class,
                () -> new Module("CS1010", "Introduction to Computing", 61));
    }
}
