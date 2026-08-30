package degreeprogress.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import degreeprogress.models.modules.ModuleCode;
import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.CompositeRequirement;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ModuleSelector;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.UnitCountRequirement;

/** Displays the shared form used to create and edit root and child requirements. */
public final class RequirementDialog {
    private static final double DIALOG_WIDTH = 520;
    private static final double DIALOG_VIEWPORT_HEIGHT = 450;
    private static final double CONTENT_SPACING = 12;
    private static final int DESCRIPTION_ROW_COUNT = 3;
    private static final int MULTI_VALUE_ROW_COUNT = 3;

    private RequirementDialog() {
    }

    /**
     * Opens the requirement creation form and returns the created requirement.
     *
     * @param owner window that owns the dialog, or {@code null} when no owner is available
     * @return the created requirement, or an empty optional when the dialog is cancelled
     */
    public static Optional<Requirement> showAndWait(Window owner) {
        return showAndWait(owner, "Add");
    }

    /**
     * Opens the requirement creation form with a context-specific title.
     *
     * @param owner window that owns the dialog, or {@code null} when no owner is available
     * @param dialogTitle title to display for the creation action
     * @return the created requirement, or an empty optional when the dialog is cancelled
     */
    public static Optional<Requirement> showAndWait(Window owner, String dialogTitle) {
        return showAndWait(
                owner,
                dialogTitle,
                new RequirementForm(),
                "Enter the details for the new requirement.");
    }

    /**
     * Opens the requirement edit form with the existing values pre-populated.
     *
     * @param owner window that owns the dialog, or {@code null} when no owner is available
     * @param existingRequirement requirement to edit
     * @return the edited requirement, or an empty optional when the dialog is cancelled
     */
    public static Optional<Requirement> showEditAndWait(
            Window owner, Requirement existingRequirement) {
        Objects.requireNonNull(existingRequirement);
        return showAndWait(
                owner,
                "Edit requirement",
                new RequirementForm(existingRequirement),
                "Update the details for this requirement.");
    }

