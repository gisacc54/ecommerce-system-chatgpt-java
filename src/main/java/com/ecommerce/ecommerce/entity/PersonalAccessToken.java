package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "personal_access_tokens")
public class PersonalAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tokenableType;

    @Column(nullable = false)
    private Long tokenableId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(length = 64, nullable = false, unique = true)
    private String token;

    @Column(columnDefinition = "TEXT")
    private String abilities;

    @Column
    private LocalDateTime lastUsedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
