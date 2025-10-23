package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceDto {
    private Long orderId;
    private String userName;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private List<InvoiceItemDto> items;
    private String qrCodeBase64;

    public InvoiceDto(Long orderId, String userName, String shippingAddress, BigDecimal totalAmount, List<InvoiceItemDto> items) {
        this.orderId = orderId;
        this.userName = userName;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public InvoiceDto(Long orderId, String userName, String shippingAddress, BigDecimal totalAmount, List<InvoiceItemDto> items, String qrCodeBase64) {
        this.orderId = orderId;
        this.userName = userName;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.items = items;
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public Long getOrderId() { return orderId; }
    public String getUserName() { return userName; }
    public String getShippingAddress() { return shippingAddress; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<InvoiceItemDto> getItems() { return items; }
}