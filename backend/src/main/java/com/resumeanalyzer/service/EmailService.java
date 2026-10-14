package com.resumeanalyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendPasswordResetEmail(String recipientEmail, String resetToken) {
        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        if (mailUsername == null || mailUsername.isBlank() || mailUsername.startsWith("${")) {
            // SMTP is not configured
            logger.warn("===============================================================================");
            logger.warn("SMTP settings are not configured. Development Password Reset Link generated:");
            logger.warn("To: {}", recipientEmail);
            logger.warn("Reset Link: {}", resetLink);
            logger.warn("Configure SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD to send live SMTP emails.");
            logger.warn("===============================================================================");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(recipientEmail);
            message.setSubject("Password Reset Request - AI Resume Analyzer");
            message.setText("Hello,\n\nYou requested to reset your password.\n" +
                    "Click the link below to set a new password:\n" + resetLink +
                    "\n\nThis link will expire in 30 minutes.\n\nBest regards,\nAI Resume Analyzer Team");

            mailSender.send(message);
            logger.info("Password reset email sent to {}", recipientEmail);
            return true;
        } catch (Exception ex) {
            logger.error("Failed to send password reset email via SMTP: {}", ex.getMessage());
            return false;
        }
    }
}
