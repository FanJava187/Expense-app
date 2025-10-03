package com.example.expenseapp.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String token, Long expiresIn, UserInfo user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}