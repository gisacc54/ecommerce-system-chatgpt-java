// com.ecommerce.ecommerce.dto.PaymentDto.java
package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {
    private Long id;
    private BigDecimal amount;
    private String method;
    private LocalDateTime paidAt;

    public PaymentDto(Long id, BigDecimal amount, String method, LocalDateTime paidAt) {
        this.id = id;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }

    public PaymentDto() {}
    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}