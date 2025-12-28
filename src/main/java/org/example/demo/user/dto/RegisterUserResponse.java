package org.example.demo.user.dto;

import java.time.Instant;

public class RegisterUserResponse {

    private Long userId;
    private String displayName;
    private String email;
    private String role;
    private Instant createdAt;

    public RegisterUserResponse() {
    }

    public RegisterUserResponse(Long userId, String displayName, String email, String role, Instant createdAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

