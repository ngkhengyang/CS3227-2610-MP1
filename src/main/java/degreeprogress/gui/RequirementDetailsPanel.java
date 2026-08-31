package degreeprogress.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import degreeprogress.managers.ModulesManager;
import degreeprogress.managers.RequirementsManager;
import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.CompositeRequirement;
import degreeprogress.models.requirements.EvaluationResult;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ModuleSelector;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.UnitCountRequirement;

/** Displays details and editing actions for the requirement selected in the tree. */
public final class RequirementDetailsPanel extends VBox {
    private static final double PANEL_PADDING = 20;
    private static final double VERTICAL_SPACING = 16;
    private static final double HEADER_SPACING = 12;
    private static final double FIELD_SPACING = 4;

    private final RequirementsManager requirementsManager;
    private final ModulesManager modulesManager;
    private final Runnable requirementsChangedAction;
    private final Label title;
    private final Button addChildButton;
    private final Button editButton;
    private final Button deleteButton;
    private final VBox details;
    private Requirement selectedRequirement;

    /** Creates an empty requirement details panel. */
    public RequirementDetailsPanel() {
        this(null, null, () -> { });
    }

    /**
     * Creates a requirement details panel that can edit requirements and add children to composites.
     *
     * @param requirementsManager manager containing the requirements to edit
     * @param requirementsChangedAction action to run after a requirement is mutated
     */
    public RequirementDetailsPanel(
            RequirementsManager requirementsManager,
            Runnable requirementsChangedAction) {
        this(requirementsManager, null, requirementsChangedAction);
    }

    /**
     * Creates a requirement details panel that displays progress using the supplied managers.
     *
     * @param requirementsManager manager containing the requirements to edit and evaluate
     * @param modulesManager manager containing the modules used for progress evaluation
     * @param requirementsChangedAction action to run after a requirement is mutated
     */
    public RequirementDetailsPanel(
            RequirementsManager requirementsManager,
            ModulesManager modulesManager,
            Runnable requirementsChangedAction) {
        this.requirementsManager = requirementsManager;
        this.modulesManager = modulesManager;
        this.requirementsChangedAction = Objects.requireNonNull(requirementsChangedAction);

        title = new Label("Requirement details");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setWrapText(true);

        addChildButton = IconFactory.createIconButton("plus", "Add child requirement", 10);
        addChildButton.setOnAction(event -> showAddChildRequirementDialog());
        addChildButton.setVisible(false);
        addChildButton.setManaged(false);

        editButton = IconFactory.createIconButton("pencil", "Edit requirement");
        editButton.setOnAction(event -> showEditRequirementDialog());
        editButton.setDisable(true);

        deleteButton = IconFactory.createIconButton("trash", "Delete requirement");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> showDeleteConfirmation());

