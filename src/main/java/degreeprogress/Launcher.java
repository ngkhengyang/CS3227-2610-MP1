package degreeprogress;

import degreeprogress.gui.MainWindow;

/**
 * Starts the degree-progress tracker application.
 *
 * <p>This executable entry point is kept outside the GUI package. The JavaFX
 * window and presentation components remain under {@code degreeprogress.gui}.</p>
 */
public final class Launcher {
    private Launcher() {
    }

    /** Launches the JavaFX application. */
    public static void main(String[] args) {
        MainWindow.launch(MainWindow.class, args);
    }
}
