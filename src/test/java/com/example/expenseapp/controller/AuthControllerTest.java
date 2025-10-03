package com.example.expenseapp.controller;

import com.example.expenseapp.dto.*;
import com.example.expenseapp.model.User;
import com.example.expenseapp.model.VerificationToken;
import com.example.expenseapp.repository.UserRepository;
import com.example.expenseapp.repository.VerificationTokenRepository;
import com.example.expenseapp.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean  // Mock EmailService
    private EmailService emailService;

    @BeforeEach
    void setup() {
        // 清空測試資料
        tokenRepository.deleteAll();
        userRepository.deleteAll();

        // Mock EmailService 的方法
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // ========== 註冊測試 ==========

    @Test
    @DisplayName("測試註冊 - 成功")
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "john_doe",
                "john@example.com",
                "password123",
                "John Doe"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(containsString("註冊成功")));
    }

    @Test
    @DisplayName("測試註冊 - 帳號已存在")
    void testRegister_UsernameExists() throws Exception {
        // 先建立使用者
        User existingUser = new User("john_doe", "john@example.com", "password", "John");
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "john_doe",
                "different@example.com",
                "password123",
                "John Doe"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("帳號已被使用"));
    }

    @Test
    @DisplayName("測試註冊 - Email 已存在")
    void testRegister_EmailExists() throws Exception {
        User existingUser = new User("existing_user", "john@example.com", "password", "John");
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "john_doe",
                "john@example.com",
                "password123",
                "John Doe"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email 已被註冊"));
    }

    @Test
    @DisplayName("測試註冊 - 驗證失敗（密碼太短）")
    void testRegister_ValidationFailed() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "john_doe",
                "john@example.com",
                "123",  // 太短
                "John Doe"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    // ========== 登入測試 ==========

    @Test
    @DisplayName("測試登入 - 成功")
    void testLogin_Success() throws Exception {
        // 建立已驗證的使用者
        User user = new User("john_doe", "john@example.com",
                passwordEncoder.encode("password123"), "John Doe");
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("john_doe", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("john_doe"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("測試登入 - 帳號未驗證")
    void testLogin_AccountNotVerified() throws Exception {
        // 建立未驗證的使用者
        User user = new User("john_doe", "john@example.com",
                passwordEncoder.encode("password123"), "John Doe");
        user.setStatus(User.UserStatus.UNVERIFIED);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("john_doe", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Email 驗證")));
    }

    @Test
    @DisplayName("測試登入 - 密碼錯誤")
    void testLogin_WrongPassword() throws Exception {
        User user = new User("john_doe", "john@example.com",
                passwordEncoder.encode("password123"), "John Doe");
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("john_doe", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("帳號或密碼錯誤")));
    }

    @Test
    @DisplayName("測試登入 - 帳號不存在")
    void testLogin_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("帳號或密碼錯誤")));
    }

    // ========== Email 驗證測試 ==========

    @Test
    @DisplayName("測試 Email 驗證 - 成功")
    void testVerifyEmail_Success() throws Exception {
        // 建立使用者
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user.setStatus(User.UserStatus.UNVERIFIED);
        user = userRepository.save(user);

        // 建立驗證 Token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                user, token, VerificationToken.TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(24)
        );
        tokenRepository.save(verificationToken);

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("驗證成功")));

        // 確認使用者狀態已更新
        User verifiedUser = userRepository.findById(user.getId()).get();
        assert verifiedUser.getStatus() == User.UserStatus.ACTIVE;
    }

    @Test
    @DisplayName("測試 Email 驗證 - Token 無效")
    void testVerifyEmail_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("無效")));
    }

    @Test
    @DisplayName("測試 Email 驗證 - Token 已過期")
    void testVerifyEmail_ExpiredToken() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                user, token, VerificationToken.TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().minusHours(1)  // 過期的 Token
        );
        tokenRepository.save(verificationToken);

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("過期")));
    }

    @Test
    @DisplayName("測試 Email 驗證 - Token 已使用")
    void testVerifyEmail_UsedToken() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                user, token, VerificationToken.TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(24)
        );
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("已被使用")));
    }

    // ========== 重新發送驗證信測試 ==========

    @Test
    @DisplayName("測試重新發送驗證信 - 成功")
    void testResendVerification_Success() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user.setStatus(User.UserStatus.UNVERIFIED);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("已重新發送")));
    }

    @Test
    @DisplayName("測試重新發送驗證信 - Email 不存在")
    void testResendVerification_EmailNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("找不到")));
    }

    @Test
    @DisplayName("測試重新發送驗證信 - 帳號已驗證")
    void testResendVerification_AlreadyVerified() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", "john@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(containsString("已經驗證")));
    }

    // ========== 忘記密碼測試 ==========

    @Test
    @DisplayName("測試忘記密碼 - 成功")
    void testForgotPassword_Success() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        userRepository.save(user);

        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("已發送")));
    }

    @Test
    @DisplayName("測試忘記密碼 - Email 不存在")
    void testForgotPassword_EmailNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("找不到")));
    }

    // ========== 重設密碼測試 ==========

    @Test
    @DisplayName("測試重設密碼 - 成功")
    void testResetPassword_Success() throws Exception {
        User user = new User("john_doe", "john@example.com",
                passwordEncoder.encode("oldpassword"), "John Doe");
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken(
                user, token, VerificationToken.TokenType.PASSWORD_RESET,
                LocalDateTime.now().plusHours(1)
        );
        tokenRepository.save(resetToken);

        ResetPasswordRequest request = new ResetPasswordRequest(token, "newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("已成功重設")));

        // 驗證密碼已更新
        User updatedUser = userRepository.findById(user.getId()).get();
        assert passwordEncoder.matches("newpassword123", updatedUser.getPassword());
    }

    @Test
    @DisplayName("測試重設密碼 - Token 無效")
    void testResetPassword_InvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("無效")));
    }

    @Test
    @DisplayName("測試重設密碼 - Token 已過期")
    void testResetPassword_ExpiredToken() throws Exception {
        User user = new User("john_doe", "john@example.com", "password", "John Doe");
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken(
                user, token, VerificationToken.TokenType.PASSWORD_RESET,
                LocalDateTime.now().minusHours(1)  // 過期
        );
        tokenRepository.save(resetToken);

        ResetPasswordRequest request = new ResetPasswordRequest(token, "newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("過期")));
    }
}