package degreeprogress.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import degreeprogress.managers.ModulesManager;
import degreeprogress.managers.RequirementsManager;
import degreeprogress.models.modules.ModuleDocument;
import degreeprogress.models.requirements.RequirementDocument;
import degreeprogress.storage.ApplicationData;
import degreeprogress.storage.StorageException;
import degreeprogress.storage.StorageManager;

/**
 * Main JavaFX window for the degree-progress tracker.
 *
 * <p>The window composes the application toolbar and content panels while
 * coordinating application data with the managers and storage component.</p>
 */
public final class MainWindow extends Application {
    private static final String APPLICATION_TITLE = "Degree Progress Tracker";
    private static final double INITIAL_WIDTH = 960;
    private static final double INITIAL_HEIGHT = 640;
    private static final double REQUIREMENTS_PANEL_WIDTH_RATIO = 0.35;

    private StorageManager storageManager;
    private ModulesManager modulesManager;
    private RequirementsManager requirementsManager;
    private ApplicationData applicationData;

    @Override
    public void start(Stage stage) {
        initialiseApplicationData();

        BorderPane root = new BorderPane();
        root.setTop(new ApplicationToolbar(this::saveApplicationData, stage::close));

        RequirementDetailsPanel requirementDetailsPanel = new RequirementDetailsPanel();
        RequirementsPanel requirementsPanel = new RequirementsPanel(
                requirementsManager, requirementDetailsPanel::setRequirement);
        requirementsPanel.prefWidthProperty().bind(
                root.widthProperty().multiply(REQUIREMENTS_PANEL_WIDTH_RATIO));
        root.setLeft(requirementsPanel);
        root.setCenter(requirementDetailsPanel);

        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
        stage.setTitle(APPLICATION_TITLE);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    private void initialiseApplicationData() {
        storageManager = new StorageManager();
        applicationData = storageManager.load();
        modulesManager = new ModulesManager(applicationData.modules().modules());
        requirementsManager = new RequirementsManager(
                applicationData.requirements().requirements());
    }

    private void saveApplicationData() {
        try {
            applicationData = new ApplicationData(
                    applicationData.schemaVersion(),
                    new ModuleDocument(
                            applicationData.modules().schemaVersion(), modulesManager.getModules()),
                    new RequirementDocument(
                            applicationData.requirements().schemaVersion(),
                            applicationData.requirements().programme(),
                            applicationData.requirements().sources(),
                            requirementsManager.getRequirements()));
            storageManager.save(applicationData);
        } catch (StorageException | IllegalArgumentException exception) {
            showSaveError(exception);
        }
    }

    private void showSaveError(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Save failed");
        alert.setHeaderText("Could not save application data");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
