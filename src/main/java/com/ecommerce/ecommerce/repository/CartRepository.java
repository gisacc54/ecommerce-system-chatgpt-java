package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Cart;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    void deleteByUserId(Long userId);
    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    Optional<Cart> findByUserAndProduct(User user, Product product);
    List<Cart> findByUserId(Long userId);
}