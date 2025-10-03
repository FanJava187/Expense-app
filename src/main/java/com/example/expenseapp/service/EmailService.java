package com.example.expenseapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String name, String token) {
        String verificationLink = frontendUrl + "/api/auth/verify?token=" + token;

        String subject = "請驗證您的 Expense App 帳號";
        String text = String.format(
                "親愛的 %s，\n\n" +
                        "感謝您註冊 Expense App！\n\n" +
                        "請點擊以下連結完成 Email 驗證：\n%s\n\n" +
                        "此連結將在 24 小時後失效。\n\n" +
                        "如果您沒有註冊此帳號，請忽略此信。\n\n" +
                        "祝您使用愉快！\n" +
                        "Expense App 團隊",
                name, verificationLink
        );

        sendEmail(to, subject, text);
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        String resetLink = frontendUrl + "/api/auth/reset-password?token=" + token;

        String subject = "重設您的 Expense App 密碼";
        String text = String.format(
                "親愛的 %s，\n\n" +
                        "我們收到您的密碼重設請求。\n\n" +
                        "請點擊以下連結重設密碼：\n%s\n\n" +
                        "此連結將在 1 小時後失效。\n\n" +
                        "如果您沒有提出此請求，請忽略此信，您的密碼不會被更改。\n\n" +
                        "Expense App 團隊",
                name, resetLink
        );

        sendEmail(to, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}