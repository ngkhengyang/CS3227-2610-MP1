package degreeprogress.gui;

import java.util.List;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import degreeprogress.managers.ModulesManager;
import degreeprogress.models.modules.Module;

/** Displays the modules recorded by the student. */
public final class ModulesPanel extends VBox {
    private static final double VERTICAL_SPACING = 12;
    private static final double PANEL_PADDING = 16;
    private static final double MIN_PANEL_WIDTH = 280;
    private static final double MODULE_ITEM_SPACING = 8;
    private static final double MODULE_ITEM_PADDING = 8;
    private static final double CONTROL_SPACING = 6;
    private static final double HEADER_SPACING = 12;

    private final ModulesManager modulesManager;
    private final Runnable modulesChangedAction;
    private final VBox moduleItems;
    private final ScrollPane moduleList;
    private final StackPane listContainer;

    /**
     * Creates a modules panel populated from the supplied manager.
     *
     * @param modulesManager manager containing the modules to display
     */
    public ModulesPanel(ModulesManager modulesManager) {
        this(modulesManager, () -> { });
    }

    /**
     * Creates a modules panel that refreshes and invokes a callback after a module is added.
     *
     * @param modulesManager manager containing the modules to display and mutate
     * @param modulesChangedAction action to run after a module is added
     */
    public ModulesPanel(ModulesManager modulesManager, Runnable modulesChangedAction) {
        Objects.requireNonNull(modulesManager);
        Objects.requireNonNull(modulesChangedAction);
        this.modulesManager = modulesManager;
        this.modulesChangedAction = modulesChangedAction;

        Label title = new Label("Modules");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addButton = IconFactory.createIconButton("plus", "Add module");
        addButton.setOnAction(event -> showAddModuleDialog());

        HBox header = new HBox(HEADER_SPACING, title, addButton);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        moduleItems = new VBox(MODULE_ITEM_SPACING);
        moduleItems.setFillWidth(true);

        moduleList = new ScrollPane(moduleItems);
        moduleList.setFitToWidth(true);
        moduleList.setHbarPolicy(ScrollBarPolicy.NEVER);
        moduleList.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        listContainer = new StackPane(moduleList);
        refresh();

        setSpacing(VERTICAL_SPACING);
        setPadding(new Insets(PANEL_PADDING));
        setMinWidth(MIN_PANEL_WIDTH);
        getChildren().addAll(header, listContainer);
        VBox.setVgrow(listContainer, Priority.ALWAYS);
    }

    /** Refreshes the module list from the current modules manager state. */
    public void refresh() {
        List<Module> modules = modulesManager.getModules();
        moduleItems.getChildren().setAll(
                modules.stream().map(this::createModuleItem).toList());
        listContainer.getChildren().setAll(moduleList);
        if (modules.isEmpty()) {
            Label emptyState = new Label("No modules recorded.");
            emptyState.setMaxWidth(Double.MAX_VALUE);
            emptyState.setAlignment(Pos.CENTER);
            StackPane.setAlignment(emptyState, Pos.CENTER);
            listContainer.getChildren().add(emptyState);
        }
    }

    private void showAddModuleDialog() {
        Window owner = getScene() == null ? null : getScene().getWindow();
        ModuleDialog.showAndWait(owner).ifPresent(this::addModule);
    }

    private void showEditModuleDialog(Module module) {
        Window owner = getScene() == null ? null : getScene().getWindow();
        ModuleDialog.showEditAndWait(owner, module)
                .ifPresent(editedModule -> editModule(module, editedModule));
    }

    private void addModule(Module module) {
        try {
            modulesManager.addModule(module.getCode(), module.getName(), module.getUnits());
            refresh();
            modulesChangedAction.run();
        } catch (IllegalArgumentException exception) {
            showError("Could not add module", exception.getMessage());
        }
    }

    private void editModule(Module existingModule, Module editedModule) {
        try {
            modulesManager.editModule(
                    existingModule.getCode(),
                    editedModule.getCode(),
                    editedModule.getName(),
                    editedModule.getUnits());
            refresh();
            modulesChangedAction.run();
        } catch (IllegalArgumentException exception) {
            showError("Could not edit module", exception.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private HBox createModuleItem(Module module) {
        Label moduleName = new Label(module.getCode() + " " + module.getName());
        moduleName.setMinWidth(0);
        moduleName.setMaxWidth(Double.MAX_VALUE);
        moduleName.setWrapText(true);
        moduleName.setStyle("-fx-font-weight: bold;");

        Label units = new Label(module.getUnits() + " " + pluralizeUnits(module.getUnits()));

        VBox moduleInfo = new VBox(MODULE_ITEM_SPACING, moduleName, units);
        moduleInfo.setMinWidth(0);
        moduleInfo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(moduleInfo, Priority.ALWAYS);

        CheckBox completionCheckbox = new CheckBox();
        completionCheckbox.setSelected(module.isCompleted());
        completionCheckbox.setDisable(true);

        Button editButton = IconFactory.createIconButton("pencil", "Edit module");
        editButton.setOnAction(event -> showEditModuleDialog(module));
        Button deleteButton = IconFactory.createIconButton("trash", "Delete module");
        HBox controls = new HBox(CONTROL_SPACING, completionCheckbox, editButton, deleteButton);
        controls.setMinWidth(Region.USE_PREF_SIZE);
        controls.setMaxWidth(Region.USE_PREF_SIZE);
        controls.setAlignment(Pos.CENTER_RIGHT);

        HBox moduleItem = new HBox(MODULE_ITEM_SPACING, moduleInfo, controls);
        moduleItem.setAlignment(Pos.CENTER_LEFT);
        moduleItem.setPadding(new Insets(MODULE_ITEM_PADDING));
        moduleItem.setMinWidth(0);
        moduleItem.setMaxWidth(Double.MAX_VALUE);
        return moduleItem;
    }

    private String pluralizeUnits(int units) {
        return units == 1 ? "unit" : "units";
    }
}
