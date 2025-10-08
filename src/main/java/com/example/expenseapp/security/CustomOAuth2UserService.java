package com.example.expenseapp.security;

import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 處理使用者資訊
        processOAuth2User(userRequest, oauth2User);

        return oauth2User;
    }

    private void processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 目前只支援 Google
        if (!"google".equals(registrationId)) {
            throw new OAuth2AuthenticationException("不支援的 OAuth2 提供者：" + registrationId);
        }

        // 取得 Google 使用者資訊
        Map<String, Object> attributes = oauth2User.getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        // 檢查使用者是否已存在
        Optional<User> userOptional = userRepository.findByGoogleId(googleId);

        User user;
        if (userOptional.isPresent()) {
            // 已存在的使用者，更新資訊
            user = userOptional.get();
            user.setName(name);
            user.setAvatarUrl(picture);
        } else {
            // 新使用者，建立帳號
            // 檢查 Email 是否已被其他方式註冊
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                // Email 已存在，連結到現有帳號
                user = existingUser.get();
                user.setGoogleId(googleId);
                user.setAvatarUrl(picture);
            } else {
                // 全新使用者
                user = new User();
                user.setUsername(email.split("@")[0] + "_" + googleId.substring(0, 6));
                user.setEmail(email);
                user.setName(name);
                user.setGoogleId(googleId);
                user.setProvider("google");
                user.setAvatarUrl(picture);
                user.setStatus(User.UserStatus.ACTIVE); // Google 使用者直接啟用
                user.setPassword(""); // OAuth 使用者不需要密碼
            }
        }

        userRepository.save(user);
    }
}