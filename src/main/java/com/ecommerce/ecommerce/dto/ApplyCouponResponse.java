package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class ApplyCouponResponse {

    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal adjustedTotal;
    private String couponCode;

    public ApplyCouponResponse(BigDecimal originalTotal, BigDecimal discountAmount, BigDecimal adjustedTotal, String couponCode) {
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.adjustedTotal = adjustedTotal;
        this.couponCode = couponCode;
    }

    // Getters and setters
    public BigDecimal getOriginalTotal() { return originalTotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getAdjustedTotal() { return adjustedTotal; }
    public String getCouponCode() { return couponCode; }
}