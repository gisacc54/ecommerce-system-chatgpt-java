package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.entity.ProductImage;
import com.ecommerce.ecommerce.service.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * POST /products/images/upload
     * Upload a product image
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadProductImage(
            @RequestParam("productId") Long productId,
            @RequestParam("image") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductImage productImage = productImageService.uploadProductImage(productId, file);

            response.put("status", "success");
            response.put("productId", productId);
            response.put("imageUrl", productImage.getImagePath());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to upload file");
            return ResponseEntity.status(500).body(response);
        }
    }
}