package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CouponApplyResponse {
    private BigDecimal adjustedTotalTZS;
    private List<Map<String, Object>> discountDetails;
    private boolean valid;

    // Getters and Setters
    public BigDecimal getAdjustedTotalTZS() { return adjustedTotalTZS; }
    public void setAdjustedTotalTZS(BigDecimal adjustedTotalTZS) { this.adjustedTotalTZS = adjustedTotalTZS; }

    public List<Map<String, Object>> getDiscountDetails() { return discountDetails; }
    public void setDiscountDetails(List<Map<String, Object>> discountDetails) { this.discountDetails = discountDetails; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}