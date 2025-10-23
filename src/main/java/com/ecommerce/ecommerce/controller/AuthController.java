package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.LoginRequest;
import com.ecommerce.ecommerce.dto.LoginResponse;
import com.ecommerce.ecommerce.dto.PasswordResetRequest;
import com.ecommerce.ecommerce.dto.PasswordResetResponse;
import com.ecommerce.ecommerce.service.AuthService;
import com.ecommerce.ecommerce.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse resp = authService.login(request);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(new LoginResponse(false, ex.getMessage(), null, null));
        }
    }


    /**
     * POST /auth/reset-password
     * Initiate password reset flow by email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.initiateReset(request);
            return ResponseEntity.ok(new PasswordResetResponse(true, "Password reset token sent to email."));
        } catch (IllegalArgumentException ex) {
            // Email not found or validation fails
            return ResponseEntity.badRequest().body(new PasswordResetResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            // Unexpected server error
            return ResponseEntity.internalServerError().body(new PasswordResetResponse(false, "Failed to process password reset. Please try again later."));
        }

    }

    /**
     * POST /auth/logout
     *
     * Accepts Bearer token in Authorization header (preferred) OR invalidates HTTP session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            authService.logout(request);

            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "success", true,
                            "message", "Logout successful. Please login again to continue.",
                            "redirect", "/login"
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(
                    java.util.Map.of(
                            "success", false,
                            "message", ex.getMessage()
                    )
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(
                    java.util.Map.of(
                            "success", false,
                            "message", "An error occurred while processing logout."
                    )
            );
        }
    }
}