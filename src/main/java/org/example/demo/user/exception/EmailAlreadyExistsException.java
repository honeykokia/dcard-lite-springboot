package org.example.demo.user.exception;

import org.example.demo.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "EMAIL_ALREADY_EXISTS");
    }
}
