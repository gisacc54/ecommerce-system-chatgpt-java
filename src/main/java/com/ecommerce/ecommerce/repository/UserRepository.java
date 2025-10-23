package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // helper to check uniqueness
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    String findRoleById(Long id);

}