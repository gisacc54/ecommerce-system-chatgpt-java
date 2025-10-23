// com.ecommerce.ecommerce.service.OrderHistoryService.java
package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderHistoryService(OrderRepository orderRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate comprehensive order history for a user with filters, pagination and sorting.
     */
    @Transactional(readOnly = true)
    public ComprehensiveOrderHistoryResponse getComprehensiveHistory(
            Long userId, Order.Status status, LocalDateTime startDate, LocalDateTime endDate) {

        List<Order> orders = orderRepository.findComprehensiveHistory(userId, status, startDate, endDate);

        if (orders.isEmpty()) {
            return new ComprehensiveOrderHistoryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO, Collections.emptyList());
        }

        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalOrders = (long) orders.size();
        BigDecimal avgOrderValue = totalSpent.divide(BigDecimal.valueOf(totalOrders), RoundingMode.HALF_UP);

        List<OrderHistoryResponse> orderDtos = orders.stream().map(this::mapToDto).toList();

        return new ComprehensiveOrderHistoryResponse(totalSpent, totalOrders, avgOrderValue, orderDtos);
    }

    private OrderHistoryResponse mapToDto(Order o) {
        List<OrderItemDto> items = o.getOrderItems().stream()
                .map(i -> new OrderItemDto(i.getId(),i.getProduct().getId(),i.getProduct().getName(), i.getQuantity(), i.getPrice()))
                .toList();

        List<PaymentDto> payments = o.getPayments().stream()
                .map(p -> new PaymentDto(p.getId(), p.getAmount(), p.getStatus().name(), p.getCreatedAt()))
                .toList();

        List<ReviewDto> reviews = o.getReviews().stream()
                .map(r -> new ReviewDto(r.getProduct().getName(), r.getRating(),r.getComment(), r.getCreatedAt()))
                .toList();

        return new OrderHistoryResponse(o.getId(), o.getStatusStr(), o.getTotalAmount(),
                o.getCreatedAt());
    }

    // Parse filters JSON into DTO, safely
    private OrderFilterDto parseFilters(String filtersJson) {
        if (filtersJson == null || filtersJson.isBlank()) {
            return new OrderFilterDto();
        }
        try {
            return objectMapper.readValue(filtersJson, OrderFilterDto.class);
        } catch (Exception e) {
            // If parsing fails, throw informative exception
            throw new IllegalArgumentException("Invalid filters JSON: " + e.getMessage(), e);
        }
    }

    // Map Order entity to DTO including items, payments and reviews
    private ComprehensiveOrderDto mapOrderToDto(Order order) {
        ComprehensiveOrderDto dto = new ComprehensiveOrderDto();
        dto.setOrderId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());

        // Map items (assumes Order has getOrderItems())
        if (order.getOrderItems() != null) {
            List<OrderItemDto> items = order.getOrderItems().stream().map(item -> {
                OrderItemDto it = new OrderItemDto();
                it.setId(item.getId());
                if (item.getProduct() != null) {
                    it.setProductId(item.getProduct().getId());
                    it.setProductName(item.getProduct().getName());
                }
                it.setQuantity(item.getQuantity());
                it.setUnitPrice(item.getPrice());
                return it;
            }).collect(Collectors.toList());
            dto.setItems(items);
        } else {
            dto.setItems(Collections.emptyList());
        }

        // Map payments (assumes Order has getPayments())
        if (order.getPayments() != null) {
            List<PaymentDto> payments = order.getPayments().stream().map(p -> {
                PaymentDto pd = new PaymentDto();
                pd.setId(p.getId());
                pd.setAmount(p.getAmount());
                pd.setMethod(p.getPaymentMethod());
                pd.setPaidAt(p.getPaidAt());
                return pd;
            }).collect(Collectors.toList());
            dto.setPayments(payments);
        } else {
            dto.setPayments(Collections.emptyList());
        }

        // Map reviews (assumes Order has getReviews() or reviews linked to items — adapt if necessary)
        if (order.getReviews() != null) {
            List<ReviewDto> reviews = order.getReviews().stream()
                    .map(r -> new ReviewDto(
                            r.getUser() != null ? r.getUser().getName() : null,
                            r.getRating(),
                            r.getComment(),
                            r.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            dto.setReviews(reviews);
        } else {
            dto.setReviews(Collections.emptyList());
        }

        return dto;
    }
}