package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ApplyDiscountRequest {

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.0", message = "Discount percent must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percent cannot exceed 100")
    private BigDecimal discountPercent;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }
}