    private static Optional<Requirement> showAndWait(
            Window owner, String dialogTitle, RequirementForm form, String headerText) {
        Label errorMessage = new Label();
        errorMessage.setStyle("-fx-text-fill: #b00020;");
        errorMessage.setWrapText(true);

        Dialog<Requirement> dialog = new Dialog<>();
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(headerText);
        if (owner == null) {
            dialog.initModality(Modality.APPLICATION_MODAL);
        } else {
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
        }
        dialog.getDialogPane().setPrefWidth(DIALOG_WIDTH);
        ScrollPane formScrollPane = new ScrollPane(createContent(form, errorMessage));
        formScrollPane.setFitToWidth(true);
        formScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        formScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        formScrollPane.setPrefViewportHeight(DIALOG_VIEWPORT_HEIGHT);
        formScrollPane.setMaxHeight(Double.MAX_VALUE);
        dialog.getDialogPane().setContent(formScrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        AtomicReference<Requirement> submittedRequirement = new AtomicReference<>();
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                submittedRequirement.set(form.createRequirement());
                errorMessage.setText("");
            } catch (IllegalArgumentException exception) {
                submittedRequirement.set(null);
                errorMessage.setText(exception.getMessage());
                event.consume();
            }
        });
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? submittedRequirement.get()
                : null);

        return dialog.showAndWait();
    }

    private static VBox createContent(RequirementForm form, Label errorMessage) {
        VBox content = new VBox(CONTENT_SPACING);
        content.setPadding(new Insets(8));

        content.getChildren().addAll(
                createField("Name", form.nameField),
                createField("Description (optional)", form.descriptionField),
                createField("Type", form.typeSelector),
                form.typeSpecificFields,
                errorMessage);
        return content;
    }

    private static VBox createField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");
        VBox fieldContainer = new VBox(4, label, field);
        VBox.setVgrow(field, Priority.NEVER);
        return fieldContainer;
    }

    private enum RequirementType {
        SPECIFIC_MODULES("Specific modules"),
        MODULE_COUNT("Module count"),
        UNIT_COUNT("Unit count"),
        ALL_OF("All child requirements"),
        ANY_OF("Any child requirement");

        private final String displayName;

        RequirementType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final class RequirementForm {
        private String requirementId;
        private boolean editMode;

        private final TextField nameField = new TextField();
        private final TextArea descriptionField = new TextArea();
        private final ComboBox<RequirementType> typeSelector = new ComboBox<>();
        private final VBox typeSpecificFields = new VBox(CONTENT_SPACING);

        private final TextArea moduleCodesField = createMultiValueField("CS1231S, CS2040S");
        private final TextArea selectorCodesField = createMultiValueField("CS4248, CS4262");
        private final TextArea selectorPrefixesField = createMultiValueField("CS, IFS, CP");
        private final TextField minimumLevelField = new TextField();
        private final TextField maximumLevelField = new TextField();
        private final TextField minimumCountField = new TextField();
        private final TextField maximumCountField = new TextField();

        private RequirementForm() {
            requirementId = UUID.randomUUID().toString();
            initialiseFields();
        }

        private RequirementForm(Requirement existingRequirement) {
            this();
            requirementId = Objects.requireNonNull(existingRequirement).getId();
            editMode = true;
            populate(existingRequirement);
        }

        private void initialiseFields() {
            nameField.setPromptText("Requirement name");
            descriptionField.setPromptText("Describe what this requirement represents");
            descriptionField.setWrapText(true);
            descriptionField.setPrefRowCount(DESCRIPTION_ROW_COUNT);

            typeSelector.getItems().addAll(RequirementType.values());
            typeSelector.setValue(RequirementType.SPECIFIC_MODULES);
            typeSelector.valueProperty().addListener(
                    (observable, oldType, newType) -> updateTypeSpecificFields());

            minimumLevelField.setPromptText("e.g. 4000");
            maximumLevelField.setPromptText("e.g. 5000");
            minimumCountField.setPromptText("Optional; defaults to 0");
            maximumCountField.setPromptText("Optional maximum");
            updateTypeSpecificFields();
        }

        private static TextArea createMultiValueField(String prompt) {
            TextArea field = new TextArea();
            field.setPromptText(prompt);
            field.setWrapText(true);
            field.setPrefRowCount(MULTI_VALUE_ROW_COUNT);
            return field;
        }

        private void updateTypeSpecificFields() {
            typeSpecificFields.getChildren().clear();
            switch (typeSelector.getValue()) {
            case SPECIFIC_MODULES -> typeSpecificFields.getChildren().add(
                    createField("Module codes (comma or line separated)", moduleCodesField));
            case MODULE_COUNT -> addCountFields(
                    "Minimum modules (optional)", "Maximum modules (optional)");
            case UNIT_COUNT -> addCountFields(
                    "Minimum units (optional)", "Maximum units (optional)");
            case ALL_OF, ANY_OF -> typeSpecificFields.getChildren().add(
                    new Label(editMode
                            ? "Child requirements are preserved when editing."
                            : "Child requirements can be added after creation."));
            default -> throw new IllegalStateException("Unsupported requirement type");
            }
        }

        private void populate(Requirement existingRequirement) {
            nameField.setText(existingRequirement.getName());
            descriptionField.setText(existingRequirement.getDescription());

            RequirementType existingType = getRequirementType(existingRequirement);
            if (existingRequirement instanceof CompositeRequirement) {
                typeSelector.getItems().setAll(RequirementType.ALL_OF, RequirementType.ANY_OF);
                typeSelector.setDisable(false);
            } else {
                typeSelector.getItems().setAll(existingType);
                typeSelector.setDisable(true);
            }
            typeSelector.setValue(existingType);

            if (existingRequirement instanceof ModuleRequirement moduleRequirement) {
                moduleCodesField.setText(joinValues(moduleRequirement.getModuleCodes()));
            } else if (existingRequirement instanceof ModuleCountRequirement moduleCountRequirement) {
                populateCountFields(
                        moduleCountRequirement.getSelector(),
                        moduleCountRequirement.getMinimumModules(),
                        moduleCountRequirement.getMaximumModules());
            } else if (existingRequirement instanceof UnitCountRequirement unitCountRequirement) {
                populateCountFields(
                        unitCountRequirement.getSelector(),
                        unitCountRequirement.getMinimumUnits(),
                        unitCountRequirement.getMaximumUnits());
            }
        }

        private RequirementType getRequirementType(Requirement requirement) {
            if (requirement instanceof ModuleRequirement) {
                return RequirementType.SPECIFIC_MODULES;
            }
            if (requirement instanceof ModuleCountRequirement) {
                return RequirementType.MODULE_COUNT;
            }
            if (requirement instanceof UnitCountRequirement) {
                return RequirementType.UNIT_COUNT;
            }
            if (requirement instanceof AllOfRequirement) {
                return RequirementType.ALL_OF;
            }
            if (requirement instanceof AnyOfRequirement) {
                return RequirementType.ANY_OF;
            }
            throw new IllegalArgumentException("Unsupported requirement type");
        }

        private void populateCountFields(
                ModuleSelector selector, int minimumCount, Integer maximumCount) {
            minimumCountField.setText(Integer.toString(minimumCount));
            maximumCountField.setText(formatOptionalInteger(maximumCount));
            selectorCodesField.setText(joinValues(selector.getModuleCodes()));
            selectorPrefixesField.setText(joinValues(selector.getCodePrefixes()));
            minimumLevelField.setText(formatOptionalInteger(selector.getMinimumLevel()));
            maximumLevelField.setText(formatOptionalInteger(selector.getMaximumLevel()));
        }

        private String formatOptionalInteger(Integer value) {
            return value == null ? "" : Integer.toString(value);
        }

        private void addCountFields(String minimumLabel, String maximumLabel) {
            typeSpecificFields.getChildren().add(createField(minimumLabel, minimumCountField));
            typeSpecificFields.getChildren().add(createField(maximumLabel, maximumCountField));
            typeSpecificFields.getChildren().add(
                    createField("Matching module codes (optional)", selectorCodesField));
            typeSpecificFields.getChildren().add(
                    createField("Module prefixes (optional)", selectorPrefixesField));
            typeSpecificFields.getChildren().add(
                    createField("Minimum module level (optional)", minimumLevelField));
            typeSpecificFields.getChildren().add(
                    createField("Maximum module level (optional)", maximumLevelField));
        }

        private Requirement createRequirement() {
            String name = requireText(nameField.getText(), "Name");
            String description = descriptionField.getText().trim();
            RequirementType selectedType = typeSelector.getValue();
            if (selectedType == null) {
                throw new IllegalArgumentException("Type is required");
            }

            return switch (selectedType) {
            case SPECIFIC_MODULES -> new ModuleRequirement(
                    requirementId, name, description,
                    parseModuleCodes(moduleCodesField.getText(), "Module codes", true));
            case MODULE_COUNT -> new ModuleCountRequirement(
                    requirementId,
                    name,
                    description,
                    createSelector(),
                    parseIntegerOrDefault(minimumCountField.getText(), "Minimum modules"),
                    parseOptionalInteger(maximumCountField.getText(), "Maximum modules"));
            case UNIT_COUNT -> new UnitCountRequirement(
                    requirementId,
                    name,
                    description,
                    createSelector(),
                    parseIntegerOrDefault(minimumCountField.getText(), "Minimum units"),
                    parseOptionalInteger(maximumCountField.getText(), "Maximum units"));
            case ALL_OF -> new AllOfRequirement(requirementId, name, description, List.of());
            case ANY_OF -> new AnyOfRequirement(requirementId, name, description, List.of());
            };
        }

        private String joinValues(Iterable<String> values) {
            List<String> sortedValues = new ArrayList<>();
            values.forEach(sortedValues::add);
            return String.join(", ", sortedValues.stream().sorted().toList());
        }

        private ModuleSelector createSelector() {
            return new ModuleSelector(
                    parseModuleCodes(selectorCodesField.getText(), "Matching module codes", false),
                    parsePrefixes(selectorPrefixesField.getText(), "Module prefixes", false),
                    parseOptionalInteger(minimumLevelField.getText(), "Minimum module level"),
                    parseOptionalInteger(maximumLevelField.getText(), "Maximum module level"));
        }

        private int parseIntegerOrDefault(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                return 0;
            }
            return parseNonNegativeInteger(value.trim(), fieldName);
        }

        private Integer parseOptionalInteger(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return parseNonNegativeInteger(value.trim(), fieldName);
        }

        private int parseNonNegativeInteger(String value, String fieldName) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    throw new IllegalArgumentException(fieldName + " must not be negative");
                }
                return parsed;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(fieldName + " must be a whole number");
            }
        }

        private Set<String> parseValues(String value, String fieldName, boolean required) {
            Set<String> values = new LinkedHashSet<>();
            if (value != null && !value.isBlank()) {
                for (String entry : value.split("[,\\r\\n]+")) {
                    if (!entry.isBlank()) {
                        values.add(entry.trim());
                    }
                }
            }
            if (required && values.isEmpty()) {
                throw new IllegalArgumentException(fieldName + " must contain at least one value");
            }
            return values;
        }

        private Set<String> parseModuleCodes(String value, String fieldName, boolean required) {
            Set<String> values = parseValues(value, fieldName, required);
            Set<String> normalizedCodes = new LinkedHashSet<>();
            for (String code : values) {
                try {
                    normalizedCodes.add(new ModuleCode(code).value());
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            fieldName + " contains an invalid module code: "
                                    + exception.getMessage());
                }
            }
            return normalizedCodes;
        }

        private Set<String> parsePrefixes(String value, String fieldName, boolean required) {
            Set<String> values = parseValues(value, fieldName, required);
            for (String prefix : values) {
                if (prefix.chars().anyMatch(character -> !isAsciiLetter(character))) {
                    throw new IllegalArgumentException(fieldName + " must contain letters only");
                }
            }
            return values;
        }

        private boolean isAsciiLetter(int character) {
            return character >= 'A' && character <= 'Z'
                    || character >= 'a' && character <= 'z';
        }

        private String requireText(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return value.trim();
        }
    }
}
