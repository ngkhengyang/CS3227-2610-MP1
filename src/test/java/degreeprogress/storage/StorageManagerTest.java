package degreeprogress.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import degreeprogress.models.modules.Module;
import degreeprogress.models.modules.ModuleCode;
import degreeprogress.models.modules.ModuleDocument;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ProgrammeInfo;
import degreeprogress.models.requirements.RequirementDocument;

class StorageManagerTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void saveAndLoad_roundTripsApplicationDataInOneFile() throws Exception {
        Path dataFile = temporaryDirectory.resolve("nested/application-data.json");
        StorageManager storageManager = new StorageManager(dataFile);
        ApplicationData applicationData = createApplicationData();

        storageManager.save(applicationData);

        assertTrue(Files.exists(dataFile));
        String serialized = Files.readString(dataFile);
        assertTrue(serialized.contains("\"modules\""));
        assertTrue(serialized.contains("\"requirements\""));

        ApplicationData loaded = storageManager.load();

        assertEquals(applicationData.schemaVersion(), loaded.schemaVersion());
        assertEquals("CS2040S", loaded.modules().modules().get(0).getCode());
        assertTrue(loaded.modules().modules().get(0).isCompleted());
        assertEquals("foundation", loaded.requirements().requirements().get(0).getId());
    }

    @Test
    void load_whenDataFileIsMissing_returnsDefaultRequirementsAndEmptyModules() {
        StorageManager storageManager = new StorageManager(
                temporaryDirectory.resolve("missing/application-data.json"));

        ApplicationData loaded = storageManager.load();

        assertTrue(loaded.modules().modules().isEmpty());
        assertEquals("bcomp-cs", loaded.requirements().programme().id());
        assertFalse(Files.exists(storageManager.getDataFile()));
    }

    @Test
    void load_whenDataFileIsCorrupted_reportsStorageError() throws Exception {
        Path dataFile = temporaryDirectory.resolve("application-data.json");
        Files.writeString(dataFile, "not json");
        StorageManager storageManager = new StorageManager(dataFile);

        assertThrows(StorageException.class, storageManager::load);
    }

    @Test
    void load_whenSchemaVersionIsUnsupported_reportsStorageError() throws Exception {
        Path dataFile = temporaryDirectory.resolve("application-data.json");
        Files.writeString(dataFile, """
                {
                  "schemaVersion": 2,
                  "modules": {"schemaVersion": 1, "modules": []},
                  "requirements": {"schemaVersion": 1, "programme": {},
                                    "sources": [], "requirements": []}
                }
                """);
        StorageManager storageManager = new StorageManager(dataFile);

        assertThrows(StorageException.class, storageManager::load);
    }

    @Test
    void save_whenSchemaVersionIsUnsupported_rejectsApplicationData() {
        StorageManager storageManager = new StorageManager(
                temporaryDirectory.resolve("application-data.json"));
        ApplicationData applicationData = new ApplicationData(
                2,
                new ModuleDocument(1, List.of()),
                createApplicationData().requirements());

        assertThrows(IllegalArgumentException.class, () -> storageManager.save(applicationData));
    }

    @Test
    void defaultConstructor_usesDefaultApplicationDataFileName() {
        StorageManager storageManager = new StorageManager();

        assertEquals("application-data.json", storageManager.getDataFile().getFileName().toString());
    }

    @Test
    void pathConstructor_usesSuppliedApplicationDataFile() {
        Path dataFile = temporaryDirectory.resolve("custom/application-data.json");

        StorageManager storageManager = new StorageManager(dataFile);

        assertEquals(dataFile.toAbsolutePath().normalize(), storageManager.getDataFile());
    }

    private ApplicationData createApplicationData() {
        ModuleDocument modules = new ModuleDocument(
                1,
                List.of(new Module(
                        new ModuleCode("CS2040S"), "Data Structures and Algorithms", 4, true)));
        RequirementDocument requirements = new RequirementDocument(
                1,
                new ProgrammeInfo("test", "Test Programme", "TEST", 160, List.of()),
                List.of(),
                List.of(new ModuleRequirement(
                        "foundation", "Foundation", "", Set.of("CS2040S"))));
        return new ApplicationData(1, modules, requirements);
    }
}
