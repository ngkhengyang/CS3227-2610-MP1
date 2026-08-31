package degreeprogress.storage;

/** Reports the result of loading application data. */
public record StorageLoadResult(ApplicationData applicationData, boolean corruptedData) {
    /** Validates the loaded application data and fallback status. */
    public StorageLoadResult {
        if (applicationData == null) {
            throw new IllegalArgumentException("Application data must not be null");
        }
    }
}
