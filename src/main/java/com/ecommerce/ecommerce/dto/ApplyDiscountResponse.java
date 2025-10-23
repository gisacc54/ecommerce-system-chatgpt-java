package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class ApplyDiscountResponse {
    private BigDecimal originalAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountedAmount;
    private String message;

    public ApplyDiscountResponse(BigDecimal originalAmount, BigDecimal discountPercent, BigDecimal discountedAmount, String message) {
        this.originalAmount = originalAmount;
        this.discountPercent = discountPercent;
        this.discountedAmount = discountedAmount;
        this.message = message;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getDiscountedAmount() {
        return discountedAmount;
    }

    public String getMessage() {
        return message;
    }
}