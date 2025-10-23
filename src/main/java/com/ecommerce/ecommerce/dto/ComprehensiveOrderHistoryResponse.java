package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class ComprehensiveOrderHistoryResponse {
    private BigDecimal totalSpentTZS;
    private Long totalOrders;
    private BigDecimal averageOrderValueTZS;
    private List<OrderHistoryResponse> orders;

    public ComprehensiveOrderHistoryResponse(BigDecimal totalSpentTZS, Long totalOrders, BigDecimal averageOrderValueTZS, List<OrderHistoryResponse> orders) {
        this.totalSpentTZS = totalSpentTZS;
        this.totalOrders = totalOrders;
        this.averageOrderValueTZS = averageOrderValueTZS;
        this.orders = orders;
    }

    public BigDecimal getTotalSpentTZS() {
        return totalSpentTZS;
    }

    public void setTotalSpentTZS(BigDecimal totalSpentTZS) {
        this.totalSpentTZS = totalSpentTZS;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAverageOrderValueTZS() {
        return averageOrderValueTZS;
    }

    public void setAverageOrderValueTZS(BigDecimal averageOrderValueTZS) {
        this.averageOrderValueTZS = averageOrderValueTZS;
    }

    public List<OrderHistoryResponse> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderHistoryResponse> orders) {
        this.orders = orders;
    }
}
