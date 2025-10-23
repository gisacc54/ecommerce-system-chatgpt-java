package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.ProductSearchResponseDto;
import com.ecommerce.ecommerce.service.ProductSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // ✅ CORRECT
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductSearchController {

    private final ProductSearchService searchService;

    public ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Advanced search endpoint
     * GET /products/search/advanced?term={query}&filters={json}
     */

    @GetMapping("/search/advanced")
    public ResponseEntity<?> searchAdvanced(
            @RequestParam String term,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        System.out.println("searchAdvanced " + term + " " + minPrice + " " + maxPrice);
        // Safe defaults if null
        int safeMinPrice = minPrice != null ? minPrice : 0;
        int safeMaxPrice = maxPrice != null ? maxPrice : Integer.MAX_VALUE;

        Pageable pageable = PageRequest.of(page, size);
        List<ProductSearchResponseDto> results = searchService.searchProducts(
                term, category, safeMinPrice, safeMaxPrice, pageable);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "results", results
        ));
    }
}