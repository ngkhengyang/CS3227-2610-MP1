# Corrupted storage fallback

Updated startup loading so malformed or incompatible application data no longer
causes JavaFX startup to fail. `StorageManager.loadWithStatus()` returns the
bundled module and requirement defaults and marks the result as corrupted when
the user data cannot be parsed or validated. `MainWindow` shows a warning with
the data-file path before displaying the main window.

Added storage tests for invalid JSON, an unsupported schema version, and the
smoke-test case where the root `modules` attribute is renamed to `module`.
