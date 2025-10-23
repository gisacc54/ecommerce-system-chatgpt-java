package com.ecommerce.ecommerce.controller;

// package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.ProductReviewRequest;
import com.ecommerce.ecommerce.dto.ProductReviewResponse;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.service.ProductReviewService;
import com.ecommerce.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductReviewController {

    private final ProductReviewService productReviewService;
    private final ReviewService reviewService;

    public ProductReviewController(ProductReviewService productReviewService, ReviewService reviewService) {
        this.productReviewService = productReviewService;
        this.reviewService = reviewService;
    }

    /**
     * Submit a review for a specific product
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ProductReviewResponse> submitReview(
            @PathVariable("id") Long productId,
            @Valid @RequestBody ProductReviewRequest request
    ) {
        ProductReviewResponse response = productReviewService.submitReview(productId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /products/{id}/reviews - Fetch reviews for a product
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> getProductReviews(@PathVariable("id") Long productId) {

        List<Review> reviews = reviewService.getReviewsByProductId(productId);

        // Build review response list
        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("userId", r.getUser().getId());
            reviewMap.put("rating", r.getRating());
            reviewMap.put("comment", r.getComment());
            reviewMap.put("createdAt", r.getCreatedAt());
            reviewList.add(reviewMap);
        }

        // Build final response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("reviews", reviewList);

        return ResponseEntity.ok(response);
    }
}
