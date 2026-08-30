package degreeprogress.gui;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import degreeprogress.models.modules.Module;

/** Displays the form used to create a module. */
public final class ModuleDialog {
    private static final double DIALOG_WIDTH = 420;
    private static final double CONTENT_SPACING = 12;
    private static final int DEFAULT_UNITS = 4;

    private ModuleDialog() {
    }

    /**
     * Opens the module creation form and returns the entered incomplete module.
     *
     * @param owner window that owns the dialog, or {@code null} when no owner is available
     * @return the entered module, or an empty optional when the dialog is cancelled
     */
    public static Optional<Module> showAndWait(Window owner) {
        ModuleForm form = new ModuleForm();
        Label errorMessage = new Label();
        errorMessage.setStyle("-fx-text-fill: #b00020;");
        errorMessage.setWrapText(true);

        Dialog<Module> dialog = new Dialog<>();
        dialog.setTitle("Add module");
        dialog.setHeaderText("Enter the details for the new module.");
        if (owner == null) {
            dialog.initModality(Modality.APPLICATION_MODAL);
        } else {
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
        }
        dialog.getDialogPane().setPrefWidth(DIALOG_WIDTH);
        dialog.getDialogPane().setContent(createContent(form, errorMessage));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        AtomicReference<Module> submittedModule = new AtomicReference<>();
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                submittedModule.set(form.createModule());
                errorMessage.setText("");
            } catch (IllegalArgumentException exception) {
                submittedModule.set(null);
                errorMessage.setText(exception.getMessage());
                event.consume();
            }
        });
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? submittedModule.get()
                : null);

        return dialog.showAndWait();
    }

    private static VBox createContent(ModuleForm form, Label errorMessage) {
        VBox content = new VBox(CONTENT_SPACING);
        content.setPadding(new Insets(8));
        content.getChildren().addAll(
                createField("Module code", form.codeField),
                createField("Module name", form.nameField),
                createField("Number of units", form.unitsSpinner),
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

    private static final class ModuleForm {
        private final TextField codeField = new TextField();
        private final TextField nameField = new TextField();
        private final Spinner<Integer> unitsSpinner = createUnitsSpinner();

        private ModuleForm() {
            codeField.setPromptText("e.g. CS2040S");
            nameField.setPromptText("e.g. Data Structures and Algorithms");
            unitsSpinner.setEditable(true);
            unitsSpinner.getEditor().setPromptText("Number of units");
        }

        private Module createModule() {
            String unitsText = unitsSpinner.getEditor().getText().trim();
            if (unitsText.isEmpty()) {
                throw new IllegalArgumentException("Module units must be provided");
            }

            int units;
            try {
                units = Integer.parseInt(unitsText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Module units must be a whole number");
            }

            return new Module(codeField.getText(), nameField.getText(), units);
        }

        private static Spinner<Integer> createUnitsSpinner() {
            IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(
                    Module.MIN_UNITS, Module.MAX_UNITS, DEFAULT_UNITS);
            Spinner<Integer> spinner = new Spinner<>();
            spinner.setValueFactory(valueFactory);
            spinner.setMaxWidth(Double.MAX_VALUE);
            return spinner;
        }
    }
}
