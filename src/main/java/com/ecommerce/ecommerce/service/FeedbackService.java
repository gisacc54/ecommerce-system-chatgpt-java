package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.FeedbackRequest;
import com.ecommerce.ecommerce.entity.Feedback;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.FeedbackRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public void submitFeedback(Long userId, FeedbackRequest request) {
        // 1. Fetch the authenticated user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Create Feedback entity
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setComment(request.getComment());
        feedback.setRating(request.getRating());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        // 3. Save feedback to DB
        feedbackRepository.save(feedback);
    }
}