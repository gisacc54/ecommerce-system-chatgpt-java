package com.ecommerce.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailedResponse {

    private Long id;
    private String name;
    private BigDecimal priceTZS; // adjusted price
    private String description;
    private List<ReviewDto> reviews;
    private List<String> images;

    public ProductDetailedResponse(Long id, String name, BigDecimal priceTZS, String description, List<ReviewDto> reviews, List<String> images) {
        this.id = id;
        this.name = name;
        this.priceTZS = priceTZS;
        this.description = description;
        this.reviews = reviews;
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceTZS() {
        return priceTZS;
    }

    public void setPriceTZS(BigDecimal priceTZS) {
        this.priceTZS = priceTZS;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDto> reviews) {
        this.reviews = reviews;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}

