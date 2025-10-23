package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * POST /wishlist/add - Add product to user's wishlist
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestBody Map<String, Long> request) {

        Long userId = request.get("userId");
        Long productId = request.get("productId");

        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> wishlist = wishlistService.addToWishlist(userId, productId);
            response.put("status", "success");
            response.put("wishlist", wishlist);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}