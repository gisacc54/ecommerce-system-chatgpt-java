// CartItemDto.java
package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal priceTZS;

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceTZS() { return priceTZS; }
    public void setPriceTZS(BigDecimal priceTZS) { this.priceTZS = priceTZS; }
}