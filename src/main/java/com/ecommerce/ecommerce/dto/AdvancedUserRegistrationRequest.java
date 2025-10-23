package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdvancedUserRegistrationRequest {

    @NotBlank(message = "Name is required")
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern.List({
            @Pattern(regexp = ".*[A-Z].*", message = "Password must contain an uppercase letter"),
            @Pattern(regexp = ".*[a-z].*", message = "Password must contain a lowercase letter"),
            @Pattern(regexp = ".*\\d.*", message = "Password must contain a number"),
            @Pattern(regexp = ".*[!@#$%^&*].*", message = "Password must contain a special character")
    })
    private String password;

    @NotBlank(message = "Region is required")
    private String region;

    @NotBlank(message = "CAPTCHA token is required")
    private String captchaToken;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    // Getters & Setters
}