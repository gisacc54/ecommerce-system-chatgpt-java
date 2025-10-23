package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.ProductImage;
import com.ecommerce.ecommerce.repository.ProductImageRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    private String uploadDir = Paths.get("").toAbsolutePath().toString() + "/uploads/products/";

    public ProductImageService(ProductRepository productRepository,
                               ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * Upload product image and save its path in database
     *
     * @param productId ID of the product
     * @param file      Multipart file uploaded
     * @return saved ProductImage entity
     * @throws IOException if saving fails
     */
    @Transactional
    public ProductImage uploadProductImage(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Ensure upload directory exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate unique filename
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;

        Path filepath = Paths.get(uploadDir, filename);

        // Save file to disk
        file.transferTo(filepath.toFile());

        // Save record in product_images table
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImagePath("uploads/products/" + filename);

        return productImageRepository.save(productImage);
    }

    /**
     * Extract file extension
     */
    private String getFileExtension(String originalFilename) {
        if (originalFilename == null) return "jpg";
        int dotIndex = originalFilename.lastIndexOf(".");
        return (dotIndex == -1) ? "jpg" : originalFilename.substring(dotIndex + 1);
    }
}