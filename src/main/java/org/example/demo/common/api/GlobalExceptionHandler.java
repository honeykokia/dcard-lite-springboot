package org.example.demo.common.api;

import java.time.Instant;
import java.util.Locale;

import org.example.demo.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessageKey(),
                ex.getCode(),
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = resolveValidationCode(ex);

        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "VALIDATION_FAILED",
                code,
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    private String resolveValidationCode(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            ObjectError globalError = ex.getBindingResult().getGlobalError();
            if (globalError != null) {
                String constraint = globalError.getCode();
                if ("PasswordMatches".equals(constraint)) {
                    return "INVALID_CONFIRM_PASSWORD";
                }
            }
            return "VALIDATION_FAILED";
        }

        String field = fieldError.getField();
        String normalizedField = normalizeFieldForCode(field);
        String constraint = fieldError.getCode();

        // "required" style codes used in examples (e.g. PASSWORD_REQUIRED)
        if ("NotBlank".equals(constraint) || "NotNull".equals(constraint)) {
            return normalizedField + "_REQUIRED";
        }

        // For email format, prefer INVALID_EMAIL
        if ("Email".equals(constraint)) {
            return "INVALID_EMAIL";
        }

        // For cross-field password match
        if ("PasswordMatches".equals(constraint)) {
            return "INVALID_CONFIRM_PASSWORD";
        }

        return "INVALID_" + normalizedField;
    }

    private String normalizeFieldForCode(String field) {
        if (field == null || field.isBlank()) {
            return "UNKNOWN";
        }
        // confirmPassword -> CONFIRM_PASSWORD
        String snake = field
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toUpperCase(Locale.ROOT);
        return snake;
    }
}
