package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class CartTotalResponse {

    private BigDecimal cartTotal;
    private int itemCount;
    private BigDecimal discountApplied;

    public CartTotalResponse(BigDecimal cartTotal, int itemCount, BigDecimal discountApplied) {
        this.cartTotal = cartTotal;
        this.itemCount = itemCount;
        this.discountApplied = discountApplied;
    }

    // Getters
    public BigDecimal getCartTotal() { return cartTotal; }
    public int getItemCount() { return itemCount; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
}