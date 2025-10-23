// CartItemRepository.java
package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.CartItem;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    List<CartItem> findByUser(User user);
}