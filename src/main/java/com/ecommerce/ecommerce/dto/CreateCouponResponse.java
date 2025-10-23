package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateCouponResponse {

    private Long couponId;
    private String message;
    private String code;
    private BigDecimal discountPercent;
    private BigDecimal discountFixed;
    private Integer usageLimit;
    private LocalDateTime expiresAt;

    public CreateCouponResponse(Long couponId, String message, String code, BigDecimal discountPercent,
                                BigDecimal discountFixed, Integer usageLimit, LocalDateTime expiresAt) {
        this.couponId = couponId;
        this.message = message;
        this.code = code;
        this.discountPercent = discountPercent;
        this.discountFixed = discountFixed;
        this.usageLimit = usageLimit;
        this.expiresAt = expiresAt;
    }

    // Getters
    public Long getCouponId() { return couponId; }
    public String getMessage() { return message; }
    public String getCode() { return code; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getDiscountFixed() { return discountFixed; }
    public Integer getUsageLimit() { return usageLimit; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}