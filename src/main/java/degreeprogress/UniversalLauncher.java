package degreeprogress;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Starts the application from the universal release JAR.
 *
 * <p>The universal JAR contains a separate JavaFX runtime for each supported
 * platform. This bootstrap selects and extracts the matching runtime before
 * loading the regular JavaFX launcher in an isolated class loader.</p>
 */
public final class UniversalLauncher {
    private static final String APPLICATION_LAUNCHER = "degreeprogress.Launcher";
    private static final String PLATFORM_DIRECTORY = "platform/";

    private UniversalLauncher() {
    }

    /**
     * Selects the host platform, loads its JavaFX runtime, and starts the app.
     *
     * @param args command-line arguments forwarded to the JavaFX application
     * @throws Exception if the universal JAR or its selected runtime cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        Path applicationJar = locateApplicationJar();
        String platform = detectPlatform();
        Path runtimeDirectory = Files.createTempDirectory("degree-progress-javafx-");
        runtimeDirectory.toFile().deleteOnExit();

        URL[] classPath = extractPlatformRuntime(applicationJar, platform, runtimeDirectory);
        try (URLClassLoader classLoader = new URLClassLoader(
                classPath, ClassLoader.getPlatformClassLoader())) {
            Thread.currentThread().setContextClassLoader(classLoader);
            invokeApplicationLauncher(classLoader, args);
        }
    }

    private static Path locateApplicationJar() throws URISyntaxException {
        CodeSource source = UniversalLauncher.class.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) {
            throw new IllegalStateException("Could not locate the universal application JAR");
        }

        Path applicationJar = Path.of(source.getLocation().toURI());
        if (!Files.isRegularFile(applicationJar)) {
            throw new IllegalStateException("UniversalLauncher must run from a JAR file");
        }
        return applicationJar;
    }

    private static String detectPlatform() {
        String operatingSystem = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String architecture = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean isArm64 = architecture.equals("aarch64") || architecture.equals("arm64");
        boolean isX64 = architecture.equals("amd64") || architecture.equals("x86_64");

        if (operatingSystem.contains("win")) {
            if (isX64) {
                return "win";
            }
            throw new IllegalStateException("Unsupported Windows architecture: " + architecture);
        }
        if (operatingSystem.contains("mac") || operatingSystem.contains("darwin")) {
            if (isArm64) {
                return "mac-aarch64";
            }
            if (isX64) {
                return "mac";
            }
            throw new IllegalStateException("Unsupported macOS architecture: " + architecture);
        }
        if (operatingSystem.contains("linux")) {
            if (isArm64) {
                return "linux-aarch64";
            }
            if (isX64) {
                return "linux";
            }
            throw new IllegalStateException("Unsupported Linux architecture: " + architecture);
        }
        throw new IllegalStateException("Unsupported operating system: " + operatingSystem);
    }

    private static URL[] extractPlatformRuntime(
            Path applicationJar, String platform, Path runtimeDirectory) throws IOException {
        String platformPrefix = PLATFORM_DIRECTORY + platform + "/";
        List<URL> classPath = new ArrayList<>();
        classPath.add(applicationJar.toUri().toURL());

        try (JarFile jarFile = new JarFile(applicationJar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(platformPrefix)
                        && entry.getName().endsWith(".jar")) {
                    Path extractedJar = extractEntry(jarFile, entry, platformPrefix, runtimeDirectory);
                    classPath.add(extractedJar.toUri().toURL());
                }
            }
        }

        if (classPath.size() == 1) {
            throw new IllegalStateException("No JavaFX runtime is bundled for platform: " + platform);
        }
        return classPath.toArray(URL[]::new);
    }

    private static Path extractEntry(
            JarFile jarFile, JarEntry entry, String platformPrefix, Path runtimeDirectory)
            throws IOException {
        String fileName = entry.getName().substring(platformPrefix.length());
        Path extractedJar = runtimeDirectory.resolve(fileName).normalize();
        if (!runtimeDirectory.equals(extractedJar.getParent())) {
            throw new IOException("Invalid bundled runtime path: " + entry.getName());
        }

        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Files.copy(inputStream, extractedJar, StandardCopyOption.REPLACE_EXISTING);
        }
        extractedJar.toFile().deleteOnExit();
        return extractedJar;
    }

    private static void invokeApplicationLauncher(URLClassLoader classLoader, String[] args)
            throws Exception {
        Class<?> launcherClass = classLoader.loadClass(APPLICATION_LAUNCHER);
        Method mainMethod = launcherClass.getMethod("main", String[].class);
        try {
            mainMethod.invoke(null, (Object) args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw exception;
        }
    }
}
