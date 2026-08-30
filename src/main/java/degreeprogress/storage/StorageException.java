package degreeprogress.storage;

/** Indicates that application data could not be loaded or saved. */
public final class StorageException extends RuntimeException {
    /** Creates an exception with a message. */
    public StorageException(String message) {
        super(message);
    }

    /** Creates an exception with a message and the underlying cause. */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
