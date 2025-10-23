package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.dto.SalesReportDto;
import com.ecommerce.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // Fetch orders by userId with paging. Use EntityGraph to fetch items/payments if mapped.
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "payments", "reviews"})
    Page<Order> findByUserIdAndCreatedAtBetweenAndStatusIn(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            List<Order.Status> statuses,
            Pageable pageable
    );

    // Overloads to handle no status filter (call separately in service)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "payments", "reviews"})
    Page<Order> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "payments", "reviews"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.payments " +
            "WHERE o.user.id = :userId")
    List<Order> findOrdersWithItemsAndPayments(@Param("userId") Long userId);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH o.payments p
        LEFT JOIN FETCH o.reviews r
        WHERE o.user.id = :userId
        AND (:status IS NULL OR o.status = :status)
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
        ORDER BY o.createdAt DESC
        """)
    List<Order> findComprehensiveHistory(
            @Param("userId") Long userId,
            @Param("status") Order.Status status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    // You can add other variations depending on your entity structure
}
