package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    void deleteByUserId(Long userId);
    /**
     * Find all reviews for a given product
     *
     * @param product Product entity
     * @return List of reviews
     */
    List<Review> findByProduct(Product product);
    List<Review> findByProductId(Long productId);
}
