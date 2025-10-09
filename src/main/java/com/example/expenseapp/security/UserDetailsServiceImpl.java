package com.example.expenseapp.security;

import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者：" + usernameOrEmail));

        // 處理 OAuth 使用者（沒有密碼）
        String password = user.getPassword() != null && !user.getPassword().isEmpty()
                ? user.getPassword()
                : "";

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                new ArrayList<>()
        );
    }
}