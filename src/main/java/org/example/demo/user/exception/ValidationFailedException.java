package org.example.demo.user.exception;

import org.example.demo.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ValidationFailedException extends ApiException {

    public ValidationFailedException(String code) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", code);
    }
}
