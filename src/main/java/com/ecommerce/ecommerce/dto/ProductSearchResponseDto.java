package com.ecommerce.ecommerce.dto;

public class ProductSearchResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double priceTZS;
    private Double relevance;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPriceTZS() { return priceTZS; }
    public void setPriceTZS(Double priceTZS) { this.priceTZS = priceTZS; }
    public Double getRelevance() { return relevance; }
    public void setRelevance(Double relevance) { this.relevance = relevance; }

    public ProductSearchResponseDto(Long id, String name, String description, Double priceTZS, Double relevance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priceTZS = priceTZS;
        this.relevance = relevance;
    }
}
