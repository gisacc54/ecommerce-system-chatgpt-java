package com.ecommerce.ecommerce.dto;

/**
 * DTO returned to client after successful registration.
 */
public class RegisterResponse {
    private boolean success;
    private Long userId;
    private String message;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, Long userId, String message) {
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    // Getters / Setters
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}