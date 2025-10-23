package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.PasswordResetRequest;
import com.ecommerce.ecommerce.entity.PasswordReset;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.PasswordResetRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

/**
 * Service responsible for handling password reset requests:
 * - validate email exists
 * - generate secure token
 * - save token with expiry (60 minutes)
 * - send email with token
 */
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    // Expiry minutes for reset token
    private static final long EXPIRY_MINUTES = 60;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetRepository passwordResetRepository,
                                JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.mailSender = mailSender;
    }

    /**
     * Initiates password reset flow for the given request.
     *
     * @param request DTO containing email
     * @throws IllegalArgumentException if email not found
     */
    @Transactional
    public void initiateReset(PasswordResetRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1) Verify user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal too much in production; here we follow your requirement to return error when not found
            throw new IllegalArgumentException("Email address not found");
        }

        User user = userOpt.get();

        // 2) Generate secure token (URL-safe Base64)
        String token = generateSecureToken();

        // 3) Calculate expiry (60 minutes from now, using Tanzania timezone)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Africa/Dar_es_Salaam"));
        LocalDateTime expiresAt = now.plusMinutes(EXPIRY_MINUTES);

        // 4) Persist token in password_resets table
        PasswordReset pr = new PasswordReset(email, token, expiresAt, now);
        passwordResetRepository.save(pr);

        // 5) Send email to the user with the reset token (or link)
        sendResetEmail(user.getEmail(), user.getName(), token);
    }

    /**
     * Generate a secure URL-safe token using SecureRandom and Base64.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[48]; // 48 bytes -> 64 chars base64
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Send the reset email. In production you'd send a link like:
     * https://your-frontend.tz/reset-password?token=...
     */
    private void sendResetEmail(String toEmail, String name, String token) {
        // Build reset link (adjust your frontend domain)
        String resetLink = String.format("https://your-frontend.example/reset-password?token=%s", token);

        String subject = "Password Reset Request";
        String text = String.format("Hello %s,%n%n" +
                        "We received a request to reset your password. Click the link below to reset it (valid for %d minutes):%n%n" +
                        "%s%n%n" +
                        "If you didn't request a password reset, please ignore this email.%n%n" +
                        "Asante,%nTimu ya %s",
                (name == null || name.isBlank()) ? "Mteja" : name,
                EXPIRY_MINUTES,
                resetLink,
                "E-Commerce");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);
        // from address will be taken from spring.mail.username if configured
        mailSender.send(message);
    }
}