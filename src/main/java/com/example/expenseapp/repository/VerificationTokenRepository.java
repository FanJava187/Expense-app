package com.example.expenseapp.repository;

import com.example.expenseapp.model.VerificationToken;
import com.example.expenseapp.model.VerificationToken.TokenType;
import com.example.expenseapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserAndTokenType(User user, TokenType tokenType);
    void deleteByExpiresAtBefore(LocalDateTime now);
    void deleteByUser(User user);
}