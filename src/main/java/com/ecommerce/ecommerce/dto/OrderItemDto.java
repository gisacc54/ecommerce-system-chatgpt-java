package com.ecommerce.ecommerce.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/** Lightweight item DTO for email / response formatting */
public record OrderItemDto(String productName, Integer quantity, BigDecimal price) { }