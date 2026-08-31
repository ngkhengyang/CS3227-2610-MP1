package degreeprogress.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import degreeprogress.models.modules.ModuleDocument;
import degreeprogress.models.requirements.RequirementDocument;

/** Loads and saves the complete application state in one JSON file. */
public final class StorageManager {
    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final String DATA_FILE_NAME = "application-data.json";
    private static final String DEFAULT_MODULES_RESOURCE = "/default-modules.json";
    private static final String DEFAULT_REQUIREMENTS_RESOURCE = "/default-requirements.json";

    private final Path dataFile;
    private final ObjectMapper objectMapper;
    private final ModuleSerializer moduleSerializer;
    private final RequirementsSerializer requirementsSerializer;

    /** Creates a storage manager using the file beside the packaged application. */
    public StorageManager() {
        this(resolveDefaultDataFile());
    }

    /** Creates a storage manager using the supplied application data file. */
    public StorageManager(Path dataFile) {
        if (dataFile == null) {
            throw new IllegalArgumentException("Data file must not be null");
        }
        this.dataFile = dataFile.toAbsolutePath().normalize();
        objectMapper = new ObjectMapper();
        moduleSerializer = new ModuleSerializer();
        requirementsSerializer = new RequirementsSerializer();
    }

    /** Returns the file used for application data. */
    public Path getDataFile() {
        return dataFile;
    }

    /**
     * Loads application data or bundled default modules and requirements on first launch.
     *
     * <p>If the existing data file is malformed or invalid, bundled defaults are returned rather
     * than allowing the load failure to abort the application.</p>
     *
     * @return the loaded application data or bundled defaults
     */
    public ApplicationData load() {
        return loadWithStatus().applicationData();
    }

    /**
     * Loads application data and reports whether corrupted data caused a fallback to defaults.
     *
     * @return the loaded data and corruption status
     */
    public StorageLoadResult loadWithStatus() {
        if (!Files.exists(dataFile)) {
            return new StorageLoadResult(createDefaultApplicationData(), false);
        }

        String json;
        try {
            json = Files.readString(dataFile, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new StorageException("Could not read application data from " + dataFile, exception);
        }

        try {
            return new StorageLoadResult(readApplicationData(objectMapper.readTree(json)), false);
        } catch (IOException | IllegalArgumentException exception) {
            return new StorageLoadResult(createDefaultApplicationData(), true);
        }
    }

    /** Saves application data to the configured file. */
    public void save(ApplicationData applicationData) {
        if (applicationData == null) {
            throw new IllegalArgumentException("Application data must not be null");
        }
        validateSupportedSchemaVersion(applicationData.schemaVersion());
        validateSupportedSchemaVersion(applicationData.modules().schemaVersion());
        validateSupportedSchemaVersion(applicationData.requirements().schemaVersion());

        Path temporaryFile = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    temporaryFile,
                    serializeApplicationData(applicationData),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            moveIntoPlace(temporaryFile);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Could not save application data to " + dataFile, exception);
        }
    }

    private static Path resolveDefaultDataFile() {
        try {
            CodeSource source = StorageManager.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                throw new StorageException("Could not resolve the application code source");
            }
            Path codeSource = Path.of(source.getLocation().toURI());
            Path applicationDirectory = Files.isDirectory(codeSource)
                    ? codeSource
                    : codeSource.getParent();
            if (applicationDirectory == null) {
                throw new StorageException("Could not resolve the application directory");
            }
            return applicationDirectory.resolve(DATA_FILE_NAME);
        } catch (URISyntaxException | SecurityException exception) {
            throw new StorageException("Could not resolve the application data file", exception);
        }
    }

    private ApplicationData createDefaultApplicationData() {
        try (InputStream modulesInput = StorageManager.class
                .getResourceAsStream(DEFAULT_MODULES_RESOURCE);
                InputStream requirementsInput = StorageManager.class
                        .getResourceAsStream(DEFAULT_REQUIREMENTS_RESOURCE)) {
            if (modulesInput == null || requirementsInput == null) {
                throw new StorageException("Bundled default application data was not found");
            }
            ModuleDocument modules = moduleSerializer.parse(
                    new String(modulesInput.readAllBytes(), StandardCharsets.UTF_8));
            RequirementDocument requirements = requirementsSerializer.parse(
                    new String(requirementsInput.readAllBytes(), StandardCharsets.UTF_8));
            validateSupportedSchemaVersion(modules.schemaVersion());
            validateSupportedSchemaVersion(requirements.schemaVersion());
            return new ApplicationData(SUPPORTED_SCHEMA_VERSION, modules, requirements);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Could not load bundled default application data", exception);
        }
    }

    private String serializeApplicationData(ApplicationData applicationData) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("schemaVersion", applicationData.schemaVersion());
            root.set("modules", objectMapper.readTree(
                    moduleSerializer.serialize(applicationData.modules())));
            root.set("requirements", objectMapper.readTree(
                    requirementsSerializer.serialize(applicationData.requirements())));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Could not serialize application data", exception);
        }
    }

    private ApplicationData readApplicationData(JsonNode root) throws JsonProcessingException {
        requireObject(root, "Application data must be a JSON object");
        int schemaVersion = requiredInt(root, "schemaVersion");
        validateSupportedSchemaVersion(schemaVersion);

        ModuleDocument modules = moduleSerializer.parse(
                objectMapper.writeValueAsString(requiredObject(root, "modules")));
        RequirementDocument requirements = requirementsSerializer.parse(
                objectMapper.writeValueAsString(requiredObject(root, "requirements")));
        validateSupportedSchemaVersion(modules.schemaVersion());
        validateSupportedSchemaVersion(requirements.schemaVersion());
        return new ApplicationData(schemaVersion, modules, requirements);
    }

    private void moveIntoPlace(Path temporaryFile) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    dataFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    temporaryFile,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private int requiredInt(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isIntegralNumber()) {
            throw new IllegalArgumentException("Missing integer field: " + field);
        }
        return value.intValue();
    }

    private JsonNode requiredObject(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        requireObject(value, "Field '" + field + "' must be an object");
        return value;
    }

    private void requireObject(JsonNode node, String message) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateSupportedSchemaVersion(int schemaVersion) {
        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported application data schema version: "
                    + schemaVersion);
        }
    }
}
