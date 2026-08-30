package degreeprogress.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;
import degreeprogress.models.modules.ModuleDocument;

class ModuleSerializerTest {
    private final ModuleSerializer serializer = new ModuleSerializer();

    @Test
    void serialize_writesModuleFields() {
        ModuleDocument document = new ModuleDocument(
                1,
                List.of(new Module(
                        new ModuleCode("CS2040S"), "Data Structures and Algorithms", 4, true)));

        String serialized = serializer.serialize(document);

        assertTrue(serialized.contains("\"schemaVersion\" : 1"));
        assertTrue(serialized.contains("\"code\" : \"CS2040S\""));
        assertTrue(serialized.contains("\"name\" : \"Data Structures and Algorithms\""));
        assertTrue(serialized.contains("\"units\" : 4"));
        assertTrue(serialized.contains("\"completed\" : true"));
    }

    @Test
    void parse_readsModuleFields() {
        String json = """
                {
                  "schemaVersion": 1,
                  "modules": [{
                    "code": "CS2040S",
                    "name": "Data Structures and Algorithms",
                    "units": 4,
                    "completed": true
                  }]
                }
                """;

        Module parsed = serializer.parse(json).modules().get(0);

        assertEquals("CS2040S", parsed.getCode());
        assertEquals("Data Structures and Algorithms", parsed.getName());
        assertEquals(4, parsed.getUnits());
        assertTrue(parsed.isCompleted());
    }

    @Test
    void parse_rejectsMalformedOrIncompleteModuleJson() {
        assertThrows(IllegalArgumentException.class, () -> serializer.parse("not json"));

        String missingCompleted = """
                {
                  "schemaVersion": 1,
                  "modules": [{"code":"CS2040S", "name":"Algorithms", "units":4}]
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> serializer.parse(missingCompleted));
    }

    @Test
    void serializeAndParse_preservesIncompleteModule() {
        Module module = new Module(new ModuleCode("CP3880"), "Internship", 12, false);
        Module parsed = serializer.parse(serializer.serialize(
                new ModuleDocument(1, List.of(module)))).modules().get(0);

        assertFalse(parsed.isCompleted());
        assertEquals(module.getModuleCode(), parsed.getModuleCode());
    }

    @Test
    void serializeAndParse_preservesModuleDocument() {
        Module original = new Module(
                new ModuleCode("CS2103T"), "Software Engineering", 4, true);
        ModuleDocument document = new ModuleDocument(1, List.of(original));

        Module parsed = serializer.parse(serializer.serialize(document)).modules().get(0);

        assertEquals(original.getCode(), parsed.getCode());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getUnits(), parsed.getUnits());
        assertEquals(original.isCompleted(), parsed.isCompleted());
    }
}