        HBox header = new HBox(HEADER_SPACING, title, editButton, deleteButton);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);

        details = new VBox(VERTICAL_SPACING);
        setPadding(new Insets(PANEL_PADDING));
        setSpacing(VERTICAL_SPACING);
        getChildren().addAll(header, details);
        VBox.setVgrow(details, Priority.ALWAYS);

        setRequirement(null);
    }

    /**
     * Displays the supplied requirement, or an empty state when no requirement is selected.
     *
     * @param requirement requirement to display
     */
    public void setRequirement(Requirement requirement) {
        selectedRequirement = requirement;
        details.getChildren().clear();

        if (requirement == null) {
            title.setText("Requirement details");
            setAddChildButtonVisible(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            details.getChildren().add(new Label("Select a requirement to view its details."));
            return;
        }

        title.setText(requirement.getName());
        setAddChildButtonVisible(requirement instanceof CompositeRequirement
                && requirementsManager != null);
        editButton.setDisable(requirementsManager == null);
        deleteButton.setDisable(requirementsManager == null);
        details.getChildren().add(createProgressDetail(requirement));
        details.getChildren().add(createDetail("Type", getRequirementType(requirement)));
        details.getChildren().add(createDetail("Description", getDescription(requirement)));
        details.getChildren().add(createRequirementDetail(requirement));
        addSelectorDetails(requirement);
    }

    /** Refreshes the selected requirement's details and progress. */
    public void refresh() {
        if (selectedRequirement != null) {
            setRequirement(selectedRequirement);
        }
    }

    private VBox createProgressDetail(Requirement requirement) {
        EvaluationResult result = evaluateRequirement(requirement);
        Label progressLabel = new Label(result == null
                ? "Progress: 0 / 0"
                : "Progress: " + result.achieved() + " / " + result.target());
        progressLabel.setStyle("-fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(getProgress(result));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #2e7d32;");
        return new VBox(FIELD_SPACING, progressLabel, progressBar);
    }

    private EvaluationResult evaluateRequirement(Requirement requirement) {
        if (requirementsManager == null || modulesManager == null) {
            return null;
        }

        return requirementsManager.evaluateRequirement(
                requirement.getId(), modulesManager);
    }

    private double getProgress(EvaluationResult result) {
        if (result == null) {
            return 0;
        }
        if (result.target() == 0) {
            return result.fulfilled() ? 1 : 0;
        }
        double progress = (double) result.achieved() / result.target();
        return Math.max(0, Math.min(progress, 1));
    }

    private void setAddChildButtonVisible(boolean visible) {
        addChildButton.setVisible(visible);
        addChildButton.setManaged(visible);
    }

    private void showAddChildRequirementDialog() {
        if (!(selectedRequirement instanceof CompositeRequirement composite)
                || requirementsManager == null) {
            return;
        }

        Window owner = getScene() == null ? null : getScene().getWindow();
        Optional<Requirement> child = RequirementDialog.showAndWait(owner, "Add child");
        child.ifPresent(requirement -> addChildRequirement(composite, requirement));
    }

    private void addChildRequirement(CompositeRequirement parent, Requirement child) {
        try {
            requirementsManager.addChildRequirement(parent.getId(), child);
            requirementsChangedAction.run();
            setRequirement(parent);
        } catch (IllegalArgumentException exception) {
            showError("Could not add child requirement", exception.getMessage());
        }
    }

    private void showEditRequirementDialog() {
        if (selectedRequirement == null || requirementsManager == null) {
            return;
        }

        Window owner = getScene() == null ? null : getScene().getWindow();
        RequirementDialog.showEditAndWait(owner, selectedRequirement)
                .ifPresent(this::editRequirement);
    }

    private void showDeleteConfirmation() {
        if (selectedRequirement == null || requirementsManager == null) {
            return;
        }

        Requirement requirementToDelete = selectedRequirement;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete requirement");
        confirmation.setHeaderText("Delete \"" + requirementToDelete.getName() + "\"?");
        confirmation.setContentText(requirementToDelete instanceof CompositeRequirement
                ? "This will also delete all child requirements."
                : "This action cannot be undone.");

        Window owner = getScene() == null ? null : getScene().getWindow();
        if (owner != null) {
            confirmation.initOwner(owner);
        }

        confirmation.showAndWait()
                .filter(ButtonType.OK::equals)
                .ifPresent(button -> deleteRequirement(requirementToDelete));
    }

    private void deleteRequirement(Requirement requirement) {
        try {
            requirementsManager.deleteRequirement(requirement.getId());
            setRequirement(null);
            requirementsChangedAction.run();
        } catch (IllegalArgumentException exception) {
            showError("Could not delete requirement", exception.getMessage());
        }
    }

    private void editRequirement(Requirement editedRequirement) {
        try {
            Requirement updatedRequirement = requirementsManager.editRequirement(
                    selectedRequirement.getId(), editedRequirement);
            requirementsChangedAction.run();
            setRequirement(updatedRequirement);
        } catch (IllegalArgumentException exception) {
            showError("Could not edit requirement", exception.getMessage());
        }
    }

    private void showError(String errorTitle, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(errorTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createDetail(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");

        Label value = new Label(valueText);
        value.setWrapText(true);

        return new VBox(FIELD_SPACING, label, value);
    }

    private VBox createListDetail(String labelText, List<String> values) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");

        VBox valueList = new VBox(FIELD_SPACING);
        for (String entry : values) {
            Label entryLabel = new Label("• " + entry);
            entryLabel.setWrapText(true);
            valueList.getChildren().add(entryLabel);
        }

        return new VBox(FIELD_SPACING, label, valueList);
    }

    private String getDescription(Requirement requirement) {
        return requirement.getDescription().isBlank()
                ? "No description provided."
                : requirement.getDescription();
    }

    private String getRequirementType(Requirement requirement) {
        if (requirement instanceof AllOfRequirement) {
            return "All of the child requirements";
        }
        if (requirement instanceof AnyOfRequirement) {
            return "Any of the child requirements";
        }
        if (requirement instanceof ModuleRequirement) {
            return "Specific modules";
        }
        if (requirement instanceof ModuleCountRequirement) {
            return "Module count";
        }
        return "Unit count";
    }

    private VBox createRequirementDetail(Requirement requirement) {
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            List<String> moduleDescriptions = sortedValues(moduleRequirement.getModuleCodes()).stream()
                    .map(moduleCode -> "Complete: " + moduleCode)
                    .toList();
            return createListDetail("Requirements", moduleDescriptions);
        }
        if (requirement instanceof CompositeRequirement compositeRequirement) {
            List<String> childNames = compositeRequirement.getChildren().stream()
                    .map(Requirement::getName)
                    .toList();
            Label label = new Label("Requirements");
            label.setStyle("-fx-font-weight: bold;");
            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);

            HBox header = new HBox(HEADER_SPACING, label, addChildButton);
            HBox.setHgrow(label, Priority.ALWAYS);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setMaxWidth(Double.MAX_VALUE);

            if (!childNames.isEmpty()) {
                VBox valueList = new VBox(FIELD_SPACING);
                for (String childName : childNames) {
                    Label childLabel = new Label("• " + childName);
                    childLabel.setWrapText(true);
                    valueList.getChildren().add(childLabel);
                }
                return new VBox(FIELD_SPACING, header, valueList);
            }
            return new VBox(FIELD_SPACING, header, new Label("No child requirements."));
        }
        if (requirement instanceof ModuleCountRequirement moduleCountRequirement) {
            return createDetail("Requirements", formatCountRequirement(
                    "module", moduleCountRequirement.getMinimumModules(),
                    moduleCountRequirement.getMaximumModules()));
        }
        UnitCountRequirement unitCountRequirement = (UnitCountRequirement) requirement;
        return createDetail("Requirements", formatCountRequirement(
                    "unit", unitCountRequirement.getMinimumUnits(),
                    unitCountRequirement.getMaximumUnits()));
    }

    private void addSelectorDetails(Requirement requirement) {
        ModuleSelector selector = getSelector(requirement);
        if (selector == null) {
            return;
        }

        if (!selector.getCodePrefixes().isEmpty()) {
            details.getChildren().add(createListDetail(
                    "Module prefixes:", sortedValues(selector.getCodePrefixes())));
        }
        if (!selector.getModuleCodes().isEmpty()) {
            details.getChildren().add(createListDetail(
                    "Valid modules:", sortedValues(selector.getModuleCodes())));
        }
        if (selector.getMinimumLevel() != null || selector.getMaximumLevel() != null) {
            details.getChildren().add(createDetail("Module levels:", formatModuleLevels(selector)));
        }
    }

    private ModuleSelector getSelector(Requirement requirement) {
        if (requirement instanceof ModuleCountRequirement moduleCountRequirement) {
            return moduleCountRequirement.getSelector();
        }
        if (requirement instanceof UnitCountRequirement unitCountRequirement) {
            return unitCountRequirement.getSelector();
        }
        return null;
    }

    private String formatCountRequirement(String unit, int minimum, Integer maximum) {
        if (maximum == null) {
            return "At least " + minimum + " " + pluralize(unit, minimum) + " taken";
        }
        if (minimum == maximum) {
            return "Exactly " + minimum + " " + pluralize(unit, minimum) + " taken";
        }
        if (minimum == 0) {
            return "At most " + maximum + " " + pluralize(unit, maximum) + " taken";
        }
        return minimum + " to " + maximum + " " + pluralize(unit, maximum) + " taken";
    }

    private String pluralize(String unit, int count) {
        return count == 1 ? unit : unit + "s";
    }

    private String formatModuleLevels(ModuleSelector selector) {
        if (selector.getMinimumLevel() == null) {
            return "At most " + selector.getMaximumLevel();
        }
        if (selector.getMaximumLevel() == null) {
            return "At least " + selector.getMinimumLevel();
        }
        return selector.getMinimumLevel() + " to " + selector.getMaximumLevel();
    }

    private List<String> sortedValues(Iterable<String> values) {
        List<String> sortedValues = new ArrayList<>();
        values.forEach(sortedValues::add);
        return sortedValues.stream().sorted().toList();
    }
}
