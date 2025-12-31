package org.example.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.example.demo.user.validation.PasswordMatches;

@PasswordMatches
public class RegisterUserRequest {

    /**
     * RP-001: Required; trimmed; max 20; not only digits or only symbols.
     * Pattern copied from docs/api/api-spec.yaml
     */
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^(?!\\s*$)(?![\\d]+$)(?![^\\w\\s]+$).+$")
    private String name;

    /** RP-001: Required; valid email; max 100; stored lower-cased. */
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    /**
     * RP-001: Required; 8~12; must contain at least one letter and one digit.
     * Pattern copied from docs/api/api-spec.yaml
     */
    @NotBlank
    @Size(min = 8, max = 12)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,12}$")
    private String password;

    /** RP-001: Required; must match password exactly (validated by @PasswordMatches). */
    @NotBlank
    @Size(min = 8, max = 12)
    private String confirmPassword;

    public RegisterUserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

