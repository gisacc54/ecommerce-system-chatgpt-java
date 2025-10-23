package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    void deleteByUserId(Long userId);
}
