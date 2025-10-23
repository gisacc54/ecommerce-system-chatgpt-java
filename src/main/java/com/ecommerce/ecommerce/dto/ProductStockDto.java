package com.ecommerce.ecommerce.dto;

public class ProductStockDto {
    private Long productId;
    private Integer stockQuantity;
    private Boolean available;

    public ProductStockDto(Long productId, Integer stockQuantity, Boolean available) {
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.available = available;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Boolean getAvailable() {
        return available;
    }
}