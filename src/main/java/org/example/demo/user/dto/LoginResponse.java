package org.example.demo.user.dto;

public class LoginResponse {

    private Long userId;
    private String displayName;
    private String role;
    private String accessToken;

    public LoginResponse() {
    }

    public LoginResponse(Long userId, String displayName, String role, String accessToken) {
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
        this.accessToken = accessToken;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

