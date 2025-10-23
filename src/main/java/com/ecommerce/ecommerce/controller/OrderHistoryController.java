// com.ecommerce.ecommerce.controller.OrderHistoryController.java
package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.ComprehensiveOrderHistoryResponse;
import com.ecommerce.ecommerce.dto.OrdersHistoryResponseDto;
import com.ecommerce.ecommerce.entity.Order;
import com.ecommerce.ecommerce.service.OrderHistoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/orders/history")
@Validated
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @Autowired
    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    /**
     * GET /orders/history/comprehensive?user={id}&filters={json}&page=0&size=20&sort=createdAt,desc
     *
     * filters example:
     * {
     *   "startDate":"2025-10-01",
     *   "endDate":"2025-10-23",
     *   "status":"PAID"
     * }
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<ComprehensiveOrderHistoryResponse> getComprehensiveHistory(
            @RequestParam Long user,
            @RequestParam(required = false) String filtersJson) {

        // Parse optional filters safely
        Order.Status status = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (filtersJson != null && !filtersJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode filters = mapper.readTree(filtersJson);

                if (filters.has("status"))
                    status = Order.Status.valueOf(filters.get("status").asText());

                if (filters.has("startDate"))
                    startDate = LocalDate.parse(filters.get("startDate").asText()).atStartOfDay();

                if (filters.has("endDate"))
                    endDate = LocalDate.parse(filters.get("endDate").asText()).atTime(LocalTime.MAX);

            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        ComprehensiveOrderHistoryResponse response =
                orderHistoryService.getComprehensiveHistory(user, status, startDate, endDate);

        return ResponseEntity.ok(response);
    }
}