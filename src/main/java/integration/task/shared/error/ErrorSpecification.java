package integration.task.shared.error;

import org.springframework.http.HttpStatus;

public enum ErrorSpecification {

    // AUTH
    USER_DEFINITION_NOT_VALID("UserDefinitionNotValid", "User");


    String code;
    String target;
    HttpStatus httpStatus;

    ErrorSpecification(String code, String target) {
        this(code, target, HttpStatus.BAD_REQUEST);
    }

    ErrorSpecification(String code, String target, HttpStatus httpStatus) {
        this.code = code;
        this.target = target;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getTarget() {
        return target;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}