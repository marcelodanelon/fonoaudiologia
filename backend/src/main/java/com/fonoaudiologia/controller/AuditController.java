package com.fonoaudiologia.controller;

import com.fonoaudiologia.entity.AuditLog;
import com.fonoaudiologia.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> findByEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogRepository.findByEntityIdAndEntityTypeOrderByCreatedAtDesc(entityId, entityType));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}
