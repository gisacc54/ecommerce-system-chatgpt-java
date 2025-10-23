package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    void deleteByUserId(Long userId);
    /**
     * Check if a wishlist entry exists for a given user and product
     */
    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    /**
     * Fetch all wishlist entries for a given user
     */
    List<Wishlist> findByUser(User user);
}