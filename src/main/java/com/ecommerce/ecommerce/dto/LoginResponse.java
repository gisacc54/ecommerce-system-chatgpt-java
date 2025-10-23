package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Login response payload.
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private Map<String, Object> user;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String token, Map<String, Object> user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // getters / setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }
}