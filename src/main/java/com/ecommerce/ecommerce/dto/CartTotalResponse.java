package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartTotalResponse {

    private BigDecimal cartTotal;
    private int itemCount;
    private BigDecimal discountApplied;

    private List<CartItemDetail> items;
    private BigDecimal subtotalTZS;
    private BigDecimal discountTZS;
    private BigDecimal taxTZS;
    private BigDecimal shippingTZS;
    private BigDecimal totalTZS;

    public CartTotalResponse(BigDecimal cartTotal, int itemCount, BigDecimal discountApplied) {
        this.cartTotal = cartTotal;
        this.itemCount = itemCount;
        this.discountApplied = discountApplied;
    }

    public CartTotalResponse() {}

    // Getters
    public BigDecimal getCartTotal() { return cartTotal; }
    public int getItemCount() { return itemCount; }
    public BigDecimal getDiscountApplied() { return discountApplied; }

    // Getters and Setters
    public List<CartItemDetail> getItems() { return items; }
    public void setItems(List<CartItemDetail> items) { this.items = items; }

    public BigDecimal getSubtotalTZS() { return subtotalTZS; }
    public void setSubtotalTZS(BigDecimal subtotalTZS) { this.subtotalTZS = subtotalTZS; }

    public BigDecimal getDiscountTZS() { return discountTZS; }
    public void setDiscountTZS(BigDecimal discountTZS) { this.discountTZS = discountTZS; }

    public BigDecimal getTaxTZS() { return taxTZS; }
    public void setTaxTZS(BigDecimal taxTZS) { this.taxTZS = taxTZS; }

    public BigDecimal getShippingTZS() { return shippingTZS; }
    public void setShippingTZS(BigDecimal shippingTZS) { this.shippingTZS = shippingTZS; }

    public BigDecimal getTotalTZS() { return totalTZS; }
    public void setTotalTZS(BigDecimal totalTZS) { this.totalTZS = totalTZS; }

    public static class CartItemDetail {
        private Long productId;
        private String productName;
        private BigDecimal priceTZS;
        private Integer quantity;
        private BigDecimal totalTZS;

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getPriceTZS() { return priceTZS; }
        public void setPriceTZS(BigDecimal priceTZS) { this.priceTZS = priceTZS; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getTotalTZS() { return totalTZS; }
        public void setTotalTZS(BigDecimal totalTZS) { this.totalTZS = totalTZS; }
    }
}