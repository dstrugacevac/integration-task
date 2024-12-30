package integration.task.shared.salesforce.error;

public class SalesforceJobException extends RuntimeException {
    public SalesforceJobException(String message) {
        super(message);
    }
}