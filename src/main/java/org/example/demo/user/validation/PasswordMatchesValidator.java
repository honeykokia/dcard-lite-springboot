package org.example.demo.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.example.demo.user.dto.RegisterUserRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterUserRequest> {

    @Override
    public boolean isValid(RegisterUserRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String password = value.getPassword();
        String confirmPassword = value.getConfirmPassword();

        // Let @NotBlank / @Size handle "required" and length errors.
        if (password == null || confirmPassword == null) {
            return true;
        }
        return password.equals(confirmPassword);
    }
}

