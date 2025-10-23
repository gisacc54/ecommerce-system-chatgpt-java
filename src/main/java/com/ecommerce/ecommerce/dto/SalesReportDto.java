package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class SalesReportDto {
    private Long totalOrders;
    private BigDecimal totalSalesAmount;

    public SalesReportDto(Long totalOrders, BigDecimal totalSalesAmount) {
        this.totalOrders = totalOrders;
        this.totalSalesAmount = totalSalesAmount;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(BigDecimal totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }
}