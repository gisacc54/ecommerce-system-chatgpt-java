package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findFirstByEmailOrderByCreatedAtDesc(String email);
    Optional<PasswordReset> findByToken(String token);
}