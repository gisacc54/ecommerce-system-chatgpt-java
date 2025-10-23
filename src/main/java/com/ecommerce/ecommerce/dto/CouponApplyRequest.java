package com.ecommerce.ecommerce.dto;

import java.util.List;

public class CouponApplyRequest {
    private Long userId;
    private String couponCode;
    private List<CartItemDto> cartItems; // Optional

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public List<CartItemDto> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDto> cartItems) { this.cartItems = cartItems; }
}