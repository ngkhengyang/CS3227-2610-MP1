package degreeprogress.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;
import degreeprogress.models.modules.ModuleDocument;

/** Converts module documents to and from the application's JSON format. */
public final class ModuleSerializer {
    private final ObjectMapper objectMapper;

    /** Creates a module JSON converter. */
    public ModuleSerializer() {
        objectMapper = new ObjectMapper();
    }

    /** Serializes a complete module document as indented JSON. */
    public String serialize(ModuleDocument document) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(writeDocument(document));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize module document", exception);
        }
    }

    /** Parses a complete module document from JSON. */
    public ModuleDocument parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Module JSON must not be blank");
        }
        try {
            return readDocument(objectMapper.readTree(json));
        } catch (IOException | IllegalArgumentException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException("Could not parse module JSON", exception);
        }
    }

    private ObjectNode writeDocument(ModuleDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Module document must not be null");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("schemaVersion", document.schemaVersion());
        ArrayNode modules = root.putArray("modules");
        document.modules().forEach(module -> modules.add(writeModule(module)));
        return root;
    }

    private ObjectNode writeModule(Module module) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("code", module.getCode());
        node.put("name", module.getName());
        node.put("units", module.getUnits());
        node.put("completed", module.isCompleted());
        return node;
    }

    private ModuleDocument readDocument(JsonNode root) {
        requireObject(root, "Document must be a JSON object");

        List<Module> modules = new ArrayList<>();
        for (JsonNode module : requiredArray(root, "modules")) {
            modules.add(readModule(module));
        }
        return new ModuleDocument(requiredInt(root, "schemaVersion"), modules);
    }

    private Module readModule(JsonNode node) {
        requireObject(node, "Module must be a JSON object");
        return new Module(
                new ModuleCode(requiredText(node, "code")),
                requiredText(node, "name"),
                requiredInt(node, "units"),
                requiredBoolean(node, "completed"));
    }

    private String requiredText(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing non-blank text field: " + field);
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

    private boolean requiredBoolean(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isBoolean()) {
            throw new IllegalArgumentException("Missing boolean field: " + field);
        }
        return value.booleanValue();
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
