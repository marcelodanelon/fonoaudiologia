package com.fonoaudiologia.service;

import com.fonoaudiologia.entity.AuditLog;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.AuditLogRepository;
import com.fonoaudiologia.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, Long entityId, String details, String ipAddress) {
        User user = getCurrentUser();
        AuditLog log = new AuditLog(user, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    public void logWithChanges(String action, String entityType, Long entityId,
                               String changes, String ipAddress) {
        User user = getCurrentUser();
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(changes);
        log.setIpAddress(ipAddress);
        log.setUser(user);
        auditLogRepository.save(log);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        return null;
    }
}
