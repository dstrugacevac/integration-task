package integration.task.shared.salesforce.error;

public class SalesforceBatchUpdateException extends RuntimeException {
    public SalesforceBatchUpdateException(String message) {
        super(message);
    }
}