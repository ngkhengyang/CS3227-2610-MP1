package degreeprogress.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import degreeprogress.requirements.AllOfRequirement;
import degreeprogress.requirements.AnyOfRequirement;
import degreeprogress.requirements.ModuleCountRequirement;
import degreeprogress.requirements.ModuleRequirement;
import degreeprogress.requirements.ModuleSelector;
import degreeprogress.requirements.Requirement;
import degreeprogress.requirements.UnitCountRequirement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Converts the requirement domain model to and from the application's JSON
 * requirement format. This component deliberately has no file-system logic.
 */
public final class RequirementStorage {
    private final ObjectMapper objectMapper;

    public RequirementStorage() {
        objectMapper = new ObjectMapper();
    }

    /** Serialises a complete requirement document as indented JSON. */
    public String serialize(RequirementDocument document) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(writeDocument(document));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialise requirement document", exception);
        }
    }

    /** Parses a complete requirement document from JSON. */
    public RequirementDocument parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Requirement JSON must not be blank");
        }
        try {
            return readDocument(objectMapper.readTree(json));
        } catch (IOException | IllegalArgumentException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException("Could not parse requirement JSON", exception);
        }
    }

    private ObjectNode writeDocument(RequirementDocument document) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", document.schemaVersion());

        ObjectNode programme = root.putObject("programme");
        ProgrammeInfo programmeInfo = document.programme();
        programme.put("id", programmeInfo.id());
        programme.put("name", programmeInfo.name());
        programme.put("cohort", programmeInfo.cohort());
        programme.put("totalUnits", programmeInfo.totalUnits());
        writeStrings(programme.putArray("focusAreas"), programmeInfo.focusAreas());

        writeStrings(root.putArray("sources"), document.sources());
        ArrayNode requirements = root.putArray("requirements");
        document.requirements().forEach(requirement -> requirements.add(writeRequirement(requirement)));
        return root;
    }

    private ObjectNode writeRequirement(Requirement requirement) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", requirement.getId());
        node.put("name", requirement.getName());
        node.put("description", requirement.getDescription());

        if (requirement instanceof AllOfRequirement allOf) {
            node.put("type", "allOf");
            writeChildren(node, allOf.getChildren());
        } else if (requirement instanceof AnyOfRequirement anyOf) {
            node.put("type", "anyOf");
            writeChildren(node, anyOf.getChildren());
        } else if (requirement instanceof ModuleRequirement module) {
            node.put("type", "module");
            writeSortedStrings(node.putArray("moduleCodes"), module.getModuleCodes());
        } else if (requirement instanceof ModuleCountRequirement moduleCount) {
            node.put("type", "moduleCount");
            writeSelector(node.putObject("selector"), moduleCount.getSelector());
            node.put("minimumModules", moduleCount.getMinimumModules());
            putOptionalInteger(node, "maximumModules", moduleCount.getMaximumModules());
        } else if (requirement instanceof UnitCountRequirement unitCount) {
            node.put("type", "unitCount");
            writeSelector(node.putObject("selector"), unitCount.getSelector());
            node.put("minimumUnits", unitCount.getMinimumUnits());
            putOptionalInteger(node, "maximumUnits", unitCount.getMaximumUnits());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported requirement class: " + requirement.getClass().getName());
        }
        return node;
    }

    private void writeChildren(ObjectNode node, List<Requirement> children) {
        ArrayNode childNodes = node.putArray("children");
        children.forEach(child -> childNodes.add(writeRequirement(child)));
    }

    private void writeSelector(ObjectNode node, ModuleSelector selector) {
        writeSortedStrings(node.putArray("moduleCodes"), selector.getModuleCodes());
        writeSortedStrings(node.putArray("codePrefixes"), selector.getCodePrefixes());
        if (selector.getMinimumLevel() != null) {
            node.put("minimumLevel", selector.getMinimumLevel());
        }
        if (selector.getMaximumLevel() != null) {
            node.put("maximumLevel", selector.getMaximumLevel());
        }
    }

    private void writeStrings(ArrayNode array, Collection<String> values) {
        values.forEach(array::add);
    }

    private void writeSortedStrings(ArrayNode array, Collection<String> values) {
        values.stream().sorted().forEach(array::add);
    }

    private void putOptionalInteger(ObjectNode node, String field, Integer value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private RequirementDocument readDocument(JsonNode root) {
        requireObject(root, "Document must be a JSON object");
        JsonNode programme = requiredObject(root, "programme");
        ProgrammeInfo programmeInfo = new ProgrammeInfo(
                requiredText(programme, "id"),
                requiredText(programme, "name"),
                requiredText(programme, "cohort"),
                requiredInt(programme, "totalUnits"),
                readStrings(programme, "focusAreas"));

        List<Requirement> requirements = new ArrayList<>();
        JsonNode requirementArray = requiredArray(root, "requirements");
        for (JsonNode requirement : requirementArray) {
            requirements.add(readRequirement(requirement));
        }
        return new RequirementDocument(
                requiredInt(root, "schemaVersion"),
                programmeInfo,
                readStrings(root, "sources"),
                requirements);
    }

    private Requirement readRequirement(JsonNode node) {
        requireObject(node, "Requirement must be a JSON object");
        String type = requiredText(node, "type");
        String id = requiredText(node, "id");
        String name = requiredText(node, "name");
        String description = optionalText(node, "description");

        return switch (type) {
            case "module" -> new ModuleRequirement(
                    id, name, description, Set.copyOf(readStrings(node, "moduleCodes")));
            case "moduleCount" -> new ModuleCountRequirement(
                    id,
                    name,
                    description,
                    readSelector(node.get("selector")),
                    requiredInt(node, "minimumModules"),
                    optionalInt(node, "maximumModules"));
            case "unitCount" -> new UnitCountRequirement(
                    id,
                    name,
                    description,
                    readSelector(node.get("selector")),
                    requiredInt(node, "minimumUnits"),
                    optionalInt(node, "maximumUnits"));
            case "allOf" -> new AllOfRequirement(
                    id, name, description, readChildren(node));
            case "anyOf" -> new AnyOfRequirement(
                    id, name, description, readChildren(node));
            default -> throw new IllegalArgumentException("Unsupported requirement type: " + type);
        };
    }

    private List<Requirement> readChildren(JsonNode node) {
        List<Requirement> children = new ArrayList<>();
        for (JsonNode child : requiredArray(node, "children")) {
            children.add(readRequirement(child));
        }
        return children;
    }

    private ModuleSelector readSelector(JsonNode node) {
        if (node == null || node.isNull()) {
            return ModuleSelector.allModules();
        }
        requireObject(node, "Selector must be a JSON object");
        return new ModuleSelector(
                Set.copyOf(readStrings(node, "moduleCodes")),
                Set.copyOf(readStrings(node, "codePrefixes")),
                optionalInt(node, "minimumLevel"),
                optionalInt(node, "maximumLevel"));
    }

    private List<String> readStrings(JsonNode parent, String field) {
        JsonNode array = parent.get(field);
        if (array == null || array.isNull()) {
            return List.of();
        }
        if (!array.isArray()) {
            throw new IllegalArgumentException("Field '" + field + "' must be an array");
        }
        List<String> values = new ArrayList<>();
        for (JsonNode value : array) {
            if (!value.isTextual() || value.asText().isBlank()) {
                throw new IllegalArgumentException(
                        "Field '" + field + "' must contain non-blank strings");
            }
            values.add(value.asText());
        }
        return values;
    }

    private String requiredText(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing non-blank text field: " + field);
        }
        return value.asText();
    }

    private String optionalText(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        if (!value.isTextual()) {
            throw new IllegalArgumentException("Field '" + field + "' must be text");
        }
        return value.asText();
    }

    private int requiredInt(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isIntegralNumber()) {
            throw new IllegalArgumentException("Missing integer field: " + field);
        }
        return value.intValue();
    }

    private Integer optionalInt(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber()) {
            throw new IllegalArgumentException("Field '" + field + "' must be an integer");
        }
        return value.intValue();
    }

    private JsonNode requiredObject(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        requireObject(value, "Field '" + field + "' must be an object");
        return value;
    }

    private JsonNode requiredArray(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isArray()) {
            throw new IllegalArgumentException("Field '" + field + "' must be an array");
        }
        return value;
    }

    private void requireObject(JsonNode node, String message) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException(message);
        }
    }
}
