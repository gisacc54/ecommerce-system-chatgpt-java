package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.ProductImage;
import com.ecommerce.ecommerce.repository.ProductImageRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
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

    public ProductImage uploadAndProcessImage(Long productId, MultipartFile file, String uploadDir) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, WEBP allowed.");
        }

        // Validate file size (limit to 5MB for example)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit.");
        }

        // Convert MultipartFile to BufferedImage
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Perform content moderation (pseudo-code, integrate with Google Vision / AWS)
        boolean safeContent = moderateImage(file);
        if (!safeContent) {
            throw new IllegalArgumentException("Image failed moderation.");
        }

        // Generate unique file names
        String uniqueName = UUID.randomUUID().toString();
        String originalFileName = uploadDir + "/" + uniqueName + "_original.png";
        String thumbnailFileName = uploadDir + "/" + uniqueName + "_thumb.png";
        String mediumFileName = uploadDir + "/" + uniqueName + "_medium.png";
        String largeFileName = uploadDir + "/" + uniqueName + "_large.png";

        // Save original image
        ImageIO.write(originalImage, "png", new File(originalFileName));

        // Resize variants
        Thumbnails.of(originalImage).size(150, 150).toFile(new File(thumbnailFileName));
        Thumbnails.of(originalImage).size(500, 500).toFile(new File(mediumFileName));
        Thumbnails.of(originalImage).size(1024, 1024).toFile(new File(largeFileName));

        Optional<Product> product = productRepository.findById(productId);
        // Save metadata to DB
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product.get());
        productImage.setImagePath(originalFileName);
        productImage.setVariant(uniqueName);
        productImage.setCreatedAt(LocalDateTime.now());

        return productImageRepository.save(productImage);
    }

    private boolean moderateImage(MultipartFile file) {
        // TODO: Integrate with Google Vision API or AWS Rekognition
        // Return true if image passes moderation
        return true;
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