package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class ApplyCouponRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String couponCode;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal cartTotal;

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public BigDecimal getCartTotal() { return cartTotal; }
    public void setCartTotal(BigDecimal cartTotal) { this.cartTotal = cartTotal; }
}