package com.example.expenseapp.service;

import com.example.expenseapp.dto.*;
import com.example.expenseapp.exception.*;
import com.example.expenseapp.model.User;
import com.example.expenseapp.model.User.UserStatus;
import com.example.expenseapp.model.VerificationToken;
import com.example.expenseapp.model.VerificationToken.TokenType;
import com.example.expenseapp.repository.UserRepository;
import com.example.expenseapp.repository.VerificationTokenRepository;
import com.example.expenseapp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.token.email-verification.expiration}")
    private Long emailVerificationExpiration;

    @Value("${app.token.password-reset.expiration}")
    private Long passwordResetExpiration;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // 檢查帳號是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("帳號已被使用");
        }

        // 檢查 Email 是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email 已被註冊");
        }

        // 手動驗證密碼（因為 User entity 移除了 @NotBlank）
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密碼不能為空");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("密碼長度至少 8 個字元");
        }

        // 建立新使用者
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setStatus(UserStatus.UNVERIFIED);
        user.setProvider("local");

        user = userRepository.save(user);

        // 產生驗證 Token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(emailVerificationExpiration / 1000);

        VerificationToken verificationToken = new VerificationToken(
                user, token, TokenType.EMAIL_VERIFICATION, expiresAt
        );
        tokenRepository.save(verificationToken);

        // 發送驗證信
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);

        return new MessageResponse("註冊成功！請檢查您的 Email 完成驗證");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 查找使用者
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException("帳號或密碼錯誤"));

        // 驗證密碼
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        // 檢查帳號狀態
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new AccountNotVerifiedException("請先完成 Email 驗證");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountSuspendedException("您的帳號已被停權，請聯繫客服");
        }

        // 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 產生 JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getStatus().toString()
        );

        return new AuthResponse(token, jwtService.getExpirationTime(), userInfo);
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("無效的驗證連結"));

        // 檢查 Token 是否已使用
        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("此驗證連結已被使用");
        }

        // 檢查 Token 是否過期
        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("驗證連結已過期，請重新發送驗證信");
        }

        // 檢查 Token 類型
        if (verificationToken.getTokenType() != TokenType.EMAIL_VERIFICATION) {
            throw new InvalidTokenException("無效的驗證類型");
        }

        // 啟用使用者帳號
        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // 標記 Token 為已使用
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        return new MessageResponse("Email 驗證成功！您現在可以登入了");
    }

    @Transactional
    public MessageResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("找不到此 Email 的使用者"));

        // 檢查帳號狀態
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("您的帳號已經驗證過了");
        }

        // 刪除舊的驗證 Token
        tokenRepository.deleteByUser(user);

        // 產生新的驗證 Token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(emailVerificationExpiration / 1000);

        VerificationToken verificationToken = new VerificationToken(
                user, token, TokenType.EMAIL_VERIFICATION, expiresAt
        );
        tokenRepository.save(verificationToken);

        // 發送驗證信
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);

        return new MessageResponse("驗證信已重新發送，請檢查您的 Email");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("找不到此 Email 的使用者"));

        // 產生密碼重設 Token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(passwordResetExpiration / 1000);

        VerificationToken resetToken = new VerificationToken(
                user, token, TokenType.PASSWORD_RESET, expiresAt
        );
        tokenRepository.save(resetToken);

        // 發送密碼重設信
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);

        return new MessageResponse("密碼重設信已發送，請檢查您的 Email");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        VerificationToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("無效的重設連結"));

        // 檢查 Token 是否已使用
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("此重設連結已被使用");
        }

        // 檢查 Token 是否過期
        if (resetToken.isExpired()) {
            throw new TokenExpiredException("重設連結已過期，請重新申請");
        }

        // 檢查 Token 類型
        if (resetToken.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new InvalidTokenException("無效的重設類型");
        }

        // 更新密碼
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 標記 Token 為已使用
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        return new MessageResponse("密碼已成功重設，請使用新密碼登入");
    }
}