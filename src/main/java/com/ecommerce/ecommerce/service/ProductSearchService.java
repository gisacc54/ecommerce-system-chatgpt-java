package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ProductSearchResponseDto;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable; // ✅ CORRECT

import java.util.*;

@Service
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductSearchService(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Parse filters JSON into Map
     */
    public Map<String, Object> parseFilters(String filtersJson) {
        if (filtersJson == null || filtersJson.isEmpty()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(filtersJson, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap(); // Invalid JSON treated as empty
        }
    }

    /**
     * Perform advanced search with relevance scoring
     */
    public List<ProductSearchResponseDto> searchProducts(String term, String category,
                                                         int minPrice, int maxPrice,
                                                         Pageable pageable) {


        List<ProductSearchResponseDto> results = productRepository.searchAdvanced(term, category, minPrice, maxPrice, pageable);

// Use directly — no Object[] mapping needed
        return results;
    }


}