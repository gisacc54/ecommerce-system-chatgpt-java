package com.ecommerce.ecommerce.dto;

public class CancelOrderResponse {

    private Long orderId;
    private String status;
    private boolean refundProcessed;

    // Getters and Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRefundProcessed() {
        return refundProcessed;
    }

    public void setRefundProcessed(boolean refundProcessed) {
        this.refundProcessed = refundProcessed;
    }
}