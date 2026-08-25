package degreeprogress.modules;

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
}
