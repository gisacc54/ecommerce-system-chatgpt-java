package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.ProductDetailedResponse;
import com.ecommerce.ecommerce.dto.ProductStockDto;
import com.ecommerce.ecommerce.dto.ReviewDto;
import com.ecommerce.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.ProductViewLog;
import com.ecommerce.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.ecommerce.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductViewLogRepository productViewLogRepository;
    private final ReviewRepository reviewRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductViewLogRepository productViewLogRepository, ReviewRepository reviewRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productViewLogRepository = productViewLogRepository;
        this.reviewRepository = reviewRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * Creates a new product
     *
     * @param name Product name
     * @param price Price in TZS
     * @param description Product description
     * @param categoryId Category ID
     * @return newly created Product
     * @throws IllegalArgumentException if category does not exist
     */
    @Transactional
    public Product createProduct(String name, java.math.BigDecimal price, String description, Long categoryId) {
        // Fetch category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setCategory(category);

        return productRepository.save(product);
    }

    /**
     * Search products by term in name or description (case-insensitive)
     */
    public List<Product> searchProducts(String term) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(term, term);
    }
    /**
     * Fetch product by ID.
     *
     * @param id product ID
     * @return Optional<Product>
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Reduce product stock quantity after a purchase
     *
     * @param productId ID of the product
     * @param quantity  Number of items purchased
     * @return updated stock quantity
     */
    @Transactional
    public int updateInventory(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        int currentStock = product.getStockQuantity();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (currentStock < quantity) {
            throw new IllegalArgumentException("Insufficient stock available");
        }

        // Reduce stock
        product.setStockQuantity(currentStock - quantity);

        // Save updated product
        productRepository.save(product);

        return product.getStockQuantity();
    }

    /**
     * Update product details
     */
    @Transactional
    public Long updateProduct(Long productId, UpdateProductRequest request) {
        // Fetch product from DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update fields if present in request
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        // Save updated product
        productRepository.save(product);

        return product.getId();
    }

    /**
     * Fetch all products from the database
     *
     * @return List of products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Method to get stock availability
    public ProductStockDto checkStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Stock is available if quantity > 0
        boolean available = product.getStockQuantity() != null && product.getStockQuantity() > 0;

        return new ProductStockDto(product.getId(), product.getStockQuantity(), available);
    }

    @Transactional
    public ProductDetailedResponse getProductDetailed(Long productId, Long userId, HttpServletRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // --- Dynamic pricing: example based on total views
        BigDecimal adjustedPrice = applyDynamicPricing(product);

        // --- Fetch reviews
        var reviews = reviewRepository.findByProductId(productId)
                .stream()
                .map(r -> new ReviewDto(
                        r.getUser().getName(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                ))
                .collect(Collectors.toList());

        // --- Fetch image URLs
        var images = productImageRepository.findByProductId(productId)
                .stream()
                .map(p -> p.getImagePath())
                .collect(Collectors.toList());

        // --- Log view
        logView(productId, userId, request);

        // --- Build response
        return new ProductDetailedResponse(
                product.getId(),
                product.getName(),
                adjustedPrice,
                product.getDescription(),
                reviews,
                images
        );
    }

    private BigDecimal applyDynamicPricing(Product product) {
        // Example: increase price by 0.1% per 100 views
        BigDecimal multiplier = BigDecimal.valueOf(1 + product.getStockQuantity() * 0.001 / 100);
        return product.getPrice().multiply(multiplier).setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    private void logView(Long productId, Long userId, HttpServletRequest request) {


        // Optional: increment product view counter
        Product product = productRepository.findById(productId).orElseThrow();
        product.setStockQuantity(product.getStockQuantity() + 1);
        productRepository.save(product);
    }

}