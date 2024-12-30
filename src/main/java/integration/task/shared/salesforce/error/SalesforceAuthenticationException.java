package integration.task.shared.salesforce.error;

public class SalesforceAuthenticationException extends RuntimeException {
    public SalesforceAuthenticationException(String message) {
        super(message);
    }
}