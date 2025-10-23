// com.ecommerce.ecommerce.dto.OrderFilterDto.java
package com.ecommerce.ecommerce.dto;

import java.time.LocalDate;

public class OrderFilterDto {
    // Optional start and end dates (yyyy-MM-dd)
    private LocalDate startDate;
    private LocalDate endDate;

    // Optional status filter ("PENDING", "PAID", "CANCELLED", etc)
    private String status;

    // getters / setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}