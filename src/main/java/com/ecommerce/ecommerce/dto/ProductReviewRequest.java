package com.ecommerce.ecommerce.dto;
// package com.ecommerce.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Request DTO
public class ProductReviewRequest {

    @NotNull(message = "User ID lazima itolewe") // "must be provided"
    private Long userId;

    @NotNull(message = "Rating lazima itolewe")
    @Min(value = 1, message = "Rating lazima iwe angalau 1")
    @Max(value = 5, message = "Rating haipaswi kuzidi 5")
    private Integer rating;

    @NotBlank(message = "Maoni hayanaweza kuwa tupu") // "Comment cannot be blank"
    private String comment;

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

