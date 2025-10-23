package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.ReviewModerationRequest;
import com.ecommerce.ecommerce.dto.ReviewModerationResponse;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    /**
     * Moderate a product review by approving or rejecting it
     * @param reviewId - ID of the review
     * @param request - JSON payload containing the new status
     * @return ResponseEntity with updated review info
     */
    @PutMapping("/{id}/moderate")
    public ResponseEntity<ReviewModerationResponse> moderateReview(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody ReviewModerationRequest request
    ) {
        ReviewModerationResponse response = reviewService.moderateReview(reviewId, request);
        return ResponseEntity.ok(response);
    }
}
