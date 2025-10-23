package com.ecommerce.ecommerce.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/** Response returned by endpoint */
public record OrderConfirmationResponse(Long orderId, BigDecimal totalAmount, String status, boolean confirmationSent, String message, LocalDateTime confirmedAt) { }