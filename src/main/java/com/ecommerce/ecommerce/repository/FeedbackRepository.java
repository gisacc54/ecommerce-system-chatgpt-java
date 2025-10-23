package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    void deleteByUserId(Long userId);
}
