package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    void deleteByUserId(Long userId);
}
