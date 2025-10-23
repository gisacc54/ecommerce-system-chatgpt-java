package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.CartService;
import com.ecommerce.ecommerce.service.UserService;
import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request) {
        // Call service to add/update cart and return updated cart info
        return ResponseEntity.ok(cartService.addToCart(request.getUserId(),request.getProductId(),request.getQuantity()));
    }

    /**
     * Endpoint to calculate the total amount of the authenticated user's cart.
     * Optionally, a coupon code can be applied via query param.
     */
    @GetMapping("/total")
    public ResponseEntity<?> getCartTotal(
            @RequestParam(value = "couponCode", required = false) String couponCode
    ) {
        // ✅ Get currently authenticated user's email or username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName(); // usually the email

        // ✅ Find user by email
        Long userId = userRepository.findByEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Delegate calculation to service
        CartTotalResponse response = cartService.calculateCartTotal(userId, couponCode);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /cart/empty
     * Empties the authenticated user's cart.
     */
    @DeleteMapping("/empty")
    public ResponseEntity<EmptyCartResponse> emptyCart(
             // injected from JWT/session
    ) {

        // ✅ Get currently authenticated user's email or username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName(); // usually the email

        // ✅ Find user by email
        Long userId = userRepository.findByEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new EmptyCartResponse("User not authenticated", 0));
        }

        int deletedCount = cartService.emptyCart(userId);

        String message = deletedCount == 0 ?
                "No items in cart" :
                "Cart emptied successfully";

        return ResponseEntity.ok(new EmptyCartResponse(message, 0));
    }

    @PostMapping("/add/with-recs")
    public ResponseEntity<CartAddResponseDto> addToCartWithRecs(@Valid @RequestBody CartAddRequestDto request) {
        CartAddResponseDto response = cartService.addToCartWithRecs(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total/complex")
    public ResponseEntity<CartTotalResponse> getComplexTotal(@RequestParam Long userId) {
        CartTotalResponse response = cartService.getComplexCartTotal(userId);
        return ResponseEntity.ok(response);
    }
}