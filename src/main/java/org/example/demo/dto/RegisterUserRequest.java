package org.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User Registration Request.
 * Based on RP-001 specification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 20, message = "Name maximum length 20 characters")
    @Pattern(regexp = "^(?!\\d+$)(?![\\W_]+$).*$", 
             message = "Values consisting only of digits or only symbols are not allowed")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be in a valid email format")
    @Size(max = 100, message = "Email maximum length 100 characters")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 12, message = "Password length must be between 8 and 12 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "ConfirmPassword must not be blank")
    private String confirmPassword;
}
