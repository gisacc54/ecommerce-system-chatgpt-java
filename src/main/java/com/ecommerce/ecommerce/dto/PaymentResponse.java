package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long orderId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paymentTimestamp;

    public PaymentResponse(Long orderId, BigDecimal amount, String status, LocalDateTime paymentTimestamp) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.paymentTimestamp = paymentTimestamp;
    }

    // Getters
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getPaymentTimestamp() { return paymentTimestamp; }
}