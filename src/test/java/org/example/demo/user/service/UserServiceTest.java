package org.example.demo.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import org.example.demo.user.dto.LoginRequest;
import org.example.demo.user.dto.LoginResponse;
import org.example.demo.user.dto.RegisterUserRequest;
import org.example.demo.user.dto.RegisterUserResponse;
import org.example.demo.user.entity.User;
import org.example.demo.user.exception.AuthenticationFailedException;
import org.example.demo.user.exception.EmailAlreadyExistsException;
import org.example.demo.user.exception.ValidationFailedException;
import org.example.demo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class UserServiceTest {

    private UserRepository userRepository;
    private JwtTokenService jwtTokenService;
    private Validator validator;
    private UserService userService;
    private NormalizerService normalizerService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtTokenService = mock(JwtTokenService.class);
        normalizerService = mock(NormalizerService.class);
        // make mock NormalizerService behave like the real implementation for tests
        when(normalizerService.normalizeEmail(anyString())).thenAnswer(invocation -> {
            String s = invocation.getArgument(0);
            return s == null ? null : s.trim().toLowerCase(java.util.Locale.ROOT);
        });
        when(normalizerService.normalizeDisplayName(anyString())).thenAnswer(invocation -> {
            String s = invocation.getArgument(0);
            return s == null ? null : s.trim();
        });
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        userService = new UserService(userRepository, jwtTokenService, validator, normalizerService);
    }

    @Test
    void uc01_register_success_creates_user_and_returns_response() throws Exception {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName(" Leo ");
        req.setEmail("Leo@Example.com ");
        req.setPassword("abc12345");
        req.setConfirmPassword("abc12345");

        when(userRepository.findByEmail("leo@example.com")).thenReturn(Optional.empty());

        User saved = new User("leo@example.com", "hash", "Leo");
        setField(saved, "userId", 123L);
        setField(saved, "createdAt", Instant.parse("2025-12-25T10:00:00Z"));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterUserResponse res = userService.registerUser(req);

        assertEquals(123L, res.getUserId());
        assertEquals("Leo", res.getDisplayName());
        assertEquals("leo@example.com", res.getEmail());
        assertEquals("USER", res.getRole());
        assertNotNull(res.getCreatedAt());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("leo@example.com", captor.getValue().getEmail());
        assertEquals("Leo", captor.getValue().getDisplayName());
    }

    @Test
    void uc02_register_password_missing_letter_or_digit_returns_400_invalid_password() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName("Leo");
        req.setEmail("leo@example.com");
        req.setPassword("12345678");
        req.setConfirmPassword("12345678");

        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> userService.registerUser(req));
        assertEquals("INVALID_PASSWORD", ex.getCode());
    }

    @Test
    void uc03_register_password_length_invalid_returns_400_invalid_password() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName("Leo");
        req.setEmail("leo@example.com");
        req.setPassword("a1b2c");
        req.setConfirmPassword("a1b2c");

        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> userService.registerUser(req));
        assertEquals("INVALID_PASSWORD", ex.getCode());
    }

    @Test
    void uc04_register_confirm_password_mismatch_returns_400_invalid_confirm_password() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName("Leo");
        req.setEmail("leo@example.com");
        req.setPassword("abc12345");
        req.setConfirmPassword("abc12346");

        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> userService.registerUser(req));
        assertEquals("INVALID_CONFIRM_PASSWORD", ex.getCode());
    }

    @Test
    void uc05_register_invalid_email_returns_400_invalid_email() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName("Leo");
        req.setEmail("not-an-email");
        req.setPassword("abc12345");
        req.setConfirmPassword("abc12345");

        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> userService.registerUser(req));
        assertEquals("INVALID_EMAIL", ex.getCode());
    }

    @Test
    void uc06_register_email_already_exists_returns_409() {
        RegisterUserRequest req = new RegisterUserRequest();
        req.setName("Leo");
        req.setEmail("leo@example.com");
        req.setPassword("abc12345");
        req.setConfirmPassword("abc12345");

        when(userRepository.findByEmail("leo@example.com")).thenReturn(Optional.of(new User("leo@example.com", "h", "Leo")));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(req));
    }

    @Test
    void login_success_returns_access_token() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("leo@example.com");
        req.setPassword("abc12345");

        // password hash matches "abc12345"
        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc12345");
        User user = new User("leo@example.com", hash, "Leo");
        setField(user, "userId", 123L);

        when(userRepository.findByEmail("leo@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken(user)).thenReturn("token-123");

        LoginResponse res = userService.loginUser(req);
        assertEquals(123L, res.getUserId());
        assertEquals("Leo", res.getDisplayName());
        assertEquals("USER", res.getRole());
        assertEquals("token-123", res.getAccessToken());
    }

    @Test
    void login_wrong_password_returns_401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("leo@example.com");
        req.setPassword("wrong");

        String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc12345");
        User user = new User("leo@example.com", hash, "Leo");
        setField(user, "userId", 123L);

        when(userRepository.findByEmail("leo@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationFailedException.class, () -> userService.loginUser(req));
    }

    @Test
    void login_missing_password_returns_400_password_required() {
        LoginRequest req = new LoginRequest();
        req.setEmail("leo@example.com");
        req.setPassword(" ");

        ValidationFailedException ex = assertThrows(ValidationFailedException.class, () -> userService.loginUser(req));
        assertEquals("PASSWORD_REQUIRED", ex.getCode());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
