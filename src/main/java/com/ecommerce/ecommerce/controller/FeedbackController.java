package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.FeedbackRequest;
import com.ecommerce.ecommerce.dto.FeedbackResponse;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public FeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(// Injected from JWT/session
            @Valid @RequestBody FeedbackRequest request
    ) {
        // ✅ Extract authenticated username (usually email)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(new FeedbackResponse("User not authenticated"));
        }

        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new FeedbackResponse("User not authenticated"));
        }

        // Call service to save feedback
        feedbackService.submitFeedback(userId, request);

        // Return success message
        return ResponseEntity.ok(new FeedbackResponse("Feedback submitted successfully"));
    }
}