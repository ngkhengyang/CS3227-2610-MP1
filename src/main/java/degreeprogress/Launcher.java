package degreeprogress;

/**
 * Application launcher kept separate from the JavaFX {@code Application} class
 * so the project can be started reliably from Maven and IDEs.
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        DegreeProgressApp.launch(DegreeProgressApp.class, args);
    }
}

