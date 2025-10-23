package com.ecommerce.ecommerce.repository;

// package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.ProductReview;
import com.ecommerce.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<Review, Long> {
}
