package integration.task.shared.salesforce.error;

public class SalesforceJsonException extends RuntimeException {
    public SalesforceJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}