package com.ecommerce.ecommerce.service;

// package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ProductReviewRequest;
import com.ecommerce.ecommerce.dto.ProductReviewResponse;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.ProductReview;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.ProductReviewRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductReviewService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ProductReviewService(ProductRepository productRepository, ProductReviewRepository reviewRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    // In ProductReviewService or ReviewService
    @Transactional
    public ProductReviewResponse submitReview(Long productId, ProductReviewRequest request) {
        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Bidhaa haipo kwenye mfumo."));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Mtumiaji haipo kwenye mfumo."));

        // Create new review
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(Review.Status.PENDING);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review); // now this works because type matches

        return new ProductReviewResponse(
                "Umefanikiwa kuandika maoni kwenye bidhaa",
                saved.getId()
        );
    }
}