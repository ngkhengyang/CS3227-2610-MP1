package degreeprogress;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the degree-progress tracker.
 *
 * <p>The initial shell deliberately contains no application UI. Features will
 * be introduced through separate views and domain services in later steps.</p>
 */
public final class DegreeProgressApp extends Application {
    private static final String APPLICATION_TITLE = "Degree Progress Tracker";
    private static final double INITIAL_WIDTH = 960;
    private static final double INITIAL_HEIGHT = 640;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);

        stage.setTitle(APPLICATION_TITLE);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }
}

