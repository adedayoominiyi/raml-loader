package guru.nidi.loader;

/**
 *
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;

    public ResourceNotFoundException(String resourceName, Throwable cause) {
        super(cause);
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String resourceName, String message) {
        super(message);
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public String getMessage() {
        return "Resource '" + resourceName + "' not found: " + super.getMessage();
    }
}
