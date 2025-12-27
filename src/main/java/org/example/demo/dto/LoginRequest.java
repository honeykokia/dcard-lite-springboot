package org.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be in a valid email format")
    @Size(max = 100, message = "Email maximum length 100 characters")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
