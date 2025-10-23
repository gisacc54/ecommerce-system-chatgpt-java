package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.Review;
import com.ecommerce.ecommerce.service.ProductService;
import com.ecommerce.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    public ProductController(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {

        Product product = productService.createProduct(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getCategoryId()
        );

        return ResponseEntity.ok(new CreateProductResponse("success", product.getId()));
    }

    /**
     * Perform basic product search by term
     * @param term search term for product name or description
     * @return JSON response with status and search results
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam("term") String term) {
        List<Product> products = productService.searchProducts(term);

        // Map products to response format with Tanzanian Shilling formatting
        List<Map<String, Object>> results = products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("price", String.format("%.2f TZS", product.getPrice()));
            map.put("description", product.getDescription());
            return map;
        }).collect(Collectors.toList());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /products/{id} - Fetch product details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    // Build product info map
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("name", product.getName());
                    productMap.put("price", String.format("%.2f TZS", product.getPrice()));
                    productMap.put("description", product.getDescription());

                    // Build response
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("product", productMap);

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    // Product not found
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Product not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * PUT /products/{id}/inventory
     * Update product stock quantity after purchase
     */
    @PutMapping("/{id}/inventory")
    public ResponseEntity<Map<String, Object>> updateInventory(
            @PathVariable("id") Long productId,
            @RequestBody Map<String, Integer> request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            int quantity = request.get("quantity");
            int updatedStock = productService.updateInventory(productId, quantity);

            response.put("status", "success");
            response.put("productId", productId);
            response.put("updatedStock", updatedStock);

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint to update a product by ID
     * PUT /products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request
    ) {
        // Call service to update the product
        Long updatedProductId = productService.updateProduct(id, request);

        // Return JSON response
        return ResponseEntity.ok().body(
                Map.of(
                        "status", "success",
                        "productId", updatedProductId
                )
        );
    }

    /**
     * GET /products
     * Fetch all products
     *
     * @return JSON response with list of products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        // Map product entities to a simplified DTO for response
        List<Map<String, Object>> productList = products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("price", product.getPrice()); // BigDecimal works here
            map.put("description", product.getDescription());
            return map;
        }).collect(Collectors.toList());

        // Return response in standard format
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "products", productList
        ));
    }


    // DTOs


    public static class CreateProductRequest {

        @NotBlank(message = "Product name is required")
        private String name;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;

        private String description;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        public CreateProductRequest(String name, BigDecimal price, String description, Long categoryId) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.categoryId = categoryId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }


    public static class CreateProductResponse {
        private String status;
        private Long productId;

        public CreateProductResponse(String status, Long productId) {
            this.status = status;
            this.productId = productId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }
    }
}