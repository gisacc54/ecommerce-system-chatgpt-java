// src/main/java/com/ecommerce/ecommerce/dto/CancelOrderResponse.java
package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CancelOrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal refundAmount; // in TZS
    private List<OrderItemDto> items;

    // getters / setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public static class OrderItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private String unitPriceTZS;

        // getters/setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getUnitPriceTZS() { return unitPriceTZS; }
        public void setUnitPriceTZS(String unitPriceTZS) { this.unitPriceTZS = unitPriceTZS; }
    }
}