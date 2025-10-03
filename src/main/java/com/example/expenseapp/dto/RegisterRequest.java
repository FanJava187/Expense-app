package com.example.expenseapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "帳號不能為空")
    @Size(min = 3, max = 50, message = "帳號長度必須在 3-50 字元之間")
    private String username;

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, message = "密碼長度至少 8 個字元")
    private String password;

    @NotBlank(message = "姓名不能為空")
    @Size(max = 100, message = "姓名長度不能超過 100 字元")
    private String name;

    // Constructors
    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password, String name) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}