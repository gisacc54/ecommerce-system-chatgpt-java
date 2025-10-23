package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReviewModerationRequest {

    // Status must be either 'approved' or 'rejected'
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "approved|rejected", message = "Status must be 'approved' or 'rejected'")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}