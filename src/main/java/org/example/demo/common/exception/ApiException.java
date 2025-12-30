package org.example.demo.common.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String messageKey;
    private final String code;

    protected ApiException(HttpStatus status, String messageKey, String code) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getCode() {
        return code;
    }
}
