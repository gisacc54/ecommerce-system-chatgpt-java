// com.ecommerce.ecommerce.dto.ComprehensiveOrderDto.java
package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ComprehensiveOrderDto {
    private Long orderId;
    private BigDecimal totalAmount; // in TZS
    private String status;
    private LocalDateTime createdAt;
    private String shippingAddress;

    private List<OrderItemDto> items;
    private List<PaymentDto> payments;
    private List<ReviewDto> reviews;

    // getters/setters...
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
    public List<PaymentDto> getPayments() { return payments; }
    public void setPayments(List<PaymentDto> payments) { this.payments = payments; }
    public List<ReviewDto> getReviews() { return reviews; }
    public void setReviews(List<ReviewDto> reviews) { this.reviews = reviews; }
}