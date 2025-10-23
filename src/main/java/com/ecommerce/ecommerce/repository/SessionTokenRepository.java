package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    Optional<SessionToken> findByToken(String token);
    void deleteByUserId(Long userId);
}