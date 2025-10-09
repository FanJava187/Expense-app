package com.example.expenseapp.security;

import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // 取得使用者資訊
        String googleId = oauth2User.getAttribute("sub");

        // 從資料庫取得使用者
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 產生 JWT Token
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        "",
                        java.util.Collections.emptyList()
                );
        String token = jwtService.generateToken(userDetails);

        // URL 編碼所有參數（支援中文）
        String encodedUsername = URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        String encodedName = URLEncoder.encode(user.getName(), StandardCharsets.UTF_8);

        // 重定向到測試頁面，帶上 Token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2-test.html")
                .queryParam("token", token)
                .queryParam("username", encodedUsername)
                .queryParam("email", encodedEmail)
                .queryParam("name", encodedName)
                .build(true)  // 不要再次編碼
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}