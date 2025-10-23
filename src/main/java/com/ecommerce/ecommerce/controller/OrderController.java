package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.CancelOrderResponse;
import com.ecommerce.ecommerce.dto.PlaceOrderRequest;
import com.ecommerce.ecommerce.dto.PlaceOrderResponse;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.OrderService;
import com.ecommerce.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint to place an order for the authenticated user.
     * Example: POST /orders/place
     */
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @RequestBody PlaceOrderRequest request
    ) {
        // ✅ Extract authenticated username (usually email)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName(); // set in JwtAuthFilter
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Continue with placing the order
        PlaceOrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order by ID
     *
     * @param orderId the ID of the order to cancel
     * @return CancelOrderResponse with status and refund info
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable("id") Long orderId) {
        // Delegate cancellation to the service
        CancelOrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}