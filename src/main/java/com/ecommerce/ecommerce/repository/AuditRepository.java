// src/main/java/com/ecommerce/ecommerce/repository/AuditRepository.java
package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {}