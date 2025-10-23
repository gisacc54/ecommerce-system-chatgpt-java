package com.ecommerce.ecommerce.dto;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String region;
    private LocalDateTime createdAt;

    public UserDto(Long id, String name, String email, String region, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.region = region;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRegion() {
        return region;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}