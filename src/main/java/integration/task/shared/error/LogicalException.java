package integration.task.shared.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class LogicalException extends RuntimeException {

    final ErrorSpecification errorSpecification;

    protected LogicalException(String message, ErrorSpecification errorSpecification) {
        super(message);

        this.errorSpecification = errorSpecification;
    }

    protected LogicalException(Throwable throwable, ErrorSpecification errorSpecification) {
        super(throwable);

        this.errorSpecification = errorSpecification;
    }


    protected LogicalException(String message, ErrorSpecification errorSpecification, Throwable cause) {
        super(message, cause);

        this.errorSpecification = errorSpecification;
    }

    public HttpStatus getHttpStatus() {
        return errorSpecification.getHttpStatus();
    }
}
