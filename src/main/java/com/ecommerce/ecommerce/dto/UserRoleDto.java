package com.ecommerce.ecommerce.dto;

public class UserRoleDto {
    private Long userId;
    private String role;

    public UserRoleDto(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}