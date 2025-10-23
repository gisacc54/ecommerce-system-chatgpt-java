package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.dto.SalesReportDto;
import com.ecommerce.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    void deleteByUserId(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findById(Long id);

    /**
     * Find an order with its items and associated user in a single query to avoid n+1.
     */
    @Query("""
        select o from Order o
        left join fetch o.orderItems oi
        left join fetch o.user u
        where o.id = :id
        """)
    Optional<Order> findByIdWithItemsAndUser(Long id);

    @Query(value = """
    SELECT 
        COUNT(*) AS totalOrders,
        COALESCE(SUM(total_amount), 0) AS totalSalesAmount
    FROM orders
    WHERE created_at BETWEEN :start AND :end
""", nativeQuery = true)
    SalesReportDto getSalesReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
