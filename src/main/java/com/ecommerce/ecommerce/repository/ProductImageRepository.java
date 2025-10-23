package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // Standard CRUD methods for product images
    List<ProductImage> findByProductId(Long productId);
}