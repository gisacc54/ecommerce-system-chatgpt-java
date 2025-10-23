package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @DecimalMin(value = "0.0", message = "Discount percent must be >= 0")
    @DecimalMax(value = "100.0", message = "Discount percent must be <= 100")
    private BigDecimal discountPercent;

    @DecimalMin(value = "0.0", message = "Fixed discount must be >= 0")
    private BigDecimal discountFixed;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @NotNull(message = "Expiration date is required")
    private LocalDateTime expiresAt;

    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getDiscountFixed() { return discountFixed; }
    public void setDiscountFixed(BigDecimal discountFixed) { this.discountFixed = discountFixed; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}