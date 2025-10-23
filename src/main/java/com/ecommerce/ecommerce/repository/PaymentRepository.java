package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, Payment.Status status);
}