package org.example.demo.common;

/**
 * Validation patterns used for DTO field validation.
 */
public final class ValidationPatterns {
    
    /**
     * Pattern to ensure name is not only digits or only symbols.
     * Requires at least one alphanumeric character that is not a digit.
     */
    public static final String NAME_PATTERN = "^(?!\\d+$)(?![\\W_]+$).*$";
    
    /**
     * Pattern to ensure password contains at least one letter and at least one digit.
     * Uses positive lookahead to check for both requirements.
     */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).*$";

    private ValidationPatterns() {
        // Prevent instantiation
    }
}
