package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.dto.ProductSearchResponseDto;
import com.ecommerce.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable; // ✅ CORRECT
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    // Pessimistic lock when updating inventory
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    /**
     * Advanced search using weighted relevance:
     * 1. Full-text match (using LIKE as fallback)
     * 2. Region & category weighting
     * 3. Price proximity normalization
     */
    @Query(value = """
    SELECT p.id AS id, p.name AS name, p.description AS description, p.price AS price,
           ((CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) THEN 0.6 ELSE 0 END) +
            (CASE WHEN LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%')) THEN 0.4 ELSE 0 END) +
            (CASE WHEN (:category IS NOT NULL AND c.name = :category) THEN 0.1 ELSE 0 END) +
            (CASE WHEN (:minPrice IS NOT NULL AND :maxPrice IS NOT NULL AND p.price BETWEEN :minPrice AND :maxPrice) THEN 0.1 ELSE 0 END)
           ) AS relevance
    FROM products p
    LEFT JOIN categories c ON p.category_id = c.id
    WHERE (:term IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) 
                       OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%')))
    ORDER BY relevance DESC
    """, nativeQuery = true)
    List<ProductSearchResponseDto> searchAdvanced(
            @Param("term") String term,
            @Param("category") String category,
            @Param("minPrice") int minPrice,
            @Param("maxPrice") int maxPrice,
            Pageable pageable);
}