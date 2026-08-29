package degreeprogress.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ProgrammeInfo;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.RequirementDocument;
import degreeprogress.models.requirements.UnitCountRequirement;

class RequirementStorageTest {
    private final RequirementStorage storage = new RequirementStorage();

    @Test
    void serialize_writesPolymorphicTypes() {
        String serialized = storage.serialize(createPolymorphicDocument());

        assertTrue(serialized.contains("\"type\" : \"allOf\""));
        assertTrue(serialized.contains("\"type\" : \"module\""));
        assertTrue(serialized.contains("\"type\" : \"moduleCount\""));
        assertTrue(serialized.contains("\"type\" : \"unitCount\""));
        assertTrue(serialized.contains("\"type\" : \"anyOf\""));
        assertTrue(serialized.contains("\"minimumModules\" : 2"));
        assertTrue(serialized.contains("\"maximumModules\" : 4"));
        assertTrue(serialized.contains("\"minimumUnits\" : 8"));
        assertTrue(serialized.contains("\"maximumUnits\" : 12"));
    }

    @Test
    void parse_readsBundledDocument() throws IOException {
        String json;
        try (InputStream input = getClass().getResourceAsStream("/default-requirements.json")) {
            if (input == null) {
                throw new IOException("Bundled requirement data was not found");
            }
            json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        RequirementDocument parsed = storage.parse(json);

        assertEquals(1, parsed.schemaVersion());
        assertEquals("bcomp-cs", parsed.programme().id());
        assertEquals(160, parsed.programme().totalUnits());
        assertEquals(1, parsed.requirements().size());
        assertEquals(30, countRequirements(parsed.requirements()));
    }

    @Test
    void parse_readsPolymorphicTypesAndFields() {
        RequirementDocument parsed = storage.parse(createPolymorphicJson());
        Requirement root = parsed.requirements().get(0);

        assertInstanceOf(AllOfRequirement.class, root);
        assertInstanceOf(ModuleRequirement.class, root.getChildren().get(0));
        assertInstanceOf(ModuleCountRequirement.class, root.getChildren().get(1));
        assertEquals(2, ((ModuleCountRequirement) root.getChildren().get(1)).getMinimumModules());
        assertEquals(4, ((ModuleCountRequirement) root.getChildren().get(1)).getMaximumModules());
        assertInstanceOf(UnitCountRequirement.class, root.getChildren().get(2));
        assertInstanceOf(AnyOfRequirement.class, root.getChildren().get(3));
    }

    @Test
    void parse_rejectsMalformedJsonOrUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> storage.parse("not json"));

        String unknownType = """
                {
                  "schemaVersion": 1,
                  "programme": {"id":"p","name":"P","cohort":"C","totalUnits":1,"focusAreas":[]},
                  "sources": [],
                  "requirements": [{"type":"unknown","id":"r","name":"R"}]
                }
                """;
        assertThrows(IllegalArgumentException.class, () -> storage.parse(unknownType));
    }

    private RequirementDocument createPolymorphicDocument() {
        Requirement root = new AllOfRequirement(
                "root",
                "Root",
                "",
                List.of(
                        new ModuleRequirement("module", "Module", "", Set.of("CS1231S")),
                        new ModuleCountRequirement(
                                "module-count", "Module count", "", null, 2, 4),
                        new UnitCountRequirement(
                                "unit-count", "Unit count", "", null, 8, 12),
                        new AnyOfRequirement(
                                "choice",
                                "Choice",
                                "",
                                List.of(new ModuleRequirement(
                                        "alternative", "Alternative", "", Set.of("CS2040S"))))));
        return new RequirementDocument(
                1,
                new ProgrammeInfo("test", "Test Programme", "TEST", 160, List.of("AI")),
                List.of("test-source"),
                List.of(root));
    }

    private String createPolymorphicJson() {
        return """
                {
                  "schemaVersion": 1,
                  "programme": {
                    "id":"test", "name":"Test Programme", "cohort":"TEST", "totalUnits":160,
                    "focusAreas":["AI"]
                  },
                  "sources": ["test-source"],
                  "requirements": [{
                    "type":"allOf", "id":"root", "name":"Root", "description":"",
                    "children":[
                      {"type":"module", "id":"module", "name":"Module", "description":"", "moduleCodes":["CS1231S"]},
                      {"type":"moduleCount", "id":"module-count", "name":"Module count", "description":"",
                       "selector":null, "minimumModules":2, "maximumModules":4},
                      {"type":"unitCount", "id":"unit-count", "name":"Unit count", "description":"",
                       "selector":null, "minimumUnits":8, "maximumUnits":12},
                      {"type":"anyOf", "id":"choice", "name":"Choice", "description":"",
                       "children":[{"type":"module", "id":"alternative", "name":"Alternative",
                                    "description":"", "moduleCodes":["CS2040S"]}]}
                    ]
                  }]
                }
                """;
    }

    private int countRequirements(List<Requirement> requirements) {
        return requirements.stream()
                .mapToInt(requirement -> 1 + countRequirements(requirement.getChildren()))
                .sum();
    }
}
