package degreeprogress.gui;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import degreeprogress.managers.RequirementsManager;
import degreeprogress.models.requirements.Requirement;

/** Displays the configured requirements in a hierarchical tree. */
public final class RequirementsPanel extends VBox {
    private static final double VERTICAL_SPACING = 12;
    private static final double PANEL_PADDING = 16;
    private static final double MIN_PANEL_WIDTH = 220;
    private static final double HEADER_SPACING = 12;

    private final RequirementsManager requirementsManager;
    private final Consumer<Requirement> requirementSelectionAction;
    private final Runnable requirementsChangedAction;
    private final TreeView<Requirement> requirementTree;
    private final StackPane treeContainer;

    /**
     * Creates a requirements panel without a mutation callback.
     *
     * @param requirementsManager manager containing the requirements to display
     * @param requirementSelectionAction action to run when a requirement is selected
     */
    public RequirementsPanel(
            RequirementsManager requirementsManager,
            Consumer<Requirement> requirementSelectionAction) {
        this(requirementsManager, requirementSelectionAction, () -> { });
    }

    /**
     * Creates a requirements panel populated from the supplied manager.
     *
     * @param requirementsManager manager containing the requirements to display
     * @param requirementSelectionAction action to run when a requirement is selected
     * @param requirementsChangedAction action to run after a requirement is mutated
     */
    public RequirementsPanel(
            RequirementsManager requirementsManager,
            Consumer<Requirement> requirementSelectionAction,
            Runnable requirementsChangedAction) {
        Objects.requireNonNull(requirementsManager);
        Objects.requireNonNull(requirementSelectionAction);
        Objects.requireNonNull(requirementsChangedAction);
        this.requirementsManager = requirementsManager;
        this.requirementSelectionAction = requirementSelectionAction;
        this.requirementsChangedAction = requirementsChangedAction;

        Label title = new Label("Requirements");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> showAddRequirementDialog());

        HBox header = new HBox(HEADER_SPACING, title, addButton);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        requirementTree = new TreeView<>();
        requirementTree.setShowRoot(false);
        requirementTree.setCellFactory(tree -> createRequirementTreeCell());
        requirementTree.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldItem, selectedItem) -> requirementSelectionAction.accept(
                        selectedItem == null ? null : selectedItem.getValue()));

        treeContainer = new StackPane();
        refresh();

        setSpacing(VERTICAL_SPACING);
        setPadding(new Insets(PANEL_PADDING));
        setMinWidth(MIN_PANEL_WIDTH);
        getChildren().addAll(header, treeContainer);
        VBox.setVgrow(treeContainer, Priority.ALWAYS);
    }

    /** Refreshes the tree from the current requirements manager state. */
    public void refresh() {
        String selectedRequirementId = getSelectedRequirementId();
        List<Requirement> rootRequirements = requirementsManager.getRequirements();
        requirementTree.setRoot(createRequirementTree(rootRequirements));
        treeContainer.getChildren().setAll(requirementTree);
        if (rootRequirements.isEmpty()) {
            Label emptyState = new Label("No requirements configured.");
            emptyState.setMaxWidth(Double.MAX_VALUE);
            emptyState.setAlignment(Pos.CENTER);
            StackPane.setAlignment(emptyState, Pos.CENTER);
            treeContainer.getChildren().add(emptyState);
        }
        if (selectedRequirementId != null) {
            selectRequirement(selectedRequirementId);
        }
    }

    private void showAddRequirementDialog() {
        Window owner = getScene() == null ? null : getScene().getWindow();
        RequirementDialog.showAndWait(owner).ifPresent(this::addRootRequirement);
    }

    private void addRootRequirement(Requirement requirement) {
        try {
            requirementsManager.addRequirement(requirement);
            refresh();
            requirementsChangedAction.run();
            selectRequirement(requirement.getId());
        } catch (IllegalArgumentException exception) {
            showError("Could not add requirement", exception.getMessage());
        }
    }

    private void selectRequirement(String requirementId) {
        selectRequirement(requirementTree.getRoot(), requirementId);
    }

    private String getSelectedRequirementId() {
        TreeItem<Requirement> selectedItem = requirementTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            return null;
        }
        return selectedItem.getValue().getId();
    }

    private boolean selectRequirement(TreeItem<Requirement> treeItem, String requirementId) {
        if (treeItem.getValue() != null && treeItem.getValue().getId().equals(requirementId)) {
            requirementTree.getSelectionModel().select(treeItem);
            return true;
        }
        for (TreeItem<Requirement> child : treeItem.getChildren()) {
            if (selectRequirement(child, requirementId)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
