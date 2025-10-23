package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class PlaceOrderResponse {
    private Long orderId;
    private BigDecimal totalAmount;
    private String status;

    public PlaceOrderResponse(Long orderId, BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters
    public Long getOrderId() { return orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}