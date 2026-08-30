package degreeprogress.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import degreeprogress.models.requirements.AllOfRequirement;
import degreeprogress.models.requirements.AnyOfRequirement;
import degreeprogress.models.requirements.CompositeRequirement;
import degreeprogress.models.requirements.ModuleCountRequirement;
import degreeprogress.models.requirements.ModuleRequirement;
import degreeprogress.models.requirements.ModuleSelector;
import degreeprogress.models.requirements.Requirement;
import degreeprogress.models.requirements.UnitCountRequirement;

/** Displays read-only details for the requirement selected in the tree. */
public final class RequirementDetailsPanel extends VBox {
    private static final double PANEL_PADDING = 20;
    private static final double VERTICAL_SPACING = 16;
    private static final double HEADER_SPACING = 12;
    private static final double FIELD_SPACING = 4;

    private final Label title;
    private final Button editButton;
    private final VBox details;

    /** Creates an empty requirement details panel. */
    public RequirementDetailsPanel() {
        title = new Label("Requirement details");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        editButton = new Button("Edit");
        editButton.setDisable(true);

        HBox header = new HBox(HEADER_SPACING, title, editButton);
        HBox.setHgrow(title, Priority.ALWAYS);

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
        details.getChildren().clear();

        if (requirement == null) {
            title.setText("Requirement details");
            editButton.setDisable(true);
            details.getChildren().add(new Label("Select a requirement to view its details."));
            return;
        }

        title.setText(requirement.getName());
        editButton.setDisable(false);
        details.getChildren().add(createDetail("Type", getRequirementType(requirement)));
        details.getChildren().add(createDetail("Description", getDescription(requirement)));
        details.getChildren().add(createDetail("Configuration", getConfiguration(requirement)));
    }

    private VBox createDetail(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");

        Label value = new Label(valueText);
        value.setWrapText(true);

        return new VBox(FIELD_SPACING, label, value);
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

    private String getConfiguration(Requirement requirement) {
        if (requirement instanceof ModuleRequirement moduleRequirement) {
            return "Modules: " + moduleRequirement.getModuleCodes().stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
        }
        if (requirement instanceof ModuleCountRequirement moduleCountRequirement) {
            return "Minimum modules: " + moduleCountRequirement.getMinimumModules()
                    + "; Maximum modules: "
                    + formatOptionalNumber(moduleCountRequirement.getMaximumModules())
                    + "; Selector: " + formatSelector(moduleCountRequirement.getSelector());
        }
        if (requirement instanceof UnitCountRequirement unitCountRequirement) {
            return "Minimum units: " + unitCountRequirement.getMinimumUnits()
                    + "; Maximum units: "
                    + formatOptionalNumber(unitCountRequirement.getMaximumUnits())
                    + "; Selector: " + formatSelector(unitCountRequirement.getSelector());
        }
        return formatChildren((CompositeRequirement) requirement);
    }

    private String formatOptionalNumber(Integer value) {
        return value == null ? "None" : value.toString();
    }

    private String formatSelector(ModuleSelector selector) {
        List<String> criteria = new ArrayList<>();
        if (!selector.getModuleCodes().isEmpty()) {
            criteria.add("codes " + String.join(", ", selector.getModuleCodes().stream().sorted().toList()));
        }
        if (!selector.getCodePrefixes().isEmpty()) {
            criteria.add("prefixes "
                    + String.join(", ", selector.getCodePrefixes().stream().sorted().toList()));
        }
        if (selector.getMinimumLevel() != null) {
            criteria.add("minimum level " + selector.getMinimumLevel());
        }
        if (selector.getMaximumLevel() != null) {
            criteria.add("maximum level " + selector.getMaximumLevel());
        }
        return criteria.isEmpty() ? "all modules" : String.join("; ", criteria);
    }

    private String formatChildren(CompositeRequirement requirement) {
        List<String> childNames = requirement.getChildren().stream()
                .map(Requirement::getName)
                .toList();
        return childNames.isEmpty() ? "No child requirements." : String.join(", ", childNames);
    }
}
