package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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

}