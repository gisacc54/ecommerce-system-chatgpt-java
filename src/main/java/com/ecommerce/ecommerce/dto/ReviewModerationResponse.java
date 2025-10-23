package com.ecommerce.ecommerce.dto;

public class ReviewModerationResponse {
    private Long reviewId;
    private String newStatus;

    public ReviewModerationResponse(Long reviewId, String newStatus) {
        this.reviewId = reviewId;
        this.newStatus = newStatus;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}