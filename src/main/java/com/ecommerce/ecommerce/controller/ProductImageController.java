package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.entity.ProductImage;
import com.ecommerce.ecommerce.service.ProductImageService;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/upload/process")
    public ResponseEntity<?> uploadImage(
            @RequestParam Long productId,
            @RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "uploads/products"; // Customize your path
            ProductImage savedImage = productImageService.uploadAndProcessImage(productId, file, uploadDir);

            return ResponseEntity.ok(savedImage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"success\":false,\"message\":\"Internal server error.\"}");
        }
    }
}