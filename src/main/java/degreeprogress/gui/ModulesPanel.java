package degreeprogress.gui;

import java.util.List;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    /**
     * Creates a modules panel populated from the supplied manager.
     *
     * @param modulesManager manager containing the modules to display
     */
    public ModulesPanel(ModulesManager modulesManager) {
        Objects.requireNonNull(modulesManager);

        Label title = new Label("Modules");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        List<Module> modules = modulesManager.getModules();
        VBox moduleItems = new VBox(MODULE_ITEM_SPACING);
        moduleItems.setFillWidth(true);
        modules.forEach(module -> moduleItems.getChildren().add(createModuleItem(module)));

        ScrollPane moduleList = new ScrollPane(moduleItems);
        moduleList.setFitToWidth(true);
        moduleList.setHbarPolicy(ScrollBarPolicy.NEVER);
        moduleList.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        StackPane listContainer = new StackPane(moduleList);
        if (modules.isEmpty()) {
            listContainer.getChildren().add(new Label("No modules recorded."));
        }

        setSpacing(VERTICAL_SPACING);
        setPadding(new Insets(PANEL_PADDING));
        setMinWidth(MIN_PANEL_WIDTH);
        getChildren().addAll(title, listContainer);
        VBox.setVgrow(listContainer, Priority.ALWAYS);
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

        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
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
