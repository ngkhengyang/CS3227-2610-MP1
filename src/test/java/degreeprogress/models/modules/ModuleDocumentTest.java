package degreeprogress.models.modules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleDocumentTest {
    @Test
    void findByCode_matchesCaseInsensitively() {
        Module module = new Module("CP3880", 4, false);
        ModuleDocument document = new ModuleDocument(1, List.of(module));

        assertEquals(module, document.findByCode("cp3880").orElseThrow());
        assertTrue(document.containsCode("CP3880"));
        assertFalse(document.containsCode("CS2040"));
    }

    @Test
    void moduleDocumentConstructor_rejectsDuplicateCodes() {
        Module first = new Module("CS2040", 4, false);
        Module duplicate = new Module("cs2040", 4, true);

        assertThrows(IllegalArgumentException.class,
                () -> new ModuleDocument(1, List.of(first, duplicate)));
    }

    @Test
    void moduleDocumentConstructor_copiesModuleList() {
        List<Module> modules = new java.util.ArrayList<>();
        modules.add(new Module("CS2040", 4, false));
        ModuleDocument document = new ModuleDocument(1, modules);

        modules.clear();

        assertEquals(1, document.modules().size());
    }
}
