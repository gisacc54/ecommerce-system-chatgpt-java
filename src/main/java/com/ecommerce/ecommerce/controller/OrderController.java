package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.dto.CancelOrderResponse;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.OrderService;
import com.ecommerce.ecommerce.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * GET /orders/history?user={id}
     * Fetch all past orders for a given user.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(@RequestParam("user") Long userId) {
        try {
            // 1️⃣ Fetch the user’s order history
            List<OrderHistoryResponse> orderHistory = orderService.getUserOrderHistory(userId);

            // 2️⃣ If no orders found, return empty list
            if (orderHistory.isEmpty()) {
                return ResponseEntity.ok().body(List.of());
            }

            // 3️⃣ Return the list of past orders
            return ResponseEntity.ok(orderHistory);

        } catch (IllegalArgumentException e) {
            // 4️⃣ Handle user not found
            return ResponseEntity.status(404).body(
                    new ErrorResponse("User not found")
            );
        } catch (Exception e) {
            // 5️⃣ Catch any other server errors
            return ResponseEntity.status(500).body(
                    new ErrorResponse("An error occurred while fetching order history")
            );
        }
    }



    // ✅ Send confirmation email for a given order ID
    @PostMapping("/confirm/{id}")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            OrderConfirmationResponse response = orderService.sendOrderConfirmationEmail(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Order not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("Failed to send confirmation: " + e.getMessage()));
        }
    }

    // ✅ Define this only once
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}