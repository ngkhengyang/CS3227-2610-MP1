package degreeprogress.gui;

import java.util.Objects;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/** Provides the application-level toolbar and its file actions. */
public final class ApplicationToolbar extends MenuBar {
    /**
     * Creates a toolbar with Save and Exit actions in the File menu.
     *
     * @param saveAction action to run when the user chooses Save
     * @param exitAction action to run when the user chooses Exit
     */
    public ApplicationToolbar(Runnable saveAction, Runnable exitAction) {
        Objects.requireNonNull(saveAction);
        Objects.requireNonNull(exitAction);

        Menu fileMenu = new Menu("File");

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(event -> saveAction.run());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> exitAction.run());

        fileMenu.getItems().addAll(saveItem, exitItem);
        getMenus().add(fileMenu);
    }
}
