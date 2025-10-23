package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.LoginRequest;
import com.ecommerce.ecommerce.dto.LoginResponse;
import com.ecommerce.ecommerce.entity.BlacklistedToken;
import com.ecommerce.ecommerce.entity.SessionToken;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.BlacklistedTokenRepository;
import com.ecommerce.ecommerce.repository.SessionTokenRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * AuthService: authenticate credentials, generate JWT, persist session token.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SessionTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final BlacklistedTokenRepository blacklistRepo;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       SessionTokenRepository tokenRepository,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, BlacklistedTokenRepository blacklistRepo) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.blacklistRepo = blacklistRepo;
    }

    @Transactional
    public LoginResponse login(LoginRequest req) {
        // 1. Authenticate using AuthenticationManager (this checks BCrypt password)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // 2. Load user entity
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // 3. Generate JWT token
        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail());

        // 4. Persist token in sessions_tokens table with expiration
        LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Africa/Dar_es_Salaam"));
        LocalDateTime expiresAt = createdAt.plusDays(7); // match JwtUtil expiration
        SessionToken st = new SessionToken(user, createdAt,expiresAt,jwt);
        tokenRepository.save(st);

        // 5. Prepare user map to return
        Map<String, Object> userMap = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "region", user.getRegion(),
                "balance", user.getBalance(),
                "role", user.getRole()
        );

        // 6. Return response DTO
        return new LoginResponse(true, "Login successful", jwt, userMap);
    }

    /**
     * Logout logic:
     * - If request has Bearer token: validate it, persist to blacklist using its expiry
     * - If no Bearer token: fallback to session invalidation (session-based auth)
     *
     * @param request HttpServletRequest to read Authorization header & session
     * @return message summary (throw exception for invalid token)
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // 1) If Bearer token is present: treat as JWT-based logout
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Missing token");
            }

            // a) Validate token structure and signature (JwtUtil should throw if invalid)
            if (!jwtUtil.validate(token)) {
                throw new IllegalArgumentException("Invalid or expired token.");
            }

            // b) Check whether token already blacklisted
            if (blacklistRepo.existsByToken(token)) {
                // Already invalidated
                throw new IllegalArgumentException("Token already invalidated.");
            }

            // c) Determine token expiration time via JwtUtil
            LocalDateTime expiresAt = jwtUtil.getExpirationDateTime(token);

            // d) Persist token in jwt_blacklist with expiry date
            BlacklistedToken blacklisted = new BlacklistedToken(token, expiresAt, LocalDateTime.now(ZoneId.of("Africa/Dar_es_Salaam")));
            blacklistRepo.save(blacklisted);

            // e) Optionally remove a persisted session token if you also store the JWT in sessions_tokens
            // sessionTokenRepository.deleteByToken(token); // implement this method if used

            return; // JWT logout done
        }

        // 2) No Bearer token: fallback to session invalidation (session-based auth)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // invalidate the session to logout user
            session.invalidate();
            return;
        }

        // 3) Neither token nor session => cannot logout
        throw new IllegalArgumentException("No token or active session found.");
    }

    /**
     * Helper for JWT filter to check if a token is blacklisted.
     * The JwtAuthFilter should call this method when authenticating a token.
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        return blacklistRepo.existsByToken(token);
    }
}