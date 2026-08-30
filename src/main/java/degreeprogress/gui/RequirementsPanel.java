package degreeprogress.gui;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import degreeprogress.managers.RequirementsManager;
import degreeprogress.models.requirements.Requirement;

/** Displays the configured requirements in a hierarchical tree. */
public final class RequirementsPanel extends VBox {
    private static final double VERTICAL_SPACING = 12;
    private static final double PANEL_PADDING = 16;
    private static final double MIN_PANEL_WIDTH = 220;

    /**
     * Creates a requirements panel populated from the supplied manager.
     *
     * @param requirementsManager manager containing the requirements to display
     * @param requirementSelectionAction action to run when a requirement is selected
     */
    public RequirementsPanel(
            RequirementsManager requirementsManager,
            Consumer<Requirement> requirementSelectionAction) {
        Objects.requireNonNull(requirementsManager);
        Objects.requireNonNull(requirementSelectionAction);

        Label title = new Label("Requirements");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        List<Requirement> rootRequirements = requirementsManager.getRequirements();
        TreeView<Requirement> requirementTree = new TreeView<>(
                createRequirementTree(rootRequirements));
        requirementTree.setShowRoot(false);
        requirementTree.setCellFactory(tree -> createRequirementTreeCell());
        requirementTree.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldItem, selectedItem) -> requirementSelectionAction.accept(
                        selectedItem == null ? null : selectedItem.getValue()));

        StackPane treeContainer = new StackPane(requirementTree);
        if (rootRequirements.isEmpty()) {
            treeContainer.getChildren().add(new Label("No requirements configured."));
        }

        setSpacing(VERTICAL_SPACING);
        setPadding(new Insets(PANEL_PADDING));
        setMinWidth(MIN_PANEL_WIDTH);
        getChildren().addAll(title, treeContainer);
        VBox.setVgrow(treeContainer, Priority.ALWAYS);
    }

    private TreeItem<Requirement> createRequirementTree(List<Requirement> rootRequirements) {
        TreeItem<Requirement> hiddenRoot = new TreeItem<>();
        for (Requirement requirement : rootRequirements) {
            hiddenRoot.getChildren().add(createRequirementTreeItem(requirement));
        }
        return hiddenRoot;
    }

    private TreeItem<Requirement> createRequirementTreeItem(Requirement requirement) {
        TreeItem<Requirement> treeItem = new TreeItem<>(requirement);
        for (Requirement child : requirement.getChildren()) {
            treeItem.getChildren().add(createRequirementTreeItem(child));
        }
        treeItem.setExpanded(!treeItem.getChildren().isEmpty());
        return treeItem;
    }

    private TreeCell<Requirement> createRequirementTreeCell() {
        return new TreeCell<>() {
            @Override
            protected void updateItem(Requirement requirement, boolean empty) {
                super.updateItem(requirement, empty);
                setText(empty || requirement == null ? null : requirement.getName());
            }
        };
    }
}
