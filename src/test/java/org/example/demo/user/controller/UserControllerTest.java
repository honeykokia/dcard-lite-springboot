package org.example.demo.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.example.demo.common.api.GlobalExceptionHandler;
import org.example.demo.common.config.SecurityConfig;
import org.example.demo.user.dto.LoginResponse;
import org.example.demo.user.dto.RegisterUserResponse;
import org.example.demo.user.exception.AuthenticationFailedException;
import org.example.demo.user.exception.EmailAlreadyExistsException;
import org.example.demo.user.exception.ValidationFailedException;
import org.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void ct01_register_success_returns_201() throws Exception {
        RegisterUserResponse res = new RegisterUserResponse(123L, "Leo", "leo@example.com", "USER", Instant.parse("2025-12-25T10:00:00Z"));
        when(userService.registerUser(any())).thenReturn(res);

        String body = "{" +
                "\"name\":\"Leo\"," +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"abc12345\"," +
                "\"confirmPassword\":\"abc12345\"" +
                "}";

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.displayName").value("Leo"))
                .andExpect(jsonPath("$.email").value("leo@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void ct02_register_missing_password_returns_400_password_required() throws Exception {
        String body = "{" +
                "\"name\":\"Leo\"," +
                "\"email\":\"leo@example.com\"," +
                "\"confirmPassword\":\"abc12345\"" +
                "}";

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("PASSWORD_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/users/register"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void ct03_register_service_invalid_password_returns_400_invalid_password() throws Exception {
        when(userService.registerUser(any())).thenThrow(new ValidationFailedException("INVALID_PASSWORD"));

        String body = "{" +
                "\"name\":\"Leo\"," +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"12345678\"," +
                "\"confirmPassword\":\"12345678\"" +
                "}";

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }

    @Test
    void ct04_register_email_exists_returns_409() throws Exception {
        when(userService.registerUser(any())).thenThrow(new EmailAlreadyExistsException());

        String body = "{" +
                "\"name\":\"Leo\"," +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"abc12345\"," +
                "\"confirmPassword\":\"abc12345\"" +
                "}";

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void ct05_login_success_returns_200() throws Exception {
        when(userService.loginUser(any())).thenReturn(new LoginResponse(123L, "Leo", "USER", "token"));

        String body = "{" +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"abc12345\"" +
                "}";

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.displayName").value("Leo"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    @Test
    void ct06_login_missing_password_returns_400_password_required() throws Exception {
        String body = "{" +
                "\"email\":\"leo@example.com\"" +
                "}";

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("PASSWORD_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/users/login"));
    }

    @Test
    void ct07_login_auth_failed_returns_401() throws Exception {
        when(userService.loginUser(any())).thenThrow(new AuthenticationFailedException());

        String body = "{" +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"wrong\"" +
                "}";

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void register_confirm_password_mismatch_returns_400_invalid_confirm_password() throws Exception {
        String body = "{" +
                "\"name\":\"Leo\"," +
                "\"email\":\"leo@example.com\"," +
                "\"password\":\"abc12345\"," +
                "\"confirmPassword\":\"abc12346\"" +
                "}";

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.code").value("INVALID_CONFIRM_PASSWORD"));
    }
}
