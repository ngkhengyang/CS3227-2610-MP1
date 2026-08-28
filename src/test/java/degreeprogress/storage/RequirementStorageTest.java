package degreeprogress.storage;

import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ProgrammeInfo;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.RequirementDocument;
import degreeprogress.models.requirements.UnitCountRequirement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequirementStorageTest {
    private final RequirementStorage storage = new RequirementStorage();

    @Test
    void parseAndSerialize_roundTripsBundledDocument() throws IOException {
        String json;
        try (InputStream input = getClass().getResourceAsStream("/default-requirements.json")) {
            if (input == null) {
                throw new IOException("Bundled requirement data was not found");
            }
            json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        RequirementDocument parsed = storage.parse(json);
        String serialised = storage.serialize(parsed);
        RequirementDocument reparsed = storage.parse(serialised);

        assertEquals(1, parsed.schemaVersion());
        assertEquals("bcomp-cs", parsed.programme().id());
        assertEquals(160, parsed.programme().totalUnits());
        assertEquals(1, parsed.requirements().size());
        assertEquals(30, countRequirements(parsed.requirements()));
        assertEquals(parsed.schemaVersion(), reparsed.schemaVersion());
        assertEquals(parsed.sources(), reparsed.sources());
        assertEquals(countRequirements(parsed.requirements()), countRequirements(reparsed.requirements()));
    }

    @Test
    void serializeAndParse_preservesTypesAndFields() {
        Requirement leaves = new AllOfRequirement(
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
        RequirementDocument document = new RequirementDocument(
                1,
                new ProgrammeInfo("test", "Test Programme", "TEST", 160, List.of("AI")),
                List.of("test-source"),
                List.of(leaves));

        RequirementDocument parsed = storage.parse(storage.serialize(document));
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

    private int countRequirements(List<Requirement> requirements) {
        return requirements.stream()
                .mapToInt(requirement -> 1 + countRequirements(requirement.getChildren()))
                .sum();
    }
}
