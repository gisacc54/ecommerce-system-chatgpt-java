package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ReviewModerationRequest;
import com.ecommerce.ecommerce.dto.ReviewModerationResponse;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    /**
     * Get reviews for a product by product ID
     *
     * @param productId product ID
     * @return List of Review or empty list if product not found or no reviews
     */
    public List<Review> getReviewsByProductId(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            return reviewRepository.findByProduct(productOpt.get());
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public ReviewModerationResponse moderateReview(Long reviewId, ReviewModerationRequest request) {
        // Fetch the review by ID
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Validate status (redundant if using @Pattern, but safe)
        String status = request.getStatus().toLowerCase();
        if (!status.equals("approved") && !status.equals("rejected")) {
            throw new RuntimeException("Invalid status. Must be 'approved' or 'rejected'.");
        }

        // Update review status
        review.setStatusStr(status);
        reviewRepository.save(review);

        // Return response
        return new ReviewModerationResponse(review.getId(), review.getStatusStr());
    }
}