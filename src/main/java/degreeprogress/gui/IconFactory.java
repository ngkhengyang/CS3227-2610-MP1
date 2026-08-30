package degreeprogress.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Creates icon-only buttons from SVG assets bundled with the application. */
final class IconFactory {
    private static final double SVG_VIEW_BOX_SIZE = 24;
    private static final double DEFAULT_ICON_SIZE = 14;
    // JavaFX layout dimensions are measured in logical pixels.
    private static final double ICON_BUTTON_PADDING = 5;
    private static final String ICON_RESOURCE_DIRECTORY = "/assets/icons/";
    private static final Pattern PATH_DATA_PATTERN = Pattern.compile(
            "<path\\b[^>]*\\bd\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private IconFactory() {
    }

    /**
     * Creates a button with a bundled SVG icon and accessible descriptive text.
     *
     * @param iconName name of the SVG asset without its file extension
     * @param accessibleText text describing the button action
     * @return an icon-only button
     */
    static Button createIconButton(String iconName, String accessibleText) {
        return createIconButton(iconName, accessibleText, DEFAULT_ICON_SIZE);
    }

    /**
     * Creates a button with a bundled SVG icon, accessible descriptive text, and custom icon size.
     *
     * @param iconName name of the SVG asset without its file extension
     * @param accessibleText text describing the button action
     * @param iconSize icon size in JavaFX logical pixels
     * @return an icon-only button
     * @throws IllegalArgumentException if the icon size is not positive and finite
     */
    static Button createIconButton(String iconName, String accessibleText, double iconSize) {
        if (!Double.isFinite(iconSize) || iconSize <= 0) {
            throw new IllegalArgumentException("Icon size must be positive and finite");
        }

        SVGPath icon = new SVGPath();
        icon.setContent(loadPathData(iconName));
        icon.setScaleX(iconSize / SVG_VIEW_BOX_SIZE);
        icon.setScaleY(iconSize / SVG_VIEW_BOX_SIZE);
        icon.setStyle("-fx-fill: -fx-text-base-color;");

        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(iconSize, iconSize);
        iconContainer.setPrefSize(iconSize, iconSize);
        iconContainer.setMaxSize(iconSize, iconSize);

        Button button = new Button(null, iconContainer);
        double buttonSize = getButtonSize(iconSize);
        button.setPadding(new Insets(ICON_BUTTON_PADDING));
        button.setMinSize(buttonSize, buttonSize);
        button.setPrefSize(buttonSize, buttonSize);
        button.setMaxSize(buttonSize, buttonSize);
        button.setAccessibleText(accessibleText);
        button.setTooltip(new Tooltip(accessibleText));
        return button;
    }

    private static double getButtonSize(double iconSize) {
        return iconSize + 2 * ICON_BUTTON_PADDING;
    }

    private static String loadPathData(String iconName) {
        String resourcePath = ICON_RESOURCE_DIRECTORY + iconName + ".svg";
        try (InputStream inputStream = IconFactory.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing icon resource: " + resourcePath);
            }

            String svg = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = PATH_DATA_PATTERN.matcher(svg);
            if (!matcher.find()) {
                throw new IllegalStateException("Icon resource has no path data: " + resourcePath);
            }
            return matcher.group(1);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read icon resource: " + resourcePath, exception);
        }
    }
}
