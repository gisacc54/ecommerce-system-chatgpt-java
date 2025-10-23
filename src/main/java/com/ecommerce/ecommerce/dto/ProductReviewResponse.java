package com.ecommerce.ecommerce.dto;

// Response DTO
public class ProductReviewResponse {
    private String message;
    private Long reviewId;

    public ProductReviewResponse(String message, Long reviewId) {
        this.message = message;
        this.reviewId = reviewId;
    }

    // Getters
    public String getMessage() { return message; }
    public Long getReviewId() { return reviewId; }
}
