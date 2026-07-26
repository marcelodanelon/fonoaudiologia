package com.fonoaudiologia.repository;

import com.fonoaudiologia.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<AuditLog> findByEntityIdAndEntityTypeOrderByCreatedAtDesc(Long entityId, String entityType);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
