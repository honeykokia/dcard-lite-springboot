package org.example.demo.user.service;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.example.demo.user.dto.LoginRequest;
import org.example.demo.user.dto.LoginResponse;
import org.example.demo.user.dto.RegisterUserRequest;
import org.example.demo.user.dto.RegisterUserResponse;
import org.example.demo.user.entity.User;
import org.example.demo.user.exception.AuthenticationFailedException;
import org.example.demo.user.exception.EmailAlreadyExistsException;
import org.example.demo.user.exception.ValidationFailedException;
import org.example.demo.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final NormalizerService normalizerService;

    public UserService(UserRepository userRepository, JwtTokenService jwtTokenService, Validator validator, NormalizerService normalizerService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.validator = validator;
        this.normalizerService = normalizerService;
    }

    @Transactional
    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        // RP-001: trim inputs before validating/persisting
        if (request != null) {
            request.setEmail(normalizerService.normalizeEmail(request.getEmail()));
            request.setName(normalizerService.normalizeDisplayName(request.getName()));
        }
        validateOrThrow(request);
        String normalizedEmail = request.getEmail();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        String displayName = request.getName();
        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user = new User(normalizedEmail, passwordHash, displayName);
        User saved = userRepository.save(user);

        return new RegisterUserResponse(
                saved.getUserId(),
                saved.getDisplayName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse loginUser(LoginRequest request) {
        // RP-001: trim email before validating/querying
        if (request != null) {
            request.setEmail(normalizerService.normalizeEmail(request.getEmail()));
        }
        validateOrThrow(request);
        String normalizedEmail = request.getEmail();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(AuthenticationFailedException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException();
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        return new LoginResponse(user.getUserId(), user.getDisplayName(), user.getRole().name(), accessToken);
    }

    private void validateOrThrow(Object request) {
        if (request == null) {
            throw new ValidationFailedException("VALIDATION_FAILED");
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }

        String code = mapViolationToCode(violations.iterator());
        throw new ValidationFailedException(code);
    }

    private String mapViolationToCode(Iterator<ConstraintViolation<Object>> it) {
        ConstraintViolation<Object> v = it.next();
        String annotation = v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();

        if ("PasswordMatches".equals(annotation)) {
            return "INVALID_CONFIRM_PASSWORD";
        }

        String normalizedField = normalizeFieldForCode(field);

        if ("NotBlank".equals(annotation) || "NotNull".equals(annotation)) {
            return normalizedField + "_REQUIRED";
        }
        if ("Email".equals(annotation)) {
            return "INVALID_EMAIL";
        }

        return "INVALID_" + normalizedField;
    }

    private String normalizeFieldForCode(String field) {
        if (field == null || field.isBlank()) {
            return "UNKNOWN";
        }
        return field
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toUpperCase(Locale.ROOT);
    }
}
