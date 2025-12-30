package org.example.demo.user.exception;

import org.example.demo.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends ApiException {

    public AuthenticationFailedException() {
        super(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "AUTHENTICATION_FAILED");
    }
}
