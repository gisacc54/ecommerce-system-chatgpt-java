package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a blacklisted JWT token.
 * When a user logs out, we persist the JWT here until it expires.
 */
@Entity
@Table(name = "jwt_blacklist")
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The full JWT string (or a hashed fingerprint for better security)
    @Column(nullable = false, unique = true, length = 1024)
    private String token;

    // When the token naturally expires - we can delete the blacklist row after this
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public BlacklistedToken() {}

    public BlacklistedToken(String token, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}