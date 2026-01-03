package org.example.demo.common.exception;

import org.springframework.http.HttpStatus;

public class InternalErrorException extends ApiException {

    public InternalErrorException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "INTERNAL_ERROR");
    }
}
