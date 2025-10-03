package com.example.expenseapp.dto;

public class UserInfo {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String status;

    // Constructors
    public UserInfo() {
    }

    public UserInfo(Long id, String username, String email, String name, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}