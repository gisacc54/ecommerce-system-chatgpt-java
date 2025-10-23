// CartAddResponseDto.java
package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartAddResponseDto {
    private List<CartItemDto> cartItems;
    private BigDecimal totalTZS;
    private List<CartItemDto> recommendedProducts;

    // Getters and setters
    public List<CartItemDto> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDto> cartItems) { this.cartItems = cartItems; }

    public BigDecimal getTotalTZS() { return totalTZS; }
    public void setTotalTZS(BigDecimal totalTZS) { this.totalTZS = totalTZS; }

    public List<CartItemDto> getRecommendedProducts() { return recommendedProducts; }
    public void setRecommendedProducts(List<CartItemDto> recommendedProducts) { this.recommendedProducts = recommendedProducts; }
}