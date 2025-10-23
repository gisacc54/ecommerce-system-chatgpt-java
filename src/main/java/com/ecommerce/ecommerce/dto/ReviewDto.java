package com.ecommerce.ecommerce.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class ReviewDto {
    private String userName;
    private Integer rating;
    private String comment;
    private String createdAt;

    public ReviewDto(String userName, Integer rating, String comment, LocalDateTime createdAt) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = String.valueOf(createdAt);
    }


    public ReviewDto() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